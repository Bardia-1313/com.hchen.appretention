# AppRetention

Hardened LSPosed module to prevent automated system kills and extend background process retention.

## Overview
This is a hardened fork of the original AppRetentionHook, optimized for Android 10-16 and HyperOS. It enforces a "Force-Stop-Only" policy to block automated background cleanup while allowing explicit user-initiated actions.

## Repository
Current Fork: [Bardia-1313/AppRetention](https://github.com/Bardia-1313/com.hchen.appretention)
Original Project: [HChenX/AppRetentionHook](https://github.com/HChenX/AppRetentionHook)

### Supported Systems
- **HyperOS V1 / V2**
- **AOSP 10 - 16**
- **Samsung OneUI** (Experimental)
- **ColorOS** (Limited support)

## Core Logic
The module targets internal `system_server` entrypoints, primarily within `ProcessList` and `ActivityManagerService`.

### Intercepted Kill Reasons:
- Device idle cleanup
- Process limit enforcement
- Cached app freezing (Android 14+)
- Scheduled task cleanup
- Recents task removal (Converted to record-only removal)

### Exclusions (Not Blocked):
- Kernel-level LMKD (Low Memory Killer)
- Application crashes / ANRs
- Explicit user force-stop (via App Info)
- Package uninstallation/updates (for system stability)

## Configuration
The module behavior can be toggled via System Properties:
- `persist.hchen.retention.force_stop_only`: Master switch.
- `persist.hchen.retention.protect_recents`: Prevents kill on Recents swipe.
- `persist.hchen.retention.disable_freezer`: Disables CachedAppOptimizer freezing.

## Installation
1. Install the APK.
2. Enable the module in LSPosed manager.
3. Configure Scope:
    - **MIUI/HyperOS**: `System Framework` + `PowerKeeper` (com.miui.powerkeeper) + `Security Center` (com.miui.securitycenter).
    - **Other Systems**: `System Framework`.
4. Reboot device.

## Acknowledgments
Parts of the logic are inspired by:
- [Cemiuiler](https://github.com/Team-Cemiuiler/Cemiuiler)
- [Don't Kill](https://github.com/HChenX/Don-t-Kill)

## License
Licensed under GNU General Public License v3.0.
