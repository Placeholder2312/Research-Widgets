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
- Paper Strip uses Android StackView's discrete up/down card-swipe navigation.
- A single rounded Nothing-style shell clips the StackView and chevrons at the widget bounds. Paper rows are transparent so card transitions do not create nested rounded corners or distorted edge cutouts.
- Keep per-instance Recommended/Saved preferences, empty/disconnected states, title typography, red position dots, dark/light colours and dot-matrix previous/next controls.
- Decorative Aftergleam tile uses the supplied brand mark and opens the customized fork.
- Companion receiver handles protected data-change notifications and refreshes widgets; boot/update lifecycle handling restores widget state.

### Phase 3 — shared signing and cross-app integration (shared key configured)
- Use one locally controlled release keystore for both applications.
- Configure identical `RESEARCH_WIDGETS_KEYSTORE_BASE64`, `RESEARCH_WIDGETS_KEYSTORE_PASSWORD`, `RESEARCH_WIDGETS_KEY_ALIAS`, and `RESEARCH_WIDGETS_KEY_PASSWORD` Actions secrets in both repositories.
- CI validates the same keystore in both repositories and verifies each APK's signature before upload.

### Phase 4 — robustness and visual verification (in progress)
- Render the real widget layouts with Robolectric at compact, standard and resized launcher dimensions.
- Save screenshots for the initial card and after card navigation; inspect all screenshots for rounded-edge clipping and the Nothing layout.
- Verify native StackView next/previous card selection and arrow synchronization.
- Physical Samsung One UI verification remains necessary because launcher hosts can differ.

### Phase 5 — migration and release (user-controlled)
- Do not uninstall or modify the official installation automatically.
- User exports and verifies JSON backup, then chooses when to replace the official package.
- Restore backup into the custom fork; cached paper metadata is refetched and is not included in JSON.
- Install companion, validate both feeds and widgets, then consider migration complete.
