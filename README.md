# Research Widgets

Independent Android companion for the customized Aftergleam fork.

- Application ID: `com.placeholder2312.researchwidgets`
- Minimum SDK: 26; target SDK: 37
- UI: native Android AppWidget, RemoteViews and StackView
- Network permissions: none
- Provider API: `si.jakobkreft.aftergleam.researchwidgets`, v1

## Widgets

1. **Research Paper Strip** — approximately five columns by one row. Shows one full-width paper title at a time in the Nothing Classic style (light/dark follows the system); swipe up or down to move through papers (Android StackView). A dot meter shows position (one dot per paper up to 7, proportional beyond that) with the current one in red. On Android 12+ the stack is enlarged to widget/0.9 and each card is sized explicitly, because StackView measures cards with `AT_MOST` and would otherwise shrink them to their text. Tapping a title opens that paper in Aftergleam via an in-app trampoline that reports failures. Configure each instance for Recommended or Saved.
2. **Aftergleam tile** — approximately 2×2, Nothing Classic card with the Aftergleam artwork, opens the customized app.

## Build

Open the repository in Android Studio or run `./gradlew testDebugUnitTest assembleDebug`. The debug APK is produced under `app/build/outputs/apk/debug/`.

## Signing is required for IPC

Both custom APKs must use the same locally controlled certificate to access the signature-protected provider and paper-opening endpoint. For both projects configure the same values, outside Git, using environment variables:

- `RESEARCH_WIDGETS_KEYSTORE`
- `RESEARCH_WIDGETS_KEYSTORE_PASSWORD`
- `RESEARCH_WIDGETS_KEY_ALIAS`
- `RESEARCH_WIDGETS_KEY_PASSWORD`

The companion build uses this key for debug and release when configured. The Aftergleam fork must be configured with the same keystore for its debug and release builds. Do not commit keystores or passwords. The default debug keys from two separate projects do not match and will not pass signature permission checks.

Install the customized Aftergleam fork before Research Widgets so Android knows the custom permission owner. The official Aftergleam application does not expose this API.

## CI

GitHub Actions builds are manual-only. The workflow requires the shared keystore secrets, validates the keystore before compilation and prints the certificate fingerprint used by the APK signing step. The APK signature is verified before the artifact is uploaded. The test job also renders widget screenshots at compact, standard and resized dimensions for visual inspection.


## Download APK

- **[Download Research Widgets v1.3.3 APK](https://github.com/Placeholder2312/Research-Widgets/blob/apk-downloads/downloads/Research-Widgets-v1.3.3-debug.apk?raw=true)**
- Version code: 7
- APK size: 935,148 bytes
- SHA-256: `61dc31bc314c6a6ac57b3aeaf94aae83aff7271393ef559f4fcbec92a794ad27`

Build provenance: this APK was built successfully and signed from branch `fix/stackview-full-width-cards` at commit `00ad2644dc107a92fcb257e5bfd25ce6e4dfc13d`. It is not a build of `feature/aftergleam-integration`; that branch was at v1.3.0 when checked.