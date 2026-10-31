# Group work log

Detailed per-group change notes. Status counts live in TODO.md; this file records what was touched,
which tokens were used and which audit items passed.

## Group S — design system (branch `ui/design-system`)

- Tokens: ToonTheme.kt (day/night palettes, mode seals, type, spacing, radius, motion).
- Icons: ToonIcons.kt (45 glyphs). Mascot: ToonMascot.kt. Components: ToonComponents.kt.
- Compatibility: ToonSkin.kt keeps `UiTheme.*` names pointing at the palette so unmigrated screens
  render in Paper & Seal immediately; `Glass.*` adapts light-on-dark call sites to ink tints.
- Removed decorations: gradients/glows in `Glass.accent`, `appBackground`, `ExtractButton` disc,
  `ModeCapsule` thumb, progress card, dock, FABs, snack, tour ring.
- Chrome: system theme is now light-paper; status/navigation bars follow `ToonTheme.dark()` and are
  re-applied on `uiMode` change (`applyPaperTheme`).

Audit (group S):

| Item | Result |
|---|---|
| Contrast (measured, DESIGN.md §2.3) | pass — min 5.45:1 for standard text |
| Token-only colour in migrated files | pass for Toon*.kt, Ui.kt, ModeUi.kt, Home.kt, Tour.kt, ReaderActivity.kt, ActivityChip.kt |
| Touch targets ≥ 48 dp | pass in ToonComponents (iconButton 48 min, buttons 48) |
| Emoji in chrome | pending until groups H/A move menu labels to icons |
| Reduced motion | pass in new components (`Toon.reducedMotion`, durations 0) |
| Light + dark | tokens pass; screens verified per group |
