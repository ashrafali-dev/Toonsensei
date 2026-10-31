# Group work log

Detailed per-group change notes. Status counts live in TODO.md; this file records what was touched,
which tokens were used and which audit items passed.

## Group S — design system (branch `ui/design-system`)

- Tokens: ToonTheme.kt (day/night palettes, mode seals, type, spacing, radius, motion).
- Icons: ToonIcons.kt (45 glyphs). Mascot: ToonMascot.kt. Components: ToonComponents.kt
  (buttons, chips, mode chip, segmented control, grouped rows, toggle + switch, field, snack,
  banner, badge, empty/state block, skeleton, progress line, mode pill, chatbot ring,
  sheet and dialog scaffolds with scrim/motion + onDismiss hook).
- Compatibility: ToonSkin.kt keeps `UiTheme.*` names pointing at the palette so unmigrated screens
  render in Paper & Seal immediately; `Glass.*` adapts light-on-dark call sites to ink tints.
- Removed decorations: gradients/glows in `Glass.accent`, `appBackground`, `ExtractButton` disc,
  `ModeCapsule` thumb, progress card, dock, FABs, snack, tour ring, activity dots (seal tokens).
- Night: values-night styles/colors + drawable-night dialog; `applyPaperTheme()` runs on create and on
  `uiMode` change (status/nav bars, progress card, snack, error state).

Audit (group S):

| Item | Result |
|---|---|
| Contrast (measured, DESIGN.md §2.3) | pass — min 5.45:1 standard text; 3.17 only for disabled icons |
| Token-only colour in redesigned code | pass — literals remain only in the token files, seal palettes, tab-hue covers and JS/HTML |
| Touch targets ≥ 48 dp | pass in ToonComponents (icon buttons 48 min, buttons 48, rows ≤ 52/64) |
| TalkBack | pass on new components (descriptions on icon buttons, switches, mode pill, ring, snack live region) |
| Reduced motion | pass in new components |
| Light + dark | tokens pass; per-screen verification below |

## Group H — overlays (commits on `ui/design-system`)

What moved (screen-by-screen commits):
1. Link context menu and novel actions → `Toon.SheetDialog` with icon rows; new `confirmDelete`.
2. `listDialog` and `searchListDialog` rebuilt on the sheet shell (grouped rows, visible search field,
   48 dp option rows); prompt editors ×3, rename, first-run prompt choice, Send-hold editor → dialogs.
3. Scan direction + pre-scan question → radio sheets; OCR engine picker → described rows; Send-hold.
4. Provider failed, chapter glossary (list/add), community chooser + prompt, update notice, quick
   tips, learned-site report, chatbot add/remove, first-run prompt, AI eraser delete, detector /
   mask model, site settings + rule editor, ad-block rules → Toon dialogs. `AlertDialog` is no longer
   referenced by MainActivity.
5. Language sheet rebuilt (grouped pickers, footer actions), tour card restyled.
6. Snackbar: ink pill, one at a time, live region, follows the keyboard via window insets.

Audit (group H):

| Item | Result |
|---|---|
| Dialog coverage | 36/36 AlertDialog sites replaced or removed |
| Contrast | dialog body `muted` on `surface` 5.45:1; actions use primary/secondary/ghost tokens |
| Destructive separation | filled danger button, never the primary slot |
| Focus/back | dialog back dismisses; sheets close on scrim/back; modal guards reset via onDismissHook |
| Emoji in chrome | 0 removed (all strings de-emojified) |
| 360 dp | rows wrap to two lines, footers use weight-based widths |

## Group I — states

- Failed page: mascot + reason + Retry over the reader; cleared on next navigation. (13 px body, 96 dp mascot)
- Empty library / bookmarks / history: state blocks in sheets, not toasts.
- Loading: existing 44 dp progress card now paper; live-region label. Skeleton component ready for
  lists (not yet wired — tracked in TODO I05).
- Slow/offline banners and permission states tracked as I02/I04/I11.

## Group A — navigation & Home

- Home opens with four mode chips (Novel · Scanning · Real-time · Manual Lite) and one-line descriptions;
  tapping selects the mode through existing state (Manual Lite = WebToon with auto-OCR off).
- Bottom navigation (Library · Menu) on Home.
- Reader top bar: paper pill, 48 dp tab counter with count in description, flat hairline.
- Mode pill on the dock + four-option mode sheet with segmented view switch.
- Chatbot ring at the dock's right edge (chapter progress arc + view glyph).

## Group B — novel reader

- Dock: paper capsule, flat extract disc, labelled mode pill, chatbot ring.
- Offline reader palette: paper day theme, sepia, chartreuse-free charcoal, white; night values follow.

## Group F — library & search

- Tab grid on paper (surface cards, seal border on the active tab, 32 dp close buttons).
- History empty state; bookmarks empty state; library empty state.

## Group G — settings

- Menu: title, settings search that collapses non-matching groups, labels de-emojified and
  normalised to "Title: Value" badges; scan-direction wording fixed.

## Known risks

- No Gradle build was possible offline in this environment; all changes are reviewed source edits and
  still need `./gradlew assembleDebug` on a machine with the SDK/Gradle cache.
- Web-writer bubble styling (C04/D02) is untouched; it inherits the page's own colours.
- Manual Lite history (E05) and dedicated scan-progress card (C03) rewrites remain as noted in TODO.
