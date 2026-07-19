@echo off
setlocal enabledelayedexpansion

echo Verifying AppRetention project files...

powershell -NoProfile -ExecutionPolicy Bypass -Command ^
  "$ErrorActionPreference='Stop';" ^
  "$settings = Get-Content -Path 'settings.gradle' -Raw;" ^
  "$build = Get-Content -Path 'build.gradle' -Raw;" ^
  "$policy = Get-Content -Path 'app\src\main\java\com\hchen\appretention\hook\system\opt\ForceStopOnlyPolicy.java' -Raw;" ^
  "if (-not $settings.StartsWith('pluginManagement')) { throw 'settings.gradle is corrupted'; }" ^
  "if ($settings -match '# Add project specific ProGuard rules here') { throw 'ProGuard text found inside settings.gradle'; }" ^
  "if ($settings -match '(?m)^\s*src[\\/].*') { throw 'Bare src path found inside settings.gradle'; }" ^
  "if ($build -match '@rem Copyright') { throw 'gradlew.bat content found inside build.gradle'; }" ^
  "if ($build -notmatch 'com.android.application') { throw 'build.gradle does not contain Android plugin declaration'; }" ^
  "if ($policy -match 'setArgs\s*\(') { throw 'Invalid setArgs(...) call still exists in ForceStopOnlyPolicy.java'; }" ^
  "if ($policy -notmatch 'setArg\s*\(\s*argIndex\s*,\s*false\s*\)') { throw 'Expected setArg(argIndex, false) patch not found'; }" ^
  "Write-Host 'OK: project root and ForceStopOnlyPolicy.java are clean.'"

if errorlevel 1 (
    echo Verification failed.
    exit /b 1
)

echo Verification passed.
exit /b 0

REM Fix6 warning checks

findstr /S /N /C:"getParcelableExtra(\"logData\")" app\src\main\java\*.java >nul 2>&1
if %ERRORLEVEL% EQU 0 (
  echo ERROR: legacy getParcelableExtra warning source still exists.
  exit /b 1
)

findstr /S /N /C:"ArrayList<Object> pendingCompactionProcesses = (ArrayList<Object>)" app\src\main\java\*.java >nul 2>&1
if %ERRORLEVEL% EQU 0 (
  echo ERROR: unchecked pendingCompactionProcesses cast still exists.
  exit /b 1
)

echo Fix6 warning checks passed.
