# Moonlight on Meta Quest VR Feasibility Report

Date: 2026-05-06

Target: Quest 3 first, with Quest 3S/Quest 2 as possible follow-on targets.

Purpose: evaluate how to turn this Moonlight/Artemis Android fork into a VR-first client for Meta Horizon OS, and provide enough technical context for a future Codex session to begin implementation without rediscovering the project.

## Project Progress Log

### 2026-05-06 - Milestone 0 Started

Goal: get the current fork buildable and locally testable before Quest-specific changes.

Current local environment findings:

- Java is installed: Eclipse Adoptium JDK 21 at `C:\Program Files\Eclipse Adoptium\jdk-21.0.9.10-hotspot`.
- `ANDROID_HOME` and `ANDROID_SDK_ROOT` are not set.
- No Android SDK was found at `C:\Users\fabio\AppData\Local\Android\Sdk`, `C:\Android\Sdk`, `F:\Android\Sdk`, or repo-local `.android-sdk`.
- No `local.properties` exists yet.
- Android CLI tools (`sdkmanager`, `adb`) are not on PATH.
- `gradlew.bat --version` failed because Gradle tried to write under `C:\Users\CodexSandboxOffline\.gradle`; this project should use repo-local `GRADLE_USER_HOME=.gradle` during setup/testing.
- `app/src/main/jni/moonlight-core/moonlight-common-c` is empty even though `.gitmodules` points it to `https://github.com/ClassicOldSong/moonlight-common-c`.

Planned immediate setup:

- [x] Install Android command line tools into repo-local `.android-sdk`.
- [x] Install required Android SDK packages: platform API 36, build tools, platform-tools, and NDK `27.0.12077973`.
- [x] Create `local.properties` pointing Gradle at `.android-sdk`.
- [x] Add `scripts/android-env.ps1` so local shells can use repo-local Android/Gradle homes.
- [x] Add `docs/local-android-setup.md`.
- [x] Restore the `moonlight-common-c` submodule.
- [x] Run Gradle tests and a debug assemble command once prerequisites are present.

Notes:

- The Android command line tools archive downloaded from Google's official URL matched the checksum displayed on the Android Studio page as SHA-1. The page labels the column SHA-256, but the displayed Windows checksum is 40 hex characters and matches `Get-FileHash -Algorithm SHA1`.
- `adb` needs `ANDROID_USER_HOME` pointed to repo-local `.android` in this sandbox; `scripts/android-env.ps1` handles that.
- Restored submodules:
  - `app/src/main/jni/moonlight-core/moonlight-common-c` at `c999436858471dfefa7617af3b7dc03ec1644ce4`
  - `app/src/main/jni/moonlight-core/moonlight-common-c/enet` at `115a10baa1d7f291ff5b870765610fd3b4a6e43c`
- `gradlew test` passed on 2026-05-06 after updating stale Robolectric tests:
  - `LayoutInflationTest` now inflates with `R.style.AppTheme` so Material components are themed correctly.
  - Profile navigation tests now treat `profilesButton` as a generic `View` because the layout uses `ExtendedFloatingActionButton`, not `ImageButton`.
  - Startup tests call `ArtemisApplication.onCreate()` on the attached application context instead of manually constructing an unattached `Application`.
- `gradlew assembleNonRoot_gameDebug` passed on 2026-05-06.
- Generated debug APKs:
  - `app/build/outputs/apk/nonRoot_game/debug/app-nonRoot_game-arm64-v8a-debug.apk`
  - `app/build/outputs/apk/nonRoot_game/debug/app-nonRoot_game-armeabi-v7a-debug.apk`
  - `app/build/outputs/apk/nonRoot_game/debug/app-nonRoot_game-x86-debug.apk`
  - `app/build/outputs/apk/nonRoot_game/debug/app-nonRoot_game-x86_64-debug.apk`

Milestone 0 status: complete for local test/build readiness. Next milestone should start a Quest-specific product flavor/manifest without changing the streaming core.

### 2026-05-06 - Milestone 1 Started

Goal: run the current app on Quest as a Horizon OS panel and validate the existing stream path without changing the streaming core.

Completed local implementation:

