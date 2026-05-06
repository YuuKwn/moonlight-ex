# Quest Panel APK

Milestone 1 introduces a `quest` product flavor for a non-root Horizon OS panel build.

## Build

From PowerShell:

```powershell
. .\scripts\android-env.ps1
.\gradlew.bat assembleQuestDebug
```

The debug APKs are emitted under:

```text
app/build/outputs/apk/quest/debug/
```

## Flavor Notes

- Application ID: `dev.moonlightvr.quest`
- Native build argument: `PRODUCT_FLAVOR=nonRoot`
- Debug label still comes from the shared debug build type.
- The Quest manifest overlay removes Android TV EPG permissions and applies Horizon panel default sizes to the main host, app, settings, diagnostics, and stream activities.
- Quest streaming presets are available at the top of Streaming Settings in the Quest build only.
- Quest Diagnostics launches the existing debug screen with headset build, network, decoder, and gamepad information.

## Headset Validation Checklist

- Sideload one `arm64-v8a` Quest debug APK from `app/build/outputs/apk/quest/debug/`.
- Launch from unknown sources or dev mode.
- Pair with Sunshine or Apollo.
- Validate 1080p/60, 1080p/90, and 1440p/90 where the host and network support them.
- Confirm a physical gamepad is detected in Quest Diagnostics and works during stream.
- Confirm Touch controller panel interaction can reach host selection, settings, diagnostics, stream start, and stream stop.
