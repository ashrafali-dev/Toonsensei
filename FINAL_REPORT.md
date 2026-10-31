# ToonSensei UI/UX redesign — final report

Branch: **`ui/design-system`** (base `in-1-minute,-later-detector`), fast-forward chain planned for
`ui/group-h…g` and `ui/final-polish`; this branch carries every change so each step can be cherry-picked
into its own group branch without rewriting history.
Spec: `docs/redesign/DESIGN.md` · live status: `TODO.md` · change notes & audits: `docs/redesign/GROUPS.md`.

## Build status — action needed

**The project was not compiled in this environment.** Termux here has no Android SDK platform 34, no
Gradle distribution and no network access to Gradle/Google repositories, so `./gradlew assembleDebug`
could not run. All edits were reviewed by hand (brace balance, symbol audit, import sweep) and the
new colour contract and chapter-number helper are covered by JVM tests
(`app/src/test/.../ToonThemeTest.kt`, `ToonUrlTest.kt`). Run on a build machine before merging:

```
./gradlew :app:assembleDebug :app:testDebugUnitTest
```

Known compile-sensitive spots to check first: `ToonComponents.kt` (new file, largest addition),
`MainActivity.kt` onReceivedError/insets block, `ReaderActivity.kt` capsule rewrite.

## Group status

| Group | Branch | Status | Notes |
|---|---|---|---|
| S Design system | `ui/design-system` | done | tokens, 45 icons, mascot, 20 components, day/night, motion + reduced-motion, test |
| H Overlays | `ui/group-h-overlays` | done | 36/36 AlertDialogs replaced, all sheets on `Toon.SheetDialog`, snackbar + insets, task tray, banners |
| I States | `ui/group-i-states` | mostly done | failed page (+offline), slow banner, empty library/bookmarks/history/glossary/chapter, skeleton component + Home skeleton; slow/offline banners wired; permission state and skeleton on library/tabs remain |
| A Navigation & Home | `ui/group-a-navigation-redesign` | done | 4 mode chips, bottom nav, paper top bar, mode pill, mode sheet, chatbot ring, tour on Home |
| B Novel reader | `ui/group-b-novel-reader-redesign` | mostly done | dock, extract button restyle, labelled pill, ring, offline reader controls/theme; reader appearance sheet (B06) not built; inline web typography untouched |
| C Scanning | `ui/group-c-scanning-redesign` | partial | re-scan bar, scan sheets, missing-bubble/glossary rails restyled; dedicated scan-progress card (C03) still uses the translation card |
| D Real-time | `ui/group-d-realtime-redesign` | partial | dock state via pill, tool rail FABs on paper, start notice still toasts; bubble styling untouched |
| E Manual Lite | `ui/group-e-manual-lite-redesign` | partial | mode entry, seal highlight; crop overlay (E02) is the existing native capture, result card not redesigned |
| F Library & search | `ui/group-f-library-search-redesign` | done | library with covers, novel actions, chapter list, bookmarks, history, tabs grid, tab options |
| G Settings | `ui/group-g-settings-redesign` | done | menu title + search, mode sheet, language sheet, appearance still via old dark-mode row |
| P Planned | — | out of scope | Link→Translate and Preview reader do not exist in source |

## What changed, by theme

1. **Identity.** Warm paper background, hairline rules, serif wordmark + chapter text, the worm mascot;
   purple/blue neon gradients, glow shadows, glassmorphism, emoji and the rainbow stage colours are gone.
2. **Colour system.** One `ToonColors` record per mode with 29 roles, four mode seals
   (Novel `#9C3D2E`, Scanning `#2E6B47`, Real-time `#8C2C1D`, Manual Lite `#1A4FB4`) and a single accent
   (`#2C6650` paper / `#E07A5F` night). Measured contrast table in DESIGN.md §2.3, asserted by unit test.
3. **Typography.** Six sans roles + serif reader/wordmark, section labels as letterspaced marginalia.
4. **Components.** 48 dp buttons/fields/rows, 44 dp icon boxes with 48 dp hit areas, pill chips, segmented
   control with spring thumb, switches, Sheet/Dialog scaffolds with scrim + motion + dismiss hook,
   snackbar with keyboard insets, banners, state blocks, skeletons, progress line, mode pill, chatbot ring.
5. **Structure.** Home now states the product (four modes), the dock says which mode is active, and every
   list-like surface (menu, lists, tabs, glossary, site rules) is a real sheet instead of a stock dialog.

## Audit summary (gate from TODO.md)

| Item | Result |
|---|---|
| Contrast ≥ 4.5:1 | pass on all text tokens (test + DESIGN.md table); disabled icons 3.17:1 by design |
| Focus / TalkBack | labels on icon buttons, switches, mode pill, ring, tab counter, snack live region, stage announcements |
| Touch targets ≥ 48 dp | pass on new components and dock controls; legacy `chip()` 34 dp and tab-rail 40 dp rows still below — listed as remaining work |
| Light + dark | chrome has both palettes and re-applies on `uiMode`; reader has paper/sepia/charcoal/white |
| 360 dp | sheets use weight-based footers and wrapping rows; verified by inspection, not by device run |
| Motion | 110/220/260 ms tokens, reduced motion honoured in new components |
| Emoji | removed from all UI strings |

## Remaining work

- Compile + device pass (above), then screenshot each group on 360 dp and 411 dp, light and dark.
- C03 scan progress card, D04 real-time start banner, E02/E03 manual crop + result card, B06 reader
  appearance sheet, I05 skeletons on library/tabs, I11 permission states.
- Web-layer bubble typography (B10/C04) can take the reader font and paper colours from `ToonTheme`.
- The old dark-mode toggle (menu row 9) still drives WebView darkening; the appearance group should take
  over theme selection in G04.

## Risks

- Unbuilt code: see build status.
- Large `MainActivity.kt` edits were applied scripted; a compile is required to catch any missed symbol.
- `Glass.*` remains for compatibility; it adapts colours at call sites but is deprecated in favour of
  `Toon` components.
- No PRs were opened, per instructions; group branches are to be created from this branch by
  cherry-picking the commits tagged `ui(group-x)` in their messages.
