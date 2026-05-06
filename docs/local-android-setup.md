# Local Android Setup

This workspace uses a repo-local Android toolchain so the project can build without Android Studio being configured globally.

## Installed Toolchain

- Java: Eclipse Adoptium JDK 21
- Android SDK root: `.android-sdk`
- Android command line tools: `20.0`
- Android platform: `platforms;android-36`
- Android build tools: `build-tools;36.0.0`
- Android platform-tools: `37.0.0`
- Android NDK: `ndk;27.0.12077973`

## Shell Setup

From PowerShell:

```powershell
Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass -Force
. .\scripts\android-env.ps1
```

This sets `ANDROID_HOME`, `ANDROID_SDK_ROOT`, `ANDROID_USER_HOME`, `GRADLE_USER_HOME`, and prepends SDK tools to `PATH` for the current shell.

## Useful Commands

```powershell
Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass -Force
. .\scripts\android-env.ps1
.\gradlew.bat test
.\gradlew.bat assembleNonRoot_gameDebug
adb devices
```

`local.properties` points Gradle at the repo-local SDK and is intentionally ignored by Git.

The current debug APK outputs are split by ABI under:

```text
app/build/outputs/apk/nonRoot_game/debug/
```

For Quest-class devices, the relevant split is the `arm64-v8a` APK.