- [x] Added a `quest` product flavor in `app/build.gradle`.
- [x] Set Quest application ID to `dev.moonlightvr.quest`.
- [x] Kept Quest on the non-root native build path with `PRODUCT_FLAVOR=nonRoot`.
- [x] Added `BuildConfig.QUEST_BUILD` so code can gate Quest-only UI/defaults.
- [x] Added `app/src/quest/AndroidManifest.xml`.
- [x] Removed TV EPG permissions from the Quest merged manifest.
- [x] Removed Quest launch exposure through Leanback/OUYA categories by replacing the `PcView` launcher activity in the Quest manifest overlay.
- [x] Added Horizon panel default sizes for `PcView`, `AppView`, `StreamSettings`, `Game`, and `DebugInfoActivity`.
- [x] Added Quest streaming presets:
  - Balanced: 1080p, 90 FPS, 45 Mbps, auto codec, latency pacing.
  - Quality: 1440p, 90 FPS, 70 Mbps, auto codec, balanced pacing.
  - Low latency: 1080p, 120 FPS, 35 Mbps, auto codec, latency pacing, low-delay frame balance.
  - Desktop clarity: 1440p, 60 FPS, 50 Mbps, auto codec, balanced pacing.
- [x] Added a Quest-only settings category that is hidden from non-Quest builds.
- [x] Extended `DebugInfoActivity` into the first Quest diagnostics surface with OS/build, ABI, network, Wi-Fi, decoder, and gamepad details.
- [x] Added unit coverage for Quest preset selection/application.
- [x] Added `docs/quest-panel-build.md`.
- [x] Updated `.gitignore` for repo-local Android SDK/toolchain setup artifacts.

Verification:

- `gradlew test` passed on 2026-05-06.
- `gradlew assembleQuestDebug` passed on 2026-05-06.
- Combined verification `gradlew test assembleQuestDebug` passed on 2026-05-06.
- Quest merged manifest check confirmed:
  - package is `dev.moonlightvr.quest.noirdebug` for debug builds.
  - Quest panel `<layout>` entries are present.
  - `READ_EPG_DATA`, `WRITE_EPG_DATA`, `android.software.leanback`, `LEANBACK_LAUNCHER`, and `tv.ouya.intent.category.APP` are absent.
- Generated Quest debug APKs:
  - `app/build/outputs/apk/quest/debug/app-quest-arm64-v8a-debug.apk`
  - `app/build/outputs/apk/quest/debug/app-quest-armeabi-v7a-debug.apk`
  - `app/build/outputs/apk/quest/debug/app-quest-x86-debug.apk`
  - `app/build/outputs/apk/quest/debug/app-quest-x86_64-debug.apk`

Local environment repair during verification:

- The repo-local SDK/NDK install had malformed package folders from prior setup:
  - missing `source.properties` in the NDK.
  - missing `android.jar` in the canonical `platforms/android-36` folder.
  - missing build-tools binaries in the canonical `build-tools/35.0.0` folder.
  - missing `ndk-build.cmd`.
- Gradle was allowed to install complete repo-local SDK/NDK packages after SDK license hashes were added locally.
- Broken package folders were moved aside under `.android-sdk`; these are ignored by Git.

Milestone 1 status: local implementation/build slice complete. Remaining acceptance work requires headset hardware: sideload the `arm64-v8a` APK on Quest, pair with Sunshine/Apollo, test 1080p/60, 1080p/90, and 1440p/90 where supported, verify physical gamepad streaming input, and verify Touch controller panel usability.

### 2026-05-06 - Milestone 2 Started

Goal: make the stream core reusable by Android, Spatial SDK, or OpenXR frontends without changing the current Android stream behavior.

Completed local implementation:

- [x] Added `StreamLaunchParams` to parse and validate `Game` launch intent extras, create the launch `NvApp`, decode the server certificate, and build the optional `NvHTTP` quit-session helper.
- [x] Added `StreamConfigurationFactory` so `Game` delegates `StreamConfiguration` construction and frame-rate selection instead of owning that setup inline.
- [x] Added `StreamingSession` to own the active `NvConnection`, `AndroidAudioRenderer`, `MediaCodecDecoderRenderer` render-target assignment, and start/stop lifecycle state.
- [x] Added `VideoSink` with readiness/destroy callbacks, preferred size, preferred refresh metadata, and `Surface` access.
- [x] Added `AndroidSurfaceVideoSink` for the current `StreamContainer` surface path.
- [x] Extended `StreamContainer` with a surface-destroy callback for sink lifecycle propagation.
- [x] Added `HostInputMapper` and routed evdev mouse/scroll/keyboard forwarding through it.
- [x] Updated `Game` to delegate launch parsing, config generation, session start/stop, video sink readiness, and the first input-mapper path while preserving existing activity UI, overlays, decoder setup, controller handling, and connection callbacks.
- [x] Added unit coverage for launch params, frame-rate/config factory behavior, and streaming session state transitions.

Verification:

- `gradlew test` passed on 2026-05-06.
- `gradlew assembleQuestDebug` passed on 2026-05-06.

