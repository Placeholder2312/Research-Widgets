# Research Widgets

Independent Android companion for the customized Aftergleam fork.

- Application ID: `com.placeholder2312.researchwidgets`
- Minimum SDK: 26; target SDK: 37
- UI: native Android AppWidget, RemoteViews and a full-viewport ListView
- Network permissions: none
- Provider API: `si.jakobkreft.aftergleam.researchwidgets`, v1

## Widgets

1. **Research Paper Strip** — approximately five columns by one row. It shows one full-viewport paper title at a time; swipe vertically to move to the next or previous title. Nothing Classic styling, position dots and dot-matrix previous/next controls are retained. Configure each instance for Recommended or Saved.
2. **Aftergleam tile** — approximately 2×2, decorative background and the actual Aftergleam launcher foreground artwork, opens the customized app.

The paper rows fill the widget's available height so the launcher does not fan several titles across the strip. The ListView scrolls vertically; the side chevrons provide an additional one-paper navigation control.

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

GitHub Actions builds are manual-only. The workflow requires the shared keystore secrets, validates the keystore before compilation and prints the certificate fingerprint used by the APK signing step. The APK signature is verified before the artifact is uploaded.
