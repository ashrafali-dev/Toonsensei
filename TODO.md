# ToonSensei UI/UX Redesign — Live TODO

Source of truth branch: `Adding-stop-on-scanning`
Source HEAD at start: `2e200b1d80b66fc0e09283f7b3dc0a934b09ca57`
Working branch: `ui/g1-theme-and-components`

## Status legend

- `[ ]` Not started
- `[~]` In progress / implementation exists but verification is pending
- `[x]` Verified complete
- `[-]` Excluded from current scope (reason recorded)

## Project inventory

| Item | Verified baseline |
|---|---:|
| `MainActivity.kt` | 11,538 lines |
| `Ui.kt` | 608 lines |
| `Home.kt` | 596 lines |
| `ModeUi.kt` | 482 lines |
| `ReaderActivity.kt` | 182 lines |
| Kotlin files in repository | 96 |
| `AlertDialog.Builder` construction sites in MainActivity | 36 |
| `Sheets.create` construction sites in MainActivity | 7 |
| Other custom Dialog construction sites | 1 |
| Total modal construction points | 44 |
| Existing `toast(...)` call sites | 191 |

The 44 modal construction points are a code inventory, not 44 unique HTML boards. Shared builders produce multiple runtime dialogs, lists, and states. These must be counted by user-visible flow during implementation.

## Reference-board inventory

The uploaded `toon-sensei-ui-spec.zip` contains 57 numbered boards. Board 09 is superseded; boards 11 and 12 are explicitly planned for a later phase. The active redesign scope contains **54 boards**.

| Group | Scope | Board count | Priority |
|---|---|---:|---|
| G1 | Design system / components | 0 | P0 |
| G2 | Common overlays | 14 | P0 / P1 |
| G3 | State views | 2 + offline/permission variants | P0 |
| G4 | Home + navigation | 5 | P0 / P1 / P2 |
| G5 | Reader / browser chrome | 6 | P0 / P1 |
| G6 | Modes | 2 visual boards / 4 mode implementations | P0 |
| G7 | Settings + Library + Search | 19 | P0 / P1 / P2 |
| G8 | Splash + permission onboarding | 6 | P0 / P1 |
| **Total active HTML boards** |  | **54** |  |

### Excluded / later-phase references

- `09-menu.html`: superseded older menu sketch.
- `11-linkrun.html` and `12-preview.html`: later-phase planned features; do not implement in this redesign pass unless the owner explicitly expands scope.
- Board 10 is the approved “Link → Translate (soon)” placeholder, not permission to build the planned flow.

## G1 — Theme and component foundation

Branch: `ui/g1-theme-and-components`

### Files

- [x] Project-level Codex UI/UX Pro Max skill files added under `.agents/skills/ui-ux-pro-max/`.
- [x] ToonSensei Android Views-specific skill guidance added.
- [x] `ui/theme/ToonTheme.kt`: semantic token model, light/dark resource lookup, typography, space, radius, motion, elevation, accent presets.
- [x] `ui/components/ToonComponents.kt`: shared shape, ripple, text, focus and touch helpers.
- [x] `ui/components/ToonButton.kt`: primary / secondary / ghost / danger and compact / regular variants.
- [x] `ui/components/ToonChip.kt`: selected / unselected / disabled chip.
- [x] `ui/components/ToonBadge.kt`: neutral / accent / success / warning / error badge.
- [x] `ui/components/ToonCard.kt`: flat surface card; interactive use requires an accessibility label.
- [x] `ui/components/ToonInput.kt`: visible label, hint, focus, keyboard type and multiline field.
- [x] `ui/components/ToonDialog.kt`: MaterialAlertDialogBuilder wrapper.
- [x] `ui/components/ToonBottomSheet.kt`: Material BottomSheetDialog wrapper.
- [x] `ui/components/ToonToast.kt`: persistent in-layout notice shim; not Android Toast.
- [x] `ui/components/ToonStateView.kt`: loading / empty / error / offline / permission primitive.
- [x] Interactive button / chip / card / notice focus outline and 48 dp minimum touch target.
- [x] Dark selected-accent foreground contrast derived to at least 4.5:1 on selected tint.
- [x] Control edge token contrast ≥3:1 on Paper and Charcoal palettes.
- [x] Static scan of 11 G1 Kotlin theme/component files: no raw hex colors, numeric dp literals in dp calls, or hardcoded numeric textSize assignments.
- [x] Paper colors under `res/values/colors.xml`.
- [x] Charcoal colors under `res/values-night/colors.xml`.
- [x] Material Components theme overlays and night system-bar override.
- [x] Material Components dependency pinned to `1.12.0` to avoid an AGP/compileSdk upgrade in this group.
- [x] `DESIGN_SYSTEM.md` written from the uploaded ROADMAP and board spec.
- [x] `docs/redesign/G1_SUMMARY.md` records G1 branch changes and unverified checks.
- [x] Kotlin Release compilation + Android resource merge (`41217011068`; final tested component snapshot `1d95eca322ed871b4f15e05ee89fae30bb9ec868`).
- [~] Static contrast calculations / 3:1 control edges / 4.5:1 text checks; runtime Light/Dark, TalkBack, keyboard focus, 360 dp / 412 dp, and font-scale checks still need a physical/emulated device.
- [ ] Baseline component screenshots on device (not available in the current environment).
- [ ] Codex skill auto-discovery confirmation on the owner's Codex installation.

