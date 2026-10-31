# G1 Branch Summary — Theme and Components

- Source branch: `Adding-stop-on-scanning`
- Source base SHA: `2e200b1d80b66fc0e09283f7b3dc0a934b09ca57`
- Working branch: `ui/g1-theme-and-components`
- Branch HEAD after workflow cleanup: `6cf8dbd9a69c06e1deb2fa2b0beddc17c165a546`
- Final tested code snapshot: `1d95eca322ed871b4f15e05ee89fae30bb9ec868`
- Pull request: not created (per owner instruction).

## Codex UI/UX Pro Max

Installed UI/UX Pro Max v2.5.0 as a repository-scoped Codex skill in `.agents/skills/ui-ux-pro-max/`.

- 45 files total: Codex `SKILL.md`, MIT license, installation notes, ToonSensei Android Views guide, search and design-system scripts, references, and catalog data.
- 4 Python scripts, 35 CSV catalogs, including 22 stack-specific catalog files.
- Codex CLI is not present in this execution environment. Files follow the project skill discovery path; confirm skill discovery by opening this branch in the owner's Codex client and refreshing/restarting its skill list.
- No claim is made that the other three external skills (Anthropic Frontend Design, Vercel web guidelines, Emil/Impeccable) were installed.

## Design and components added

- Paper light and Charcoal dark semantic token resources.
- Semantic status roles: background, surfaces, ink, body, muted, faint, line, strong control edge, accent, success, warning, warning ring, danger, scrim, focus, ripple.
- Accent presets: Seal Red, Deep Teal, Slate Blue, Warm Bronze, Dusk Purple, Ink. Dark-mode selected-chip/focus foreground is derived to maintain at least 4.5:1 against selected tint; raw accent remains the primary CTA fill.
- Typography scale 11–26 sp, Bengali reader 19 sp / 2.05 line-spacing token. Existing Hind and Tiro assets are the verified offline fallback; Inter, Noto Serif Bengali and Shippori Mincho are not present in the source tree and are not claimed as bundled.
- Spacing 4/8/12/16/20/24/32 dp; radius 8/14/16/18/20/24/pill; zero elevation.
- Finite 150/180 ms motion tokens and system animation-scale support. G1 buttons use ripple only; no scale/spring motion.
- Components: `ToonComponents`, `ToonButton`, `ToonChip`, `ToonBadge`, `ToonCard`, `ToonInput`, `ToonDialog`, `ToonBottomSheet`, `ToonToast` (persistent in-layout notice only), `ToonStateView`.
- Material Components `1.12.0` and themed MaterialAlertDialog / BottomSheet overlays.
- Root `DESIGN_SYSTEM.md` and live `TODO.md`.

## Functional scope protection

The five principal legacy UI files are byte-identical to the source branch:
- `MainActivity.kt` — 11,538 lines.
- `Ui.kt` — 608 lines.
- `Home.kt` — 596 lines.
- `ModeUi.kt` — 482 lines.
- `ReaderActivity.kt` — 182 lines.

No screen migration has started. WebView, OCR, translation, service, permission, browser, and persistence behavior was not changed by G1.

## Automated checks

| Check | Result |
|---|---|
| Codex skill file inventory / project path | PASS |
| Paper / Charcoal resources merged | PASS |
| Kotlin Release build | PASS |
| Android resource processing | PASS |
| Permanent signing key validation | PASS |
| APK package and signing certificate verification | PASS |
| Signed APK artifact upload | PASS |
| Static scan: 11 G1 Kotlin files for hardcoded hex / dp / numeric text size | 0 matches |
| Text contrast calculations on supplied palette | PASS for documented pairs |
| Control edge contrast | 3.1:1+ Paper and Charcoal |
| Interactive focus helpers / 48 dp minimum contracts | Implemented; runtime verification pending |
| 360 dp / 412 dp device layout | PENDING |
| Light / Dark rendering on device | PENDING |
| TalkBack / keyboard / 130% font scale | PENDING |
| Baseline component screenshots | PENDING |
| Codex skill activation in owner's local Codex client | PENDING |
| Vercel Web Design Guidelines skill installation / full audit | NOT DONE; native-only audit needs native checks, not a false web-audit pass |

Final signed Release build:
- Run: https://github.com/ashrafali-dev/ToonSensei/actions/runs/41217011068
- Result: success (Release build, signing certificate verification, artifact upload).
- Artifact: `NovelStudio-SIGNED-RELEASE-APK`, 23,037,797 bytes, id `13048647988`; available until 2026-11-30.
- The build workflow's original push branch list was restored after the manual test run; no permanent branch trigger was added.

## G1 exit decision

**Code and signed Release build: PASS. G1 is not fully closed for physical UX verification.**

The current environment has no Android emulator/device UI, so no honest claim can be made for real 360/412 dp rendering, TalkBack, system font scaling, or screenshots. Keep G2 blocked until the owner checks this branch on a device and confirms the Codex project skill appears in the Codex client. No PR or release was created.
