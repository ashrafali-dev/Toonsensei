# G1 Branch Summary — Theme and Components

- Source branch: `Adding-stop-on-scanning`
- Source base SHA: `2e200b1d80b66fc0e09283f7b3dc0a934b09ca57`
- Working branch: `ui/g1-theme-and-components`
- Branch HEAD at summary creation: `ae8653add036d9482e11ba9766505399594525a2`
- Pull request: not created (per owner instruction).

## What changed

### Codex UI/UX skill

Installed the UI/UX Pro Max v2.5.0 project skill in `.agents/skills/ui-ux-pro-max/` using the Codex project-skill layout. Includes:

- `SKILL.md`, official MIT license, install notes.
- Search engine and design-system generator scripts.
- Design reasoning contract and UX / style / color / typography / motion / icon / product / chart / landing / app-interface catalogs.
- 22 stack catalogs.
- ToonSensei-specific Android Views guidance in `references/toonsensei-android-views.md`.

Codex CLI is not present in the current execution environment. The project skill files are committed and use Codex's repository discovery path; activation must be confirmed when this branch is opened in the owner's Codex client.

### Design tokens

- Paper light palette and Charcoal dark palette in Android color resources.
- Semantic color roles: background, surfaces, ink, body, muted, line, accent, success, warning, warning ring, danger, scrim, focus, ripple.
- Accent presets: Seal Red, Deep Teal, Slate Blue, Warm Bronze, Dusk Purple, Ink.
- Typography tokens 11–26 sp; existing Hind/Tiro bundled files used as offline fallbacks.
- 4 dp spacing scale; 8/14/16/18/20/24/pill radius tokens.
- 0 dp elevation.
- 150/180/150 ms finite motion tokens, system animation-disable support.
- Shared component-owned labels centralized in `strings.xml`.

### Components

Added:
`ToonComponents`, `ToonButton`, `ToonChip`, `ToonBadge`, `ToonCard`, `ToonInput`, `ToonDialog`, `ToonBottomSheet`, `ToonToast` (persistent in-layout notice only), and `ToonStateView`.

`ToonDialog` uses `MaterialAlertDialogBuilder`; `ToonBottomSheet` uses `BottomSheetDialog`.

### Android resources / dependency

- Added `res/values/colors.xml` and `res/values-night/colors.xml`.
- Replaced the old Glass theme resource with a Paper/Charcoal Material Components DayNight theme and dialog/sheet overlays.
- Added Material Components `1.12.0` while keeping project AGP 8.2.2, compileSdk 34, minSdk 24 and Java 17 unchanged.

## Functional scope protection

Not modified in G1:
- `MainActivity.kt`, `Home.kt`, `ModeUi.kt`, `ReaderActivity.kt`.
- WebView lifecycle, OCR, translation, services, permission behavior, navigation and persistence callbacks.

MainActivity remains at the source baseline of 11,538 lines. No screen migration has started.

## Token references

See root `DESIGN_SYSTEM.md`. It records the supplied ROADMAP palette, semantic token map, typography, spacing, radius, motion, component contracts, notification classes and quality gate.

## Audit and verification

| Check | Result |
|---|---|
| Source branch and base verified | PASS |
| Codex skill files present under `.agents/skills` | PASS |
| UIUX Pro Max asset set and local data present | PASS |
| Paper/Charcoal resources added | PASS |
| MainActivity / WebView / OCR / translation untouched | PASS |
| G1 Kotlin compile | NOT RUN |
| Android resource merge / Gradle build | NOT RUN |
| 360 dp / 412 dp device layout | NOT RUN |
| Light/dark device render | NOT RUN |
| TalkBack / keyboard / focus / font-scale 130% | NOT RUN |
| Vercel Web Design Guidelines audit | NOT RUN |
| Baseline component screenshots | PENDING |

The repository's Build APK workflow does not auto-run for this branch; its push trigger is limited to main and listed maintenance branches. The connected GitHub tools in this session expose read/write file and ref operations, but not workflow_dispatch. The source tree was not available in a local Gradle workspace, so a build must not be reported as passing.

## Remaining before G1 can close

1. Run `gradle assembleDebug` or the repository's Build APK workflow manually for this branch.
2. Fix any Kotlin, Android resource, Material theme, or dependency issues found.
3. Verify Paper/Charcoal, 360 dp/412 dp, system text scale, focus, TalkBack and touch targets on device.
4. Capture component screenshots / notes.
5. Complete the applicable accessibility and motion audit. Mark Vercel web-only checks N/A rather than claiming a web audit passed for native Android.

G2 must not start until the G1 build and owner/device verification gate is satisfied.