Milestone 2 status: local refactor/build slice complete. Hardware acceptance still requires confirming an actual stream on Quest and a normal Android device because this milestone intentionally preserves the existing MediaCodec/Surface path while changing ownership boundaries.

## Executive Summary

This project is feasible, and the fork is a strong starting point for the streaming, host discovery, codec negotiation, input forwarding, and performance overlay layers. The main work is not the streaming core. The main work is replacing a phone/tablet/TV Android UX with a Quest-native interaction model and deciding how deeply to integrate with Horizon OS.

The key clarification: a "native Horizon OS app" is still distributed as an Android package. The real choice is between:

1. A Quest-compatible Android APK that runs as a 2D panel in Horizon OS.
2. A Meta Spatial SDK hybrid app with Android panels plus an immersive OpenXR activity.
3. A lower-level native OpenXR app, likely C/C++ plus Android/Java integration, that owns the VR renderer.

Recommended path:

1. Start with a Quest-specific APK/product flavor to validate the current app on headset, get a clean package name, fix unsupported permissions/dependencies, tune panel sizing, and test Quest input and MediaCodec behavior.
2. Build a Spatial SDK hybrid version as the main product direction: existing Android screens become panels, while streaming runs inside an immersive theater activity.
3. Only move to a custom native OpenXR renderer if Spatial SDK cannot expose the media texture/control needed for low-latency streaming.

Do not rewrite the streaming protocol or Moonlight core first. First, separate the current `Game` activity into a reusable streaming session plus pluggable video/input/UI adapters. That refactor lets the same stream core feed either the existing Android `SurfaceView`, a Spatial SDK scene, or a native OpenXR texture pipeline.

## Current Project Snapshot

The repo is a single-module Android app:

- Gradle/Android: `com.android.application`, AGP `8.13.0`, Gradle wrapper `8.13`, `compileSdk 36`, `targetSdk 34`, `minSdk 21`, Java 11, NDK `27.0.12077973`.
- Product flavors today are `root`, `nonRoot_game`, and `quest`, all under the `root` dimension. Quest uses the non-root native path and application ID `dev.moonlightvr.quest`.
- Native code is built with `ndkBuild` from `app/src/main/jni/Android.mk`.
- The `moonlight-common-c` submodule path is configured in `.gitmodules` and was restored locally during Milestone 0. A fresh checkout still needs submodules initialized before native builds.
- License is GPLv3 per `LICENSE.txt`. Any distributed fork must respect source distribution obligations and should use its own package name/application ID.

Important local files:

- `app/build.gradle`: Android build config, dependencies, ABI splits, product flavors.
- `app/src/main/AndroidManifest.xml`: Android permissions, app activities, TV/leanback support, `Game` stream activity.
- `app/src/main/java/com/limelight/PcView.java`: PC discovery and pairing entry screen.
- `app/src/main/java/com/limelight/AppView.java`: host app/game selection.
- `app/src/main/java/com/limelight/Game.java`: monolithic streaming activity, input handling, surface lifecycle, connection start/stop, overlays, PiP, game menu, controller routing.
- `app/src/main/java/com/limelight/ui/StreamContainer.java`: selects 2D `SurfaceView` or 3D `GLSurfaceView`.
- `app/src/main/java/com/limelight/utils/Stereo3DRenderer.java`: AI-assisted side-by-side 3D renderer using GL ES, `SurfaceTexture`, TFLite LiteRT, OpenCV, GPU/NNAPI fallback.
- `app/src/main/java/com/limelight/binding/video/MediaCodecDecoderRenderer.java`: hardware decode through Android `MediaCodec`, configured against a render `Surface`.
- `app/src/main/java/com/limelight/nvstream/NvConnection.java`: Moonlight session setup, Sunshine/GFE HTTP handshake, stream negotiation, `MoonBridge` bridge start, input send APIs.
- `app/src/main/java/com/limelight/nvstream/jni/MoonBridge.java`: JNI bridge to `moonlight-core`.

The app already supports:

- Sunshine/GFE app discovery and pairing.
- H.264, HEVC, AV1 negotiation where device/host support exists.
- Up to 4K/120 FPS settings in UI.
- Stereo/5.1/7.1 audio configuration.
- HDR negotiation and MediaCodec HDR metadata.
- Virtual display flow for supported hosts.
- Gamepad, keyboard, mouse, touch, stylus, virtual controller, and some motion sensor forwarding.
- Performance overlay and latency/decode stats.
- AI SBS 3D render mode.

The app does not currently have:

- Meta Spatial SDK or OpenXR integration.
- VR compositor layer rendering.
- Quest controller/hand tracking mapping for gameplay in immersive mode.
- A clean separation between streaming session, render target, and Android activity UI.

