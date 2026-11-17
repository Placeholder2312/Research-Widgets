# Research Widgets

Nothing-style Android home-screen widgets for your research-paper feed. They are a companion to a customized fork of the [Aftergleam](https://github.com/jakobkreft/Aftergleam) reader: the widgets show today's papers, and a tap opens the paper in Aftergleam.

![Android](https://img.shields.io/badge/Android-8.0%2B-3DDC84?logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-100%25-7F52FF?logo=kotlin&logoColor=white)
![Dependencies](https://img.shields.io/badge/runtime%20deps-0-black)
![Network](https://img.shields.io/badge/network%20access-none-D71921)

## Contents

- [Widgets](#widgets)
- [Quick start](#quick-start)
- [Troubleshooting](#troubleshooting)
- [Architecture](#architecture)
- [Requirements](#requirements)
- [Signing](#signing-is-required-for-ipc)
- [Build, CI and tests](#build-ci-and-tests)
- [Aftergleam fork compatibility](#aftergleam-fork-compatibility)
- [Status](#status)

## Widgets

| Widget | Size | What you get |
| --- | --- | --- |
| **Research Paper Strip** | about 5×1 | One title fills the widget. Swipe up or down to move through papers; tap the current title to open it. |
| **Paper Strip (No Swipe)** | about 5×1 | A separate, plain widget. Tap the paper to open it; tap the right edge, the dots or the counter for the next paper. No gestures. |
| **Aftergleam tile** | about 2×2 | A static launcher tile that opens Aftergleam. |
| **Digest tile** | about 2×2 | Inverted tile with one big number. **Latest** counts today's digest (saved count underneath); **Downloaded** counts PDFs kept on the device (today's count underneath). Long-press to reconfigure. In **Downloaded** mode, tapping it opens Aftergleam's Library on the **Offline** chip. |

Both strips can show the **Recommended** digest or your **Saved** papers, chosen per widget, in a **Bottom** or **Side** look.

- **Bottom**: position dots under the title.
- **Side**: a slim vertical dot rail on the right edge, with a `03/12` counter under the title.

The current paper is the red dot. The order is reversed: the first paper is the last dot, and the red dot moves toward the first dot as you advance. With up to 7 papers each gets its own dot; beyond that the dots become a proportional meter (first paper = last dot, last paper = first dot) and the counter gives the exact position.

The look follows Nothing "Classic": light or dark from the system, monospace caps for labels, light sans-serif titles, red `#D71921` as the only accent and a 28dp card radius.

The app itself is a small control panel with connection status, add buttons, placed-widget counts, a refresh button and a version label.

### Which strip should I use?

The swipe strip is built on Android's `StackView`, and the No Swipe strip on ordinary `RemoteViews`. Launchers differ in how they deliver taps from collection widgets, so if taps on the swipe strip do nothing on your launcher, use **Paper Strip (No Swipe)**. Both are kept so you can compare them side by side.

## Quick start

1. Install the **Aftergleam fork** first ([Aftergleam-For-Widgets](https://github.com/Placeholder2312/Aftergleam-For-Widgets)), then open it once so it builds today's digest.
2. Install the newest **Research Widgets** APK from [GitHub Releases](https://github.com/Placeholder2312/Research-Widgets/releases). Both APKs must be signed with the same certificate (see [Signing](#signing-is-required-for-ipc)).
3. Open Research Widgets. The status dot turns red when it can read Aftergleam.
4. Tap an **Add** button, or add a widget from your launcher's widget picker.
5. Pick a feed and a look when the setup screen appears.

## Troubleshooting

| Symptom | What it means | What to do |
| --- | --- | --- |
| Widget says "Install the customized Aftergleam fork" | The provider isn't installed | Install the fork, not the official app |
| "Access denied. Check that both apps use the same signing certificate." | Signature mismatch | Install APKs built with the same `RESEARCH_WIDGETS_*` keystore |
| Digest tile says **UPDATE AFTERGLEAM FORK** | The installed fork predates the `v1/downloaded` feed | Install a fork build from `main` at or after the downloaded-feed change |
| "Today's digest is empty or has not been built yet" | Aftergleam hasn't built a digest | Open Aftergleam once |
| Tapping a widget does nothing | Either the launcher is not delivering the tap, or the hand-off to Aftergleam is blocked | See below |

**Telling a broken tap from a broken hand-off.** The app has a **Test: open newest paper** button that fires the same Aftergleam intent from the foreground.

- Test button works, **Aftergleam tile** works, a **strip** does not: the strip's tap path is the problem. Use **Paper Strip (No Swipe)**.
- Test button works, no widget works: the launcher-to-app hand-off is blocked. Capture logcat while tapping (`adb logcat -s ActivityTaskManager AppWidgetHostView`) and look for `Background activity launch blocked!`.
- Test button fails: the problem is in Aftergleam or signing, not the widgets.

## Architecture

The companion reads paper metadata from Aftergleam's signature-protected provider and refreshes its widgets when Aftergleam sends a protected data-changed broadcast. The swipe strip uses Android `StackView`; the No Swipe strip uses plain `RemoteViews`; the Digest tile shows local feed counts. A shared `FeedPass` prevents redundant feed queries during a single refresh broadcast.

The implementation is split by responsibility: `data/` contains the provider contract and client, `ui/` contains the app and widget setup screens, and `widget/` contains widget providers and their Android lifecycle/hand-off helpers. The data flow and component details are in [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md).

## Requirements

- Android 8.0+ (minSdk 26, targetSdk 37). The full-width title fix applies on Android 12+.
- The customized Aftergleam fork. The official app does not expose this API.
- Both APKs signed with the same certificate.

## Signing is required for IPC

Both apps must use the same locally controlled certificate to reach the signature-protected provider and paper-opening endpoint. Set the same values for both projects, outside Git:

- `RESEARCH_WIDGETS_KEYSTORE`
- `RESEARCH_WIDGETS_KEYSTORE_PASSWORD`
- `RESEARCH_WIDGETS_KEY_ALIAS`
- `RESEARCH_WIDGETS_KEY_PASSWORD`

This key signs debug and release builds when configured. Never commit keystores or passwords. Default debug keys from two separate projects differ and fail the signature check.

## Build, CI and tests

```bash
./gradlew testDebugUnitTest assembleDebug
```

The APK is written to `app/build/outputs/apk/debug/`. Prebuilt, signed debug APKs are published to [GitHub Releases](https://github.com/Placeholder2312/Research-Widgets/releases) as `research-widgets-v<version>-build-<n>`.

**CI.** GitHub Actions is manual-only (`workflow_dispatch`). It validates the shared keystore from repository secrets, builds, runs the unit tests, verifies the APK signature and publishes the APK to Releases.

**Tests.** Robolectric unit tests cover provider row mapping, graceful handling of a missing provider, per-widget feed and style storage (including widget restore), the Aftergleam v1 IPC contract, and `FeedPass` query deduplication across repeated reads in one refresh pass.

## Aftergleam fork compatibility

Paired with [`Placeholder2312/Aftergleam-For-Widgets`](https://github.com/Placeholder2312/Aftergleam-For-Widgets), branch `main`. The IPC contract is v1, protected by the `si.jakobkreft.aftergleam.permission.READ_RESEARCH_WIDGETS` signature permission.

| Piece | Value |
| --- | --- |
| Authority | `si.jakobkreft.aftergleam.researchwidgets` |
| Feeds | `v1/recommended`, `v1/saved`, `v1/downloaded` |
| Columns | the seven declared in `AftergleamContract` |
| Paper-open endpoint | `si.jakobkreft.aftergleam.integration.PaperOpenActivity` |
| Change broadcast | `si.jakobkreft.aftergleam.action.RESEARCH_WIDGETS_DATA_CHANGED` |

The `v1/downloaded` feed, used by the Digest tile's **Downloaded** mode, is part of the fork's `main`. A fork build from before that change rejects the request, and the tile then shows "UPDATE AFTERGLEAM FORK" while everything else keeps working.

Both apps must be installed with the same signing certificate, and both repositories' workflows require the same `RESEARCH_WIDGETS_*` secrets.

## Status

**Source version:** 1.10.2 (versionCode 18).

The latest source includes the shared feed-read pass and the reversed red position indicator on both strip widgets. The 1.10.2 changes have **not yet been compiled or tested**. The GitHub Actions build is manual-only, so run it from the Actions tab before treating this revision as a verified APK or publishing it. Taps, swipe and both looks still need confirmation on physical devices because launcher hosts differ. See [docs/IMPLEMENTATION-PLAN.md](docs/IMPLEMENTATION-PLAN.md) for the phased plan and what is verified.
