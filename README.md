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
- [How it works](#how-it-works)
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
| **Digest tile** | about 2×2 | Inverted tile with one big number. **Latest** counts today's digest (saved count underneath); **Downloaded** counts PDFs kept on the device (today's count underneath). Long-press to reconfigure. |

Both strips can show the **Recommended** digest or your **Saved** papers, chosen per widget, in a **Row** or **Rail** look.

- **Row**: position dots under the title.
- **Rail**: a vertical dot rail on the right edge, plus a `03/12` counter.

The current paper is the red dot. With up to 7 papers each gets its own dot; beyond that the dots become a proportional meter (first dot = first paper, last dot = last paper) and the counter gives the exact position.

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

## How it works

```mermaid
flowchart LR
    A[Aftergleam fork<br/>ContentProvider] -- signature permission --> C[PaperProviderClient]
    A -- data-changed broadcast --> R[ResearchWidgetsChangeReceiver]
    R --> P[PaperStripWidgetProvider]
    R --> T[TapPaperWidgetProvider]
    R --> D[DigestTileWidgetProvider]
    C --> P
    C --> T
    C --> D
    P -- adapter + template click --> S[StackView]
    C --> F[PaperStripRemoteViewsService<br/>RemoteViewsFactory]
    F -- one card per paper --> S
    T -- plain RemoteViews + direct clicks --> W[Paper Strip, No Swipe]
    S -- tap: PendingIntent + paper id --> O[OpenInAftergleamActivity<br/>hand-off]
    W -- tap paper --> O
    D -- tap --> O
    O --> A
```

| Package | Role |
| --- | --- |
| `data/` | Provider contract, read-only client (never throws; returns a state and message), per-widget preferences |
| `widget/` | Widget providers, the card factory, hand-off activity, `PendingIntent` helper, boot and data-changed receivers |
| `ui/` | Control-panel activity and the per-widget setup screens |

Design notes:

- **No network, no polling.** Papers come from a read-only `ContentProvider` guarded by a signature permission. Aftergleam sends a protected change broadcast and the widgets re-query. Provider reads are cached for a few seconds so a widget and its adapter share one query.
- **Reliable taps.** Android can silently block one app launching another from a window that isn't visible yet, and Android 15+ changed how a `PendingIntent` creator lends launch rights. Taps therefore go to a tiny translucent hand-off activity that opens Aftergleam from `onResume`, once its window is up, and the `PendingIntent` opts in to the creator launch privilege.
- **Full-width cards in `StackView`.** `StackView` measures cards with `AT_MOST` and parks back cards in the other 10% of its size. On Android 12+ the provider enlarges the stack to `widget / 0.9`, anchors it bottom-start, clips the back cards and sizes every card explicitly.
- **Layout fitting.** Title size comes from width tiers; the number of title lines (2, 3 or 5, via separate layouts) comes from the widget height after reserving space for dots or the counter. Row and Rail each have their own layouts.
- **Honest position indicator.** The dots never show a position the widget isn't at, and the screen-reader description says "Paper 3 of 12".
- **Dependency-free.** Plain Android SDK, no third-party runtime libraries.

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

**Tests.** Robolectric unit tests cover provider row mapping, graceful handling of a missing provider, per-widget feed and style storage (including widget restore), and a contract test that pins the package, permission, URIs, columns and intent values to the fork's v1 API.

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

Builds and unit tests pass in CI. Taps, swipe and both looks still need confirmation on physical devices, because launcher hosts differ. See [docs/IMPLEMENTATION-PLAN.md](docs/IMPLEMENTATION-PLAN.md) for the phased plan and what is verified.