## How The Current Stream Pipeline Works

The current streaming pipeline is broadly:

1. `PcView` finds or manually pairs a host.
2. `AppView` lists host apps and calls `ServerHelper.doStart()`.
3. `ServerHelper.createStartIntent()` launches `Game` with host/app/session extras.
4. `Game.onCreate()` reads preferences, selects resolution/fps/codec/HDR, creates `MediaCodecDecoderRenderer`, creates `StreamConfiguration`, creates `NvConnection`, and initializes input handlers.
5. `StreamContainer` owns the active rendering surface.
6. When a surface becomes available, `Game` calls `decoderRenderer.setRenderTarget(streamContainer.getSurface())` and starts `conn.start(new AndroidAudioRenderer(...), decoderRenderer, Game.this)`.
7. `NvConnection` negotiates with the host, then calls `MoonBridge.startConnection(...)`.
8. Native Moonlight callbacks call back into `MediaCodecDecoderRenderer.submitDecodeUnit(...)`.
9. `MediaCodecDecoderRenderer` queues compressed frames into `MediaCodec`, which renders to the configured `Surface`.

That surface abstraction is the strategic seam for VR. If we can make the VR renderer provide a `Surface` or texture-backed sink that `MediaCodec` can render into, the rest of the Moonlight connection path can remain mostly intact.

In 2D mode, `StreamContainer` uses `SurfaceView`.

In AI 3D mode, `StreamContainer` creates a `GLSurfaceView` and `Stereo3DRenderer`; `Stereo3DRenderer` creates a `SurfaceTexture`, wraps it in a `Surface`, receives decoded video there, runs optional depth inference, and draws SBS eye regions inside one Android GL view. This proves the project already knows how to redirect MediaCodec output into a GL texture. It does not yet prove VR compositor integration, lens-correct stereo, OpenXR swapchain rendering, or head/controller input integration.

## Platform Options

### Option A: Quest-Compatible Android APK / 2D Panel

Feasibility: high.

This is the fastest validation path. Meta explicitly supports bringing existing Android apps to Horizon OS, and recommends using product flavors for device-specific APKs. The current app is already mostly ordinary Android Java, AppCompat, XML layouts, `MediaCodec`, and NDK. That maps well to a Quest panel app.

What this gives:

- Existing code can remain mostly intact.
- Existing `SurfaceView`/`MediaCodec` path is likely the easiest thing to test.
- PC discovery, pairing, settings, app grid, game launch, decoding, and input code can run before any immersive rewrite.
- Quest panels can be given default dimensions through manifest `<layout>` settings.
- Multiple panel activities can be used without integrating a full spatial SDK.

What this does not give:

- A VR-first streaming environment.
- A world-locked theater screen with recentering, screen distance, curvature, and hand/controller affordances.
- Full raw Quest controller input. Meta documents targeted input for 2D apps; joystick handling has limitations and is tied to hovering over the panel.
- OpenXR predicted-display timing or compositor layer control.

Recommended use:

- Build this first as a proof of viability and as a debugging tool.
- Keep it as a fallback mode and settings/admin UI even after an immersive mode exists.
- Do not treat it as the final UX unless the goal is simply "Moonlight in a large flat Quest panel."

Implementation notes:

- Add a `quest` product flavor or a new `platform` flavor dimension rather than mutating the existing phone/TV defaults.
- Use a unique application ID such as `dev.<owner>.moonlightvr` or another final brand package.
- Remove or isolate TV-only permissions/features that Horizon OS may reject, especially `com.android.providers.tv.permission.READ_EPG_DATA` and `WRITE_EPG_DATA`.
- Add Quest/Horizon manifests under `app/src/quest/AndroidManifest.xml`.
- Add default `<layout android:defaultWidth="..." android:defaultHeight="..." />` for panel activities.
- Ensure all important navigation has visible back/close controls.
- Make hit targets at least 48dp, preferably 60dp for primary controls.
- Add hover/focus states.
- Avoid pure white and pure black in main panels.

### Option B: Meta Spatial SDK Hybrid App

Feasibility: medium-high, recommended long-term path.

Meta Spatial SDK is built for Android developers who want immersive Horizon OS apps without adopting a full game engine. It supports Kotlin, Android tooling, panels, scenes, passthrough/MR capabilities, and hybrid apps that move between 2D panel activities and OpenXR-based immersive activities.

This path fits this repo well because the streaming stack is Android/Java/NDK, not Unity/Unreal. It allows:

- Existing Android screens as panels for pairing, PC/app selection, settings, diagnostics, and profile management.
- An immersive activity for streaming, with a world-locked screen and VR-first controls.
- Incremental migration instead of a total rewrite.
- Use of Meta tooling such as Spatial Editor and hot reload for scene work.

