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
- A single rounded Nothing-style shell clips the StackView and chevrons at the widget bounds. Paper rows are transparent so card transitions do not create nested rounded corners or distorted edge cutouts. On Android 12+, the RemoteViews card is explicitly sized to StackView's 90% child viewport (minus its built-in frame padding); this prevents text-intrinsic sizing from making the stack fan across the strip and lets the active card fill nearly the whole widget.
- Keep per-instance Recommended/Saved preferences, empty/disconnected states, title typography, red position dots, dark/light colours and dot-matrix previous/next controls.
- Decorative Aftergleam tile uses the supplied brand mark and opens the customized fork.
- Companion receiver handles protected data-change notifications and refreshes widgets; boot/update lifecycle handling restores widget state.

### Phase 3 — shared signing and cross-app integration (shared key configured)
- Use one locally controlled release keystore for both applications.
- Configure identical `RESEARCH_WIDGETS_KEYSTORE_BASE64`, `RESEARCH_WIDGETS_KEYSTORE_PASSWORD`, `RESEARCH_WIDGETS_KEY_ALIAS`, and `RESEARCH_WIDGETS_KEY_PASSWORD` Actions secrets in both repositories.
- CI validates the same keystore in both repositories and verifies each APK's signature before upload.

### Phase 4 — robustness and visual verification (emulated visual pass complete; device check pending)
- Render the real widget layouts with Robolectric at compact (360×56 dp), standard (420×88 dp), and taller (420×96 dp) dimensions using native graphics.
- Capture the current-card, next-card, compact and tall screenshots; inspect the rounded shell, title/chevron clearance, Nothing dot row, and transition state.
- Verify native StackView next/previous card selection and arrow synchronization.
- The shared shell clips the animated StackView to its rounded outline; transparent card rows avoid nested rounded corners. RemoteViews card padding mirrors the 40 dp chevron-safe inset used by the live widget.
- Physical Samsung One UI verification remains necessary because launcher hosts can differ.

### Phase 5 — migration and release (user-controlled)
- Do not uninstall or modify the official installation automatically.
- User exports and verifies JSON backup, then chooses when to replace the official package.
- Restore backup into the custom fork; cached paper metadata is refetched and is not included in JSON.
- Install companion, validate both feeds and widgets, then consider migration complete.
