# Changelog

All notable changes to ShizukuPlus are documented here.

## [Unreleased / Build r2248+]

### 🐛 Bug Fixes

#### Server / Service
- Fixed `attachApplication` (binder code 17) dead-code regression that caused ALL API v13+ clients to fail. Affected: Morphe, InstallerX Revived, Droid-ify, ObtainX, Obtainium, MT Manager APK install, Termux `rish`. ([#406](https://github.com/thejaustin/ShizukuPlus/issues/406), [#394](https://github.com/thejaustin/ShizukuPlus/issues/394), [#392](https://github.com/thejaustin/ShizukuPlus/issues/392), [#391](https://github.com/thejaustin/ShizukuPlus/issues/391), [#387](https://github.com/thejaustin/ShizukuPlus/issues/387), [#386](https://github.com/thejaustin/ShizukuPlus/issues/386))
- Fixed shell consent (`rish`/`adb`) not persisting after `Allow always` tap. Callers are now identified by UID via `Os.getuid()` fallback even when PM lookup fails. ([#391](https://github.com/thejaustin/ShizukuPlus/issues/391), [#398](https://github.com/thejaustin/ShizukuPlus/issues/398))
- Fixed SU bridge self-test failing due to writing to an already-open file descriptor. ([#402](https://github.com/thejaustin/ShizukuPlus/issues/402))
- Fixed Live Activity notification reappearing after disabling the toggle. ([#400](https://github.com/thejaustin/ShizukuPlus/issues/400))

#### Manager App (UI)
- **Eliminated black screen flash** on every theme/accent/icon/blur change — settings screen now recomposes in-place without `Activity.recreate()`. ([#407](https://github.com/thejaustin/ShizukuPlus/issues/407) adjacent)
- Fixed Update channel: Dev/Beta channel now correctly fetches pre-releases and selects the right APK (Plus vs Drop-In). ([#407](https://github.com/thejaustin/ShizukuPlus/issues/407))
- Fixed bottom navigation bar overlapping: Feature Hub, Settings list, Activity Log, and App search bar. 
- Fixed Dhizuku Mode toggle not persisting state immediately on change.

### ✨ Enhancements

#### UI / UX  
- **Authentic Samsung OneUI one-handed mode**: Content scales to 75% with bottom-right pivot anchor (matching Samsung's actual behavior) with smooth spring animation. Works on all devices.
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