Core challenge:

- Confirm the best way to present a low-latency `MediaCodec` output surface inside a Spatial SDK scene. The current app can render decoder output into a `SurfaceTexture`, but Spatial SDK integration will need a research spike around external textures, custom shaders, media surfaces, or hybrid panel embedding.

Recommended use:

- Make this the primary product architecture if Spatial SDK can support the video texture latency requirements.
- Keep the existing Android activities for setup.
- Build a new immersive `QuestStreamActivity` that owns the VR scene and delegates streaming to a shared `StreamingSession`.

Implementation notes:

- Add Kotlin support and a `questSpatial` or `quest` flavor.
- Add Spatial SDK dependencies only to the Quest flavor.
- Introduce `StreamingSession` and `VideoSink` interfaces before building the immersive activity.
- Make `Game` a legacy Android shell over the shared session rather than the owner of all streaming logic.
- Use Android panels for setup and an immersive OpenXR activity for playback.
- Use exclusive mode first: panel setup launches immersive stream, then panel closes. Cooperative overlay panels can come later.

### Option C: Native OpenXR App

Feasibility: medium, highest control, highest cost.

Meta OpenXR SDK provides native Quest samples and APIs for all Quest devices. This is the most direct path if the final product must control the VR compositor, swapchains, input actions, foveation, passthrough layers, and exact frame timing.

What this gives:

- Full immersive VR control.
- Direct OpenXR action mappings for Touch controllers and hands.
- Custom rendering of the stream as a quad/cylinder/theater surface.
- Better path to low-level performance tuning.
- Better path to composition layers, passthrough, hand tracking, and foveation where relevant.

What this costs:

- Much larger rewrite.
- The current Android activity/view UI cannot simply be reused as-is.
- Need to bridge Java/Kotlin session logic and native OpenXR rendering, or move more code into C/C++.
- Need robust lifecycle handling across Android activity, OpenXR session, EGL context, MediaCodec surface, and Moonlight native threads.
- Need a complete VR input/action layer.

Recommended use:

- Treat as phase 3, not phase 1.
- Use it if Spatial SDK cannot deliver stable low-latency decoded video in an immersive scene.
- Prototype by rendering MediaCodec output from `SurfaceTexture`/external OES texture into OpenXR swapchains.

## Recommended Architecture

The most important engineering move is to break apart `Game.java`. It is currently the stream activity, lifecycle owner, render target owner, input router, UI overlay owner, settings reader, and connection listener. That is normal for a mature Android app, but it will slow any VR port.

Introduce these concepts:

```text
StreamingSession
  Owns NvConnection, StreamConfiguration, decoder/audio renderer lifecycle.

VideoSink
  Provides a Surface for MediaCodec and reports size/availability/destruction.

InputSink
  Converts local input into MoonBridge/NvConnection events.

StreamUiController
  Shows connection state, errors, overlays, menus, and stats.

LegacyGameActivity
  Existing Android SurfaceView implementation.

QuestPanelActivity
  2D Horizon panel implementation.

QuestImmersiveActivity
  Spatial SDK or native OpenXR implementation.
```

First refactor target:

- Extract setup logic from `Game.onCreate()` into a `StreamingSessionFactory`.
- Extract `StreamConfiguration` construction into a dedicated builder/helper.
- Extract render surface handling behind `VideoSink`.
- Keep existing behavior intact by making `Game` use the new helpers.

That refactor is valuable even before Quest work because it reduces risk around the stream lifecycle.

## UX Changes Needed For VR First

### Home And Host Selection

Current UX is a phone/tablet/TV grid. For Quest, the first screen should feel like a control room, not a mobile launcher.

Recommended UX:

- Main panel: available PCs, connection quality, current host state, active session state.
- Secondary panel or drawer: profiles, settings, diagnostics.
- Pairing flow: large PIN/code entry, clear host instructions, no tiny dialogs.
- App selection: big readable tiles, but not a TV clone. Prioritize recent apps, resume session, desktop, and favorites.
- Long-press context menus should become explicit action menus; context menus are poor for hand/controller UX.

### Streaming Environment

The stream view should become a place:

- World-locked virtual screen in front of the user.
- Recenter button always available.
- Screen size, distance, height, and curvature controls.
- Optional passthrough environment for mixed reality.
- Optional dark theater environment for VR.
- Quick actions: disconnect, quit host app, keyboard, mouse mode, controller mode, resize, recenter, performance HUD.
- No head-locked fullscreen video by default. Head-locked content is uncomfortable for long sessions.

### Input Model

Input is the hardest UX/system issue.

