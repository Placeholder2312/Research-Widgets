# Research Widgets

An offline-first Android companion for the customized Aftergleam fork.

This repository is intentionally independent from Aftergleam. It contains launcher widgets only and reads paper metadata from Aftergleam's versioned, read-only ContentProvider. It does not implement recommendation or ranking logic.

## Development status

Project bootstrap in progress. The provider contract and security boundary are documented in the Aftergleam fork at `docs/RESEARCH-WIDGETS-INTEGRATION.md`.

- Package: `com.placeholder2312.researchwidgets`
- Minimum Android: API 26
- Target Android: API 37 (aligned with the fork; verify on target devices)
- UI: native Android Views, AppWidget, RemoteViews, StackView
- Network access: none required
- IPC: signature-protected ContentProvider and explicit protected paper-open Activity

## Local build

Open this project in Android Studio and sync Gradle. Build the debug APK from the app module. Configure the same private signing keystore in both applications before testing signature-protected IPC; never commit keystores or passwords.

## Widgets

1. **Research Paper Strip** — one title card at a time, vertically navigable, configurable for Recommended or Saved.
2. **Aftergleam tile** — decorative 2×2 launcher tile using the Aftergleam brand mark and opening the customized app.

## Important

The companion only works with a compatible customized Aftergleam build that provides provider API v1. An official Aftergleam installation does not expose this private integration API.