G1 code/build gate: PASS. Physical-device and Codex-client verification remain open. G2 must wait for the owner to verify the branch on the target device and confirm Codex discovers the project skill.

### G1 final build evidence

- Final GitHub Actions run: [41217011068](https://github.com/ashrafali-dev/ToonSensei/actions/runs/41217011068) — Build / permanent certificate verification / artifact upload PASS.
- Signed APK artifact: `NovelStudio-SIGNED-RELEASE-APK` (23,037,797 bytes; artifact id `13048647988`; expires 2026-11-30).
- Final tested code snapshot: `1d95eca322ed871b4f15e05ee89fae30bb9ec868`. Workflow-only cleanup after that snapshot restored the original push trigger list.
- Final build does not equal physical-device validation. Keep G2 blocked until device/Codex checks are done.

## G2 — Common overlays

Branch: `ui/g2-overlays-migration`
Reference boards: **14**

Boards:
- P0: [ ] 13 Notice banner · [ ] 16 Mode sheet · [ ] 17 Re-scan overlay · [ ] 28 Popup patterns
- P1: [ ] 05 Tasks tray · [ ] 18 Chapter glossary list · [ ] 19 Confirm found names · [ ] 20 Add glossary entry · [ ] 24 Tab options · [ ] 25 Search-result context menu · [ ] 27 Message presentation · [ ] 36 Chatbot row menu · [ ] 44 Delay pickers · [ ] 57 Update/community dialogs

Runtime dialog inventory (36 source construction sites):
1. Chapter glossary list
2. Delete chapter glossary entry
3. Chapter glossary type picker
4. Add chapter glossary entry
5. Community chooser
6. Community invitation
7. Update notice
8. Send-hold duration input
9. Quick tip
10. Long-reading help
11. First prompt choice
12. Link context menu
13. Scanning direction picker
14. OCR engine picker
15. Remove chatbot confirmation
16. Add chatbot form
17. Scan options picker
18. Translation failed / retry notice
19. AI provider chooser
20. “What the app learned” site explanation
21. AdBlock rules editor
22. Site settings chooser
23. Site rule editor
24. Global glossary menu
25. Delete all glossary confirmation
26. Add glossary entry
27. Delete AI text-eraser model confirmation
28. Bubble detector details / model action
29. Balloon mask model details / action
30. Novel actions
31. Delete novel confirmation
32. Delete chapter confirmation
33. Rename novel
34. Edit Novel prompt
35. Edit Scanning prompt
36. Edit Real-time prompt

Also migrate the 7 `Sheets.create` construction points and 1 custom chat-login `Dialog`. Convert each user-visible flow to the correct dialog/sheet/banner/log pattern. Classify the 191 toast call-sites; do not blindly turn all of them into banners.

- [ ] Extract overlays from MainActivity into `ui/screens/overlays/`.
- [ ] Migrate each dialog/sheet separately; one user-visible flow per commit.
- [ ] No behavior/callback/permission change.
- [ ] Vercel UX guideline subset audit adapted for native Android; report non-applicable web checks.
- [ ] Save baseline screenshots/notes for every overlay board.

## G3 — State views

Branch: `ui/g3-state-views`
Reference boards: **2**, plus non-visual state variants.

- P0 [ ] Board 26: Loading / Slow / Failed.
- P0 [ ] Board 46: Library Empty.
- P0 [ ] Offline / network unavailable.
- P0 [ ] Permission required / denied / rationale.
- P1 [ ] Retry / recover / no-results sub-states where existing logic supports them.

- [ ] Use the shared `ToonStateView` once; do not create one-off screen-specific state widgets.
- [ ] Replace fake/indeterminate progress with real stage/counter/time when the task exposes it.
- [ ] Integrate loading/empty/error/offline/permission states across applicable screens.
- [ ] Audit every state against TalkBack and dark/light palette.

## G4 — Home + navigation

Branch: `ui/g4-home-navigation`
Reference boards: **5**

- P0 [ ] Board 02: Home.
- P0 [ ] Board 21: Mode start (Novel).
- P1 [ ] Board 23: Tabs.
- P1 [ ] Board 38: Everyday menu.
- P2 [ ] Board 10: Link → Translate “soon” placeholder only.

- [ ] Extract Home builder from MainActivity.
- [ ] Reuse the shared token-driven search, site shortcut, recent and action components.
- [ ] Keep deep links, tab history, and browser behavior intact.
- [ ] No implementation of planned Board 11/12 in this pass.

## G5 — Reader / browser chrome

Branch: `ui/g5-reader`
Reference boards: **6**

- P0 [ ] Board 03: Site + dock.
- P0 [ ] Board 04: Site with controls hidden / immersive.
- P0 [ ] Board 06: Split with chatbot.
- P1 [ ] Board 22: Chatbot pane sizes / cycle.
- P1 [ ] Board 37: Provider tabs.
- P1 [ ] Board 49: Saved reader.

- [ ] Extract top bar, dock, split layout, immersive controls and saved reader UI.
- [ ] Keep `novelWv` and `chatWv` alive; do not replace real websites with native mock content.
- [ ] Status remains visible when chatbot pane is hidden.
- [ ] No bottom action bar for temporary modes.
- [ ] Preserve WebView back, scroll, navigation, renderer recovery and JavaScript bridge behavior.

## G6 — Mode UI

Branch: `ui/g6-modes`
Reference boards: **2**, four functional screen implementations.

- P0 [ ] Board 07: Real-time + tools.
- P0 [ ] Board 08: Manual Lite + tools.
- P0 [ ] Extract Novel mode.
- P0 [ ] Extract Scanning mode.
- P0 [ ] Extract Real-time mode.
- P0 [ ] Extract Manual Lite mode.

- [ ] Create separate `NovelModeScreen.kt`, `ScanningModeScreen.kt`, `RealTimeModeScreen.kt`, `ManualLiteModeScreen.kt`.
- [ ] Preserve mode state machine, OCR, batching, pause/resume, manual crop/read, and callback wiring.
- [ ] Manual Lite = Real-time with Auto OFF.
- [ ] No new OCR engine and no duplicate OCR control state.

## G7 — Settings + Library + Search

Branch: `ui/g7-settings-and-library`
Reference boards: **19**

Settings boards:
- P0 [ ] Board 14: Appearance.
- P0 [ ] Board 15: OCR engine.
- P1 [ ] Board 34: Chatbots list.
- P1 [ ] Board 35: Add chatbot.
- P1 [ ] Board 39: Advanced home.
- P1 [ ] Board 40: Translation settings.
- P1 [ ] Board 41: Webtoon tools.
- P1 [ ] Board 42: Browser settings.
- P1 [ ] Board 43: Diagnostics.
- P1 [ ] Board 52: Prompts.
- P1 [ ] Board 53: Novel glossary.
- P1 [ ] Board 54: Site settings.
- P1 [ ] Board 55: Edit site settings.
- P1 [ ] Board 56: Chatbot login.

Library/search boards:
- P0 [ ] Board 45: Library list.
- P1 [ ] Board 47: Library / novel actions.
- P1 [ ] Board 48: Chapters.
- P1 [ ] Board 50: Bookmarks.
- P1 [ ] Board 51: History.

- [ ] Extract settings, about, library, browser history and search builders.
- [ ] Preserve existing preference keys and saved language/provider values.
- [ ] No new feature switches without a matching board or owner instruction.
- [ ] Use trailing trash/delete affordance rather than long-press-only deletion.

## G8 — Splash + Permission Onboarding

Branch: `ui/g8-onboarding-splash`
Reference boards: **6**

- P0 [ ] Board 01: Splash.
- P0 [ ] Board 29: First-run language.
- P0 [ ] Board 30: First-run OCR engine.
- P1 [ ] Board 31: Tips.
- P1 [ ] Board 32: Chat tour.
- P1 [ ] Board 33: Re-scan / glossary tour.
- P0 [ ] Notification permission states: ask / allowed / denied, following Android lifecycle and existing capability checks.

- [ ] Existing users do not see first-run language/OCR screens again.
- [ ] Preserve selected language and OCR engine.
- [ ] Tour spotlight is static; no pulse or breathing animation.

## MainActivity extraction plan

The target is `MainActivity.kt < 1,500 lines`, containing navigation + wiring only.

- [ ] Overlays and dialog builders.
- [ ] Shared state views and notice/status integration.
- [ ] Home.
- [ ] Reader/browser chrome.
- [ ] Four modes.
- [ ] Settings, library, history, search.
- [ ] Remaining lifecycle/navigation wiring.
- [ ] Record line count after every extraction; it must decrease.

No new View construction code goes into MainActivity after G1. Each extraction receives its own commit and preserves behavior.

## Branch and commit rules

1. One branch per group, strictly in the order above.
2. One visible screen/dialog/component per commit where practical.
3. Commit format: `ui(<group>): <action> — <screen/component>`.
4. Push only; do not open a PR.
5. Store baseline screenshot/note and branch summary per group.
6. Run applicable audit/build checks before moving on.
7. Never edit the source branch directly.
8. Never introduce tokens outside `DESIGN_SYSTEM.md`.
9. Never modify WebView, services, OCR, translation, permissions or persistence behavior as part of visual work.

## Current gate

- [x] Correct source branch verified.
- [x] Uploaded roadmap/board inventory inspected.
- [x] Codex project skill files added on working branch.
- [x] G1 component and token source files committed.
- [x] DESIGN_SYSTEM.md created.
- [~] Android Gradle build and runtime verification — not yet run in this environment.
- [ ] Finish G1 verification before starting G2.