Separate input into two layers:

1. Quest UI input: hands/controllers/rays/pinch/select/back interact with panels and overlays.
2. Host stream input: gamepad, mouse, keyboard, touch, and motion events are sent to the PC.

Do not let every Quest controller action always go to the host. The user needs a reliable way to open the VR menu, recenter, and recover from bad mappings.

Recommended defaults:

- Physical Bluetooth gamepad: best gameplay path; forward as normal gamepad.
- Quest Touch controllers in immersive mode: map through OpenXR actions to virtual Xbox-style gamepad where possible.
- Hands: UI only by default; optional simple mouse/pointer mode for desktop use.
- Mouse/keyboard: support Bluetooth peripherals; use existing keyboard/mouse paths.
- Virtual keyboard: make it a VR overlay/panel, not a mobile IME-dependent flow.
- Desktop streaming: provide pointer ray mode, trackpad mode, and absolute pointer mode.

2D panel limitation:

- In panel mode, Horizon OS sends targeted input to the app when hovering/selecting a panel. Meta documents limitations for joystick/raw controller handling in 2D apps, so serious gameplay mapping should be done in immersive/OpenXR mode.

### Settings

Quest users should not face the full Android settings tree first.

Recommended Quest presets:

- Balanced: 1080p or 1440p, 90 FPS, HEVC/H.264 auto, conservative bitrate.
- Quality: 1440p/4K, 90 FPS, HEVC/AV1 where supported, higher bitrate.
- Low latency: lower resolution, 90/120 FPS where stable, lower frame queue.
- Desktop clarity: higher resolution, lower motion assumptions, pointer optimized.

Keep advanced settings behind an "Advanced" section.

### 2D, SBS 3D, And AI 3D

The existing AI SBS 3D mode is interesting but risky for v1:

- It adds GPU/NNAPI/OpenCV/TFLite load on a thermally constrained headset.
- It increases render complexity and latency risk.
- It may create eye strain if depth/convergence is wrong.
- It is not the same as proper stereoscopic VR rendering.

Recommendation:

- Ship VR v1 as a high-quality 2D virtual screen.
- Keep SBS 3D as an experimental toggle.
- Add explicit comfort warnings and easy reset for parallax/convergence.
- Avoid enabling AI 3D by default on Quest until measured on hardware.

## Quest / Horizon OS Intricacies

### Android Compatibility

Meta documents that adapting Android apps to VR may require compatibility changes around design, dependencies, permissions, and streaming media behavior.

Specific implications for this project:

- No Google Mobile Services dependency is present in the current Gradle file, which is good.
- The app has Android TV/leanback heritage. Quest flavor should strip or isolate TV-specific assumptions.
- Manifest permissions should be reviewed against Horizon unsupported permissions. The TV EPG permissions are suspicious for Quest and likely unnecessary.
- App orientation/screen assumptions should become panel sizing and immersive screen sizing.

### Design And Input

Meta design requirements call out larger hit targets, no universal system back button for all input methods, hover/focus states, contrast, and typography for spatial viewing.

Local impact:

- Current XML layouts use many 70dp icon buttons, which are mostly okay in size but need hover/focus, labels/tooltips, and better spatial layout.
- Dialog-heavy flows need bigger controls and clearer escape paths.
- Back navigation must be visible in UI, not dependent on Android back.
- Long lists/preferences must be readable at panel distance.

### Streaming Media Quality

Meta's 2D streaming guidance expects high quality on theater-sized screens, Smart-TV-like bitrate profiles, landscape panels, codec probing, and support for hand/controllerless input. This aligns with Moonlight's high-bitrate local network use, but the app should expose Quest-first presets and warnings.

Local impact:

- Continue using `MediaCodecInfo` probing as this app already does.
- Prefer hardware decode only.
- Use measured Quest device capability rather than hardcoding AV1/HEVC assumptions.
- Make network diagnostics first-class.
- Optimize for local Wi-Fi and low jitter.

### Performance And Latency

VR streaming has two latency budgets:

- Host-to-client streaming latency.
- Client-to-display VR compositor latency.

Current app already works hard on Android frame pacing, `Surface.setFrameRate()`, decoder queueing, direct submit, and latency overlays. In immersive VR, that work must be reconciled with OpenXR frame timing and compositor behavior.

Risks:

- Extra texture copies from MediaCodec to GL to VR scene.
- Frame timing mismatch between stream FPS and headset refresh.
- Thermal throttling from AI depth conversion or high bitrate decode.
- Audio/video sync drift if render scheduling changes.
- Motion-to-photon discomfort if virtual screen/overlays move incorrectly.

Measure on device early with:

