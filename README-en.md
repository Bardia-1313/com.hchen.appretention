<div align="center">
<h1>AppRetention Framework</h1>

![stars](https://img.shields.io/github/stars/HChenX/AppRetentionHook?style=flat)
![downloads](https://img.shields.io/github/downloads/HChenX/AppRetentionHook/total)
![Github repo size](https://img.shields.io/github/repo-size/HChenX/AppRetentionHook)
[![GitHub release (latest by date)](https://img.shields.io/github/v/release/HChenX/AppRetentionHook)](https://github.com/HChenX/AppRetentionHook/releases)
![language](https://img.shields.io/badge/language-java-purple)

<p><b><a href="README-en.md">English</a> | <a href="README.md">简体中文</a></b></p>
<p>Advanced System-Level Instrumentation for Background Process Retention</p>
</div>

---

## 🛠 Project Overview

**AppRetention** is a specialized LSPosed module designed to instrument the Android `system_server` and intercept process termination logic. By implementing a **Force-Stop-Only** policy, it ensures that background applications remain resident in memory unless explicitly terminated by the user or required by critical system maintenance.

### Key Capabilities
- **Automated Kill Interception**: Blocks system-initiated process trimming based on idle state, process count limits, or cached app policies.
- **ROM-Specific Adaptation**: Tailored logic for **HyperOS (V1/V2)**, **AOSP (10-16)**, and **Samsung OneUI**.
- **OOM Adj Optimization**: Dynamically adjusts process priorities to stabilize the background LRU list.
- **Recents Protection**: Prevents process death when swiping tasks from the Recent Apps screen.

---

## ⚙️ How it Works

The module hooks into core Android framework entrypoints:
- `ActivityManagerService`: Intercepts `killBackgroundProcesses` and `stopAppForUser`.
- `ProcessList`: Filters `killPackageProcesses` based on exit reasons and descriptions.
- `CachedAppOptimizer`: Disables or tunes the background freezer and compaction triggers.

### Intercepted Vectors:
- **Idle Cleanup**: Prevents the system from killing apps left unused for extended periods.
- **Memory Pressure (Framework-level)**: Blocks non-critical cleanup triggered by the `system_server`.
- **Process Limits**: Overrides the maximum background process constraints.

*Note: This module does not interfere with the kernel-level Low Memory Killer (LMKD) or application-level crashes/ANRs.*

---

## 🚀 Installation & Configuration

1. **Install** the latest release.
2. **Enable** in the LSPosed Manager.
3. **Configure Scope**:
    - **MIUI / HyperOS**: `System Framework`, `Security Center`, and `Battery & Performance (powerkeeper)`.
    - **AOSP / Other**: `System Framework`.
4. **Reboot** to apply instrumentation.

### System Properties
Advanced users can tune the module via `setprop`:
- `persist.hchen.retention.force_stop_only`: Toggle master policy.
- `persist.hchen.retention.protect_recents`: Toggle Recents protection.
- `persist.hchen.adj.opt.enable`: Toggle OOM Adjustment optimization.

---

## ⚖️ Technical Disclaimer

This module modifies critical system behaviors. While optimized for stability, users should be aware that preventing automated cleanup may lead to:
- Increased memory usage under heavy multitasking.
- Higher standby power consumption if many active services are retained.
- Potential system freezes if physical RAM is fully exhausted.

**Always maintain a recovery backup before applying system-level hooks.**

---

## 🙏 Credits

Technical inspiration and reference logic:
- [Cemiuiler Project](https://github.com/Team-Cemiuiler/Cemiuiler)
- [Don't Kill](https://github.com/HChenX/Don-t-Kill)

---
<div align="center">
Built for power users and system enthusiasts.
</div>
