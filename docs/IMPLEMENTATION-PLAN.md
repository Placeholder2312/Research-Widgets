# Implementation plan and verified status

## Work sequence

### Phase 0 — repository and contract audit (complete)
- Keep the original upstream read-only.
- Work on the Aftergleam integration and companion app repositories.
- Freeze provider API v1 fields and permission name before building the companion against it.

### Phase 1 — Aftergleam provider and handoff (implemented; automated CI green)
- Read today's persisted digest in its stored rank order; do not rank in the companion.
- Read currently saved records ordered by the last SAVED signal timestamp.
- Expose only the seven documented metadata columns through a signature-protected, read-only ContentProvider.
- Open a paper through the signature-protected PaperOpenActivity trampoline, not by exporting MainActivity as a new API.
- Invalidate the two feed URIs and send a package-targeted, signature-protected update broadcast after digest, save/unsave, import, restore, and notification-save writes.

### Phase 2 — companion widgets and settings (implemented)
- Native AppWidget/RemoteViews app with package `com.placeholder2312.researchwidgets`.
- Paper Strip uses Android StackView for up/down swipe paging, enlarged to widget/0.9 on Android 12+ with explicitly sized cards so one full-width title shows at a time (the back cards sit outside the widget and are clipped).
- One rounded Nothing-style shell; the red position dot starts on the last dot and moves toward the first as papers advance. Beyond 7 papers, the proportional meter is mirrored the same way. Taps use a PendingIntent to a hand-off activity that opens Aftergleam from onResume, with the creator background-launch opt-in on Android 15+.
- Keep per-instance Recommended/Saved feed and Bottom/Side looks (stored preference keys remain `row`/`rail`), empty/disconnected states, title typography and dark/light colours. Bottom places dots under the title; Side places a vertical dot rail at the right with a counter.
- Paper Strip (No Swipe) is a separate plain `RemoteViews` widget with direct paper-open and next-paper click zones; it has its own feed, look and position preferences.
- Decorative Aftergleam tile uses the supplied brand mark and opens the customized fork.
- Digest tile (2×2, inverted card) has Latest (today's digest + saved) and Downloaded (PDFs on device) modes, chosen per instance from the provider, refreshed by the data-changed broadcast and on boot/update. The fork's `v1/downloaded` endpoint is required for Downloaded mode.
- Companion receiver handles protected data-change notifications and refreshes all three widget providers through one shared `FeedPass`; each feed is queried at most once per pass. The short-lived provider cache is reused by the collection adapter, and resize-only updates reuse recent reads.
- Boot/update lifecycle handling restores widget state.

### Phase 3 — shared signing and cross-app integration (shared key configured)
- Use one locally controlled release keystore for both applications.
- Configure identical `RESEARCH_WIDGETS_KEYSTORE_BASE64`, `RESEARCH_WIDGETS_KEYSTORE_PASSWORD`, `RESEARCH_WIDGETS_KEY_ALIAS`, and `RESEARCH_WIDGETS_KEY_PASSWORD` Actions secrets in both repositories.
- CI validates the same keystore in both repositories and verifies each APK's signature before upload.

### Phase 4 — robustness and verification (1.10.2 build pending; device check pending)
- The Robolectric suite covers provider mapping, graceful provider failures, per-widget preferences and restore, the Aftergleam v1 IPC contract, and `FeedPass` query deduplication.
- The GitHub Actions workflow is manual-only. No build or test result is recorded yet for the merged 1.10.2 source; run the workflow before treating that revision as a verified APK.
- The earlier Robolectric screenshot/flipper tests were removed with the flipper; layout rendering is verified by eye on device.
- Physical Samsung One UI verification of swipe, tap, full-width title and both styles is still required, because launcher hosts differ.

### Phase 5 — migration and release (user-controlled)
- Do not uninstall or modify the official installation automatically.
- User exports and verifies JSON backup, then chooses when to replace the official package.
- Restore backup into the custom fork; cached paper metadata is refetched and is not included in JSON.
- Install companion, validate both feeds and widgets, then consider migration complete.