- End-to-end client latency from existing overlay.
- Decoder latency.
- Dropped frames.
- Wi-Fi RTT/jitter.
- Headset refresh rate.
- Thermal throttling over 30+ minute sessions.

### Passthrough / Mixed Reality

Quest 3 and Quest 3S support passthrough camera APIs for certain use cases on recent Horizon OS versions, with permissions and privacy implications. For this project, passthrough is more likely needed as a background/environment mode than for computer vision. Do not request camera permissions unless the app truly needs camera images. Use Spatial SDK/OpenXR passthrough/environment features for MR presentation where possible.

### Store, Branding, And Licensing

Important:

- Do not ship with Moonlight's official application ID.
- Use a new package name and app name.
- GPLv3 obligations apply if distributing binaries.
- Meta store review may scrutinize streaming, input, network permissions, privacy, and third-party host setup.
- Sunshine/Apollo compatibility should be documented without implying affiliation.

## Roadmap For Future Codex Work

### Milestone 0: Baseline Build And Repository Hygiene

Goal: make the current fork buildable and testable.

Tasks:

- Initialize and pin `app/src/main/jni/moonlight-core/moonlight-common-c`.
- Build `nonRoot_gameDebug` and run unit tests.
- Record Android Studio/NDK/JDK requirements in `README.md` or `docs/build.md`.
- Choose final package/application ID for the VR fork.
- Keep root flavor out of Quest builds.
- Verify current unit tests under `app/src/test`.

Acceptance:

- `./gradlew test` or the Windows equivalent passes.
- `./gradlew assembleNonRoot_gameDebug` or selected variant builds.
- No missing submodule/native source failures.

### Milestone 1: Quest Panel APK

Goal: run the current app on Quest as a panel and stream successfully.

Tasks:

- Add Quest flavor or platform dimension.
- Add `app/src/quest/AndroidManifest.xml`.
- Remove Quest-incompatible permissions/features from the Quest merged manifest.
- Add panel default sizes to `PcView`, `AppView`, `StreamSettings`, and maybe `Game`.
- Add explicit back/close buttons in major screens.
- Increase/verify hit targets and hover/focus states.
- Add Quest streaming presets.
- Add a "Quest diagnostics" screen: decoder list, codec support, network RTT, packet loss, Wi-Fi band if available, headset OS/build info.

Acceptance:

- Sideloaded Quest APK launches from unknown sources/dev mode.
- Pairing with Sunshine works.
- 1080p/60, 1080p/90, and 1440p/90 are tested if host/network supports them.
- Physical gamepad works.
- Touch controller panel interactions are usable.
- No required Google Mobile Services dependency.

### Milestone 2: Streaming Session Refactor

Goal: make the stream core reusable by Android, Spatial SDK, or OpenXR frontends.

Tasks:

- Extract `StreamLaunchParams` from `Game` intent extras.
- Extract `StreamConfigurationFactory`.
- Create `StreamingSession` owning `NvConnection`, `AndroidAudioRenderer`, `MediaCodecDecoderRenderer`, and lifecycle.
- Create `VideoSink` with `Surface getSurface()`, readiness callbacks, preferred size/refresh metadata, and destroy handling.
- Create `AndroidSurfaceVideoSink` for current `StreamContainer`.
- Move input routing behind `HostInputMapper`.
- Keep existing `Game` behavior working.

Acceptance:

- Existing Android stream path still works.
- Unit tests cover config generation, launch params, and lifecycle state transitions where possible.
- `Game.java` shrinks meaningfully or delegates most setup logic.

### Milestone 3: Spatial SDK Prototype

Goal: prove decoded video can appear in an immersive Spatial SDK scene with acceptable latency.

Tasks:

- Add Kotlin and Spatial SDK dependency in Quest flavor.
- Create minimal `QuestImmersiveActivity`.
- Render a static scene with a world-locked screen.
- Research and implement a `SpatialVideoSink` that exposes a `Surface` for `MediaCodec` and displays it in the scene.
- Add recenter, resize, and disconnect controls.
- Route basic controller actions to UI, not host gameplay yet.

Acceptance:

- Same Moonlight session can render to immersive screen.
- Latency increase versus panel mode is measured.
- Stream can run for 30 minutes without lifecycle/surface crash.

### Milestone 4: Immersive Input

Goal: make the immersive stream playable.

Tasks:

- Map Touch controllers through Spatial/OpenXR input to host gamepad events.
- Define a reserved system/menu chord that never goes to host.
- Support physical gamepad as preferred path.
- Add pointer/mouse mode for desktop.
- Add VR keyboard overlay/panel.
- Add per-game input profiles.

Acceptance:

