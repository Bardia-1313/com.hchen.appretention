package com.hchen.appretention.hook.system.opt;
import static com.hchen.appretention.data.method.SystemMethod.onLmkdConnect;
import static com.hchen.appretention.data.method.SystemMethod.updateOomLevels;
import static com.hchen.appretention.data.method.SystemMethod.writeLmkd;
import static com.hchen.appretention.data.path.SystemClass.ProcessList;
import static com.hchen.hooktool.core.CoreTool.getField;
import static com.hchen.hooktool.core.CoreTool.hookConstructor;
import static com.hchen.hooktool.core.CoreTool.hookMethod;
import static com.hchen.hooktool.core.CoreTool.setField;
import android.system.Os;
import android.system.OsConstants;
import com.hchen.appretention.data.field.SystemField;
import com.hchen.hooktool.hook.IHook;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
public final class OomLevelsOpt {
    private static final int OOM_MIN_FREE_DISCOUNT = 3;
    private static final int PAGE_SIZE = (int) Os.sysconf(OsConstants._SC_PAGESIZE);
    private static Object mProcessListInstance = null;
    public static void init() {
        hookConstructor(ProcessList,
            new IHook() {
                @Override
                public void after() {
                    mProcessListInstance = thisObject();
                }
            }
        );
        hookMethod(ProcessList,
            onLmkdConnect,
            OutputStream.class,
            new IHook() {
                @Override
                public void before() {
                    updateOomMinFree(thisObject());
                }
            }
        );
        hookMethod(ProcessList,
            updateOomLevels,
            int.class, int.class, boolean.class,
            new IHook() {
                @Override
                public void after() {
                    updateOomMinFree(thisObject());
                }
            }
        );
        hookMethod(ProcessList,
            writeLmkd,
            ByteBuffer.class, ByteBuffer.class,
            new IHook() {
                @Override
                public void before() {
                    ByteBuffer buffer = (ByteBuffer) getArg(0);
                    if (buffer == null) return;
                    ByteBuffer bufCopy = buffer.duplicate();
                    bufCopy.rewind();
                    if (bufCopy.getInt() == 0) {
                        setOomMinFreeBuf(bufCopy);
                        setArg(0, buffer);
                    }
                }
                private void setOomMinFreeBuf(ByteBuffer bufCopy) {
                    int[] mOomAdj = (int[]) getField(mProcessListInstance, SystemField.mOomAdj);
                    int[] mOomMinFree = (int[]) getField(mProcessListInstance, SystemField.mOomMinFree);
                    if (mOomMinFree == null || mOomAdj == null)
                        return;
                    int[] mOomMinFreeArray = updateOomMinFree(mProcessListInstance);
                    if (mOomMinFreeArray == null) return;
                    bufCopy.rewind();
                    bufCopy.putInt(0);
                    for (int i = 0; i < mOomAdj.length; i++) {
                        bufCopy.putInt(((mOomMinFreeArray[i] * 1024) / PAGE_SIZE));
                        bufCopy.putInt(mOomAdj[i]);
                    }
                }
            }
        );
    }
    private static int[] updateOomMinFree(Object processListInstance) {
        int[] mOomMinFree = (int[]) getField(processListInstance, SystemField.mOomMinFree);
        if (mOomMinFree == null)
            return null;
        int[] mOomMinFreeArray = Arrays.stream(mOomMinFree).map(operand -> operand / OOM_MIN_FREE_DISCOUNT).toArray();
        setField(processListInstance, SystemField.mOomMinFree, mOomMinFreeArray);
        return mOomMinFreeArray;
    }
}
