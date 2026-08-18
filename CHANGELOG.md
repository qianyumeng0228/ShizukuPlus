# Changelog

All notable changes to ShizukuPlus are documented here.

## [Unreleased / Build r2248+]

### 🐛 Bug Fixes

#### Server / Service
- Fixed `attachApplication` (binder code 17) dead-code regression that caused ALL API v13+ clients to fail. Affected: Morphe, InstallerX Revived, Droid-ify, ObtainX, Obtainium, MT Manager APK install, Termux `rish`. ([#406](https://github.com/thejaustin/ShizukuPlus/issues/406), [#394](https://github.com/thejaustin/ShizukuPlus/issues/394), [#392](https://github.com/thejaustin/ShizukuPlus/issues/392), [#391](https://github.com/thejaustin/ShizukuPlus/issues/391), [#387](https://github.com/thejaustin/ShizukuPlus/issues/387), [#386](https://github.com/thejaustin/ShizukuPlus/issues/386))
- Fixed shell consent (`rish`/`adb`) not persisting after `Allow always` tap. Callers are now identified by UID via `Os.getuid()` fallback even when PM lookup fails. ([#391](https://github.com/thejaustin/ShizukuPlus/issues/391), [#398](https://github.com/thejaustin/ShizukuPlus/issues/398))
- Fixed SU bridge self-test failing due to writing to an already-open file descriptor. ([#402](https://github.com/thejaustin/ShizukuPlus/issues/402))
- Fixed Live Activity notification reappearing after disabling the toggle. ([#400](https://github.com/thejaustin/ShizukuPlus/issues/400))
- SU bridge: removed the `id()`/`whoami()` shell function mocks from the dynamic Magisk-mode injection header — they were shadowing the real coreutils binaries for every `sh -c` call routed through the bridge, not just root-check probes.
- Fixed `newProcess()` dropping the entire boot environment (`BOOTCLASSPATH`, `ANDROID_DATA`, `ANDROID_ROOT`, etc.) when Magisk mocking is enabled and the caller passes a `null` env — it now seeds from `System.getenv()` before appending the Magisk vars, instead of replacing the env outright. Fixes spawned `app_process` children dying instantly with `ANDROID_DATA environment variable unset`. ([#410](https://github.com/thejaustin/ShizukuPlus/issues/410))

#### Manager App (UI)
- **Eliminated black screen flash** on every theme/accent/icon/blur change — settings screen now recomposes in-place without `Activity.recreate()`. ([#407](https://github.com/thejaustin/ShizukuPlus/issues/407) adjacent)
- Fixed Update channel: Dev/Beta channel now correctly fetches pre-releases and selects the right APK (Plus vs Drop-In). ([#407](https://github.com/thejaustin/ShizukuPlus/issues/407))
- Fixed bottom navigation bar overlapping: Feature Hub, Settings list, Activity Log, and App search bar. 
- Fixed Dhizuku Mode toggle not persisting state immediately on change.
- **Fixed "Shizuku is running" status icon** rendering as a garbled server-rack-and-checkmark mashup — reverted to the clean circle-checkmark glyph.
- **Fixed One-handed mode squeezing all content into the bottom-right corner** — the scale-down pivot was anchored at the corner instead of bottom-center, the actual Samsung OneUI behavior. Affected both Home and Settings.
- **Fixed update downloads appearing to hang instead of prompting to install** — the silent-install attempt (via root/Shizuku) had no timeout, so a stuck root prompt or dead Shizuku binder wedged the coroutine forever with no fallback to the system installer. Now falls back after 5s.
- Fixed update download progress notification vibrating/alerting on every 500ms progress tick instead of only once.
- **Manual crash reports were shipping with empty log sections** (`[#405](https://github.com/thejaustin/ShizukuPlus/issues/405)`, `[#397](https://github.com/thejaustin/ShizukuPlus/issues/397)`, `[#317](https://github.com/thejaustin/ShizukuPlus/issues/317)`) — the logcat tail filter (`*:S AndroidRuntime:E ...`) silenced everything by default and only allow-listed tags that didn't match real crash output. Now captures the last 300 unfiltered lines of the app's own logcat.
- Fixed `showStartAdbHome()` defaulting to `true` in Java while its XML preference (`show_start_adb_home`) defaults to `false` — first-run users saw the "Start ADB" home shortcut despite the setting screen showing it off.

### ✨ Enhancements

#### UI / UX  
- **Redesigned App Icon**: Added a "Plus" badge in the upper right of the app icon across the board. The Plus integrates directly into the original hexagon geometry.
- **Themed Icon Support**: Implemented a fully vector Material You monochrome icon, enabling the app icon (including the new Plus badge) to respond flawlessly to expressive theme color changes.
- **Authentic Samsung OneUI one-handed mode**: Content scales to 75% with bottom-center pivot anchor (matching Samsung's actual behavior) with smooth spring animation. Works on all devices.
- **Samsung OneUI Settings header**: LargeTopAppBar uses ExtraBold (W800) title at 28sp with −0.5sp letter-spacing and transparent container — matching OneUI 6/7 Settings exactly.
- **App search bar**: Modernized with Material 3 pill shape (28dp corner radius).
- Shell consent notification now shows the app package name instead of "cannot be identified" when PM lookup fails.

## [v13.6.0.r2239]

### Bug Fixes
- Power-save whitelist re-applied on each `bindApplication` retry.
- Watchdog scope clarified; RNDIS/Ethernet transport monitored.
- Binder delivery retried on frozen-app failure with actionable UI feedback.

## [v13.6.0.r2222]

### Bug Fixes  
- Shell caller now correctly identified; package name shown in consent notification.

## [v13.6.0 / r2215]

Initial public release of Shizuku+.