- User can play a controller game without a separate gamepad, with documented limitations.
- User can use desktop mode with ray/pointer or trackpad behavior.
- User can always open VR menu and disconnect.

### Milestone 5: Production UX

Goal: make it feel like a VR product.

Tasks:

- Build the "stream space" home.
- Add panel setup + immersive playback flow.
- Add theater/MR environment options.
- Add screen curvature/size/distance/height.
- Add comfort defaults and reset.
- Add onboarding for Sunshine pairing and Quest input modes.
- Add crash-safe recovery and last-session resume.

Acceptance:

- New user can pair, select app, stream, open menu, and disconnect without Android-native knowledge.
- All major controls are hand/controller accessible.
- Performance overlay can be toggled in VR.

## Risk Register

High risks:

- `Game.java` coupling makes direct VR work brittle.
- MediaCodec-to-Spatial/OpenXR texture path may add copies or lifecycle complexity.
- Quest controller input in 2D panel mode is not enough for serious gameplay.
- AI SBS 3D may be too expensive on headset.
- Submodule is currently missing in working tree.

Medium risks:

- Horizon OS permission/dependency review may reject legacy Android TV assumptions.
- Streaming at high resolution/bitrate may overheat or drain battery quickly.
- HDR behavior on Quest needs hardware verification.
- AV1/HEVC assumptions must be probed, not hardcoded.
- Store review may require clearer privacy/network explanations.

Low risks:

- Core Sunshine/GFE protocol viability; Moonlight already solves this.
- Basic Android build structure; repo is modern enough.
- Basic panel app feasibility; Meta supports Android apps on Horizon OS.

## Open Technical Questions

- Can Spatial SDK display a `MediaCodec`-backed external texture with low enough latency, or do we need native OpenXR?
- Should decoded video be rendered as a normal mesh texture, compositor layer, or media layer where available?
- Can we preserve current `MediaCodecDecoderRenderer` scheduling, or does it need an OpenXR-aware renderer?
- What exact Quest controller mapping feels acceptable for Xbox-style games?
- Should AI 3D be removed from default Quest builds, left experimental, or moved behind a compile-time flag?
- What is the minimum host setup story: Sunshine only, Apollo-enhanced virtual display, or legacy GFE support?
- Should the product aim for Meta Store distribution or sideload/App Lab first?

## Concrete Recommendation

Build the first version as:

- A Quest-specific non-root Android APK for validation and fallback.
- A refactored shared streaming core.
- A Spatial SDK hybrid app as the target architecture.

Do not begin with a total C++ OpenXR rewrite. The current project has too much valuable Android streaming infrastructure to throw away. The best engineering strategy is to preserve the Moonlight session and MediaCodec strengths while gradually replacing the user-facing shell and render target.

The first Codex implementation task should be:

1. Initialize/restore the native submodule and verify baseline build.
2. Add a Quest product flavor with a unique app ID and clean manifest.
3. Add a small `docs/build.md` and Quest build command.
4. Run tests.

The second task should be:

1. Extract `StreamLaunchParams`.
2. Extract `StreamConfigurationFactory`.
3. Introduce `VideoSink`.
4. Keep existing `Game` working.

That sequence creates momentum without overcommitting to an immersive renderer before the headset/runtime facts are known.

## Source Notes

Meta/Horizon OS sources checked on 2026-05-06:

- Meta Android apps overview: https://developers.meta.com/horizon/develop/android-apps/
- Meta product flavors guidance: https://developers.meta.com/horizon/documentation/android-apps/product-flavors/
- Meta Android app compatibility overview: https://developers.meta.com/horizon/documentation/android-apps/making-apps-compatible-overview/
- Meta Android design requirements: https://developers.meta.com/horizon/documentation/android-apps/design-requirements/
- Meta Android input guidance: https://developers.meta.com/horizon/documentation/android-apps/input/
- Meta media requirements for streaming apps: https://developers.meta.com/horizon/documentation/android-apps/media-requirements/
- Meta panel sizing: https://developers.meta.com/horizon/documentation/android-apps/panel-sizing/
- Meta Spatial SDK overview: https://developers.meta.com/horizon/documentation/spatial-sdk/spatial-sdk-explainer/
- Meta Spatial SDK hybrid apps overview: https://developers.meta.com/horizon/documentation/spatial-sdk/hybrid-apps-overview/
- Meta Spatial SDK hot reload: https://developers.meta.com/horizon/documentation/spatial-sdk/spatial-sdk-hot-reload/
- Meta passthrough camera overview: https://developers.meta.com/horizon/documentation/unity/unity-pca-overview/
- Meta OpenXR SDK samples: https://github.com/meta-quest/Meta-OpenXR-SDK
