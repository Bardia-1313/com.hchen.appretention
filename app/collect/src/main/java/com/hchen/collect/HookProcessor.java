package com.hchen.collect;
import com.google.auto.service.AutoService;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.Set;
import java.util.function.Consumer;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
@AutoService(Processor.class)
@SupportedAnnotationTypes("com.hchen.collect.HookEntrance")
@SupportedSourceVersion(SourceVersion.RELEASE_17)
public class HookProcessor extends AbstractProcessor {
    boolean isProcessed = false;
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (isProcessed) return true;
        isProcessed = true;
        try (Writer writer = processingEnv.getFiler().createSourceFile("com.hchen.appretention.hook.EntranceMap").openWriter()) {
            writer.write("""
                package com.hchen.appretention.hook;
                import java.util.HashMap;
                public class EntranceMap {
                    public String mTargetBrand;
                    public String mTargetPackage;
                    public int[] mTargetSdks;
                    public float mTargetOS;
                    public boolean mDownward;
                    public boolean mUpward;
                    public boolean isHyperOS;
                    public EntranceMap(String targetBrand, String targetPackage, int[] targetSdks, float targetOS, boolean isHyperOS, boolean downward, boolean upward){
                        this.mTargetBrand = targetBrand;
                        this.mTargetPackage = targetPackage;
                        this.mTargetSdks = targetSdks;
                        this.mTargetOS = targetOS;
                        this.isHyperOS = isHyperOS;
                        this.mDownward = downward;
                        this.mUpward = upward;
                    }
                    public static HashMap<String, EntranceMap> get() {
                        HashMap<String, EntranceMap> dataMap = new HashMap<>();
                """);
            roundEnv.getElementsAnnotatedWith(HookEntrance.class).forEach(new Consumer<Element>() {
                @Override
                public void accept(Element element) {
                    String fullClassName = null;
                    if (element instanceof TypeElement typeElement) {
                        fullClassName = typeElement.getQualifiedName().toString();
                        if (fullClassName == null) {
                            throw new RuntimeException("E: Full class name is null!!");
                        }
                    }
                    HookEntrance hookEntrance = element.getAnnotation(HookEntrance.class);
                    String targetBrand = hookEntrance.targetBrand();
                    String targetPackage = hookEntrance.targetPackage();
                    int[] targetSdks = hookEntrance.targetSdks();
                    float targetOS = hookEntrance.targetOS();
                    boolean isHyperOS = hookEntrance.isHyperOS();
                    boolean downward = hookEntrance.downward();
                    boolean upward = hookEntrance.upward();
                    String targetSdkStrings = Arrays.toString(targetSdks).replace("[", "").replace("]", "");
                    try {
                        writer.write("        ");
                        writer.write("dataMap.put(\"" + fullClassName + "\", new EntranceMap(\"" + targetBrand + "\", "
                            + "\"" + targetPackage + "\"" + ", new int[]{" + targetSdkStrings + "}, " + targetOS + "f, " + isHyperOS + ", " + downward + ", " + upward + "));\n");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            writer.write("""
                        return dataMap;
                    }
                }
                """);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }
}
