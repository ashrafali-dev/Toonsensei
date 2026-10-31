# ToonSensei Design System

Version: 1.0.0-draft
Source branch: `Adding-stop-on-scanning`
Implementation branch: `ui/g1-theme-and-components`
Platform: Android / Kotlin / programmatic Views / WebView
Status: G1 foundation added; build and device audit pending.

This document is the single source of truth for app-owned UI tokens and component anatomy. It is grounded in the supplied `toon-sensei-ui-spec/ROADMAP.md` and its numbered HTML boards. The target branch's existing Kotlin code remains the authority for behavior and callbacks.

## 1. Product and implementation boundaries

ToonSensei is a WebView-based browser/translation tool. `novelWv` displays the actual comic/novel website; `chatWv` displays the actual AI provider website. The app chrome controls these existing web surfaces.

- Do not build a second, app-rendered novel/comic reader.
- Do not build mock chat messages or replace chatbot websites with an invented native chat UI.
- Keep both WebViews alive when a pane is hidden; hiding is not destruction.
- Preserve existing extraction, OCR, translation, services, browser, permissions, and persistence behavior.
- Manual Lite is Real-time with Auto OFF; it is not a fifth OCR engine.
- Keep the four user modes: Novel, Scanning, Real-time, Manual Lite.
- Ignore the retired Hub and do not reintroduce its UI.
- Keep removed/retired providers out of the UI without deleting their underlying implementation unless explicitly requested.
- No Compose migration and no XML screen layouts. XML values/styles resources are allowed for centralized Android theme tokens.

### Reference hierarchy

1. Owner's explicit architecture and feature constraints.
2. Supplied `ROADMAP.md` for behavior, priorities, motion restrictions, and interaction rules.
3. Numbered HTML board for visual anatomy, spacing, color, labels, and component order.
4. Current source branch for existing logic, data, callbacks, and lifecycle.

Where these disagree, document the conflict; do not silently invent behavior.

## 2. Visual direction

**Paper by day, Charcoal by night.** Use quiet editorial surfaces, clear text hierarchy, hairline dividers, and one restrained accent seal. The content website remains visually controlled by that website; the design system applies to ToonSensei's native chrome and saved-content screens only.

UI/UX Pro Max's relevant guidance supports a restrained, high-contrast, mobile-first layout. Its generic palette recommendations are not a replacement for the owner's supplied palette. The current catalog has no exact manga-reader product profile; the custom Paper/Charcoal palette therefore takes precedence.

### Avoid

- Gradients, glassmorphism, blur, glow, heavy shadows, decorative shimmer.
- Spinning rings, infinite decoration, blinking/pulsing/bouncing dots, spring/overshoot motion.
- Bolt or spark icons and emoji in app chrome.
- Bottom Android Toasts, auto-disappearing notices, or bottom action bars for temporary modes.
- Fake loading percentages or success states that do not reflect real task state.
- Styling the real website page or chatbot content as if it were app-owned UI.

## 3. Color tokens

All screen code reads semantic resources through `ToonTheme.Colors`. The light and night resource tables contain the palette values; no raw color literals belong in screen builders.

### 3.1 Paper — light

| Semantic token | Value | Purpose |
|---|---|---|
| `toon_bg` | `#F4F1EA` | Window / page background |
| `toon_surface` | `#FBFAF6` | Card, sheet, row surface |
| `toon_surface_alt` | `#E9E4D8` | Secondary surface / input fill |
| `toon_ink` | `#1F1D1A` | Main heading and primary text |
| `toon_body` | `#1F1D1A` | Body text |
| `toon_muted` | `#6B665E` | Secondary text |
| `toon_faint` | `#6B665E` | Non-essential metadata |
| `toon_line` | `#E3DED3` | Hairline divider |
| `toon_line_strong` | `#CFC8BA` | Control edge |
| `toon_accent` | `#9C3D2E` | Active mode, focus, main CTA |
| `toon_accent_tint` | `#F0E1DB` | Selected/soft accent surface |
| `toon_on_accent` | `#FFFFFF` | Text on accent |
| `toon_success` | `#3F6B4B` | Success icon/marker |
| `toon_success_tint` | `#E3EDE5` | Success surface |
| `toon_success_ink` | `#2F5A3B` | Success text |
| `toon_warning` | `#5E3F0B` | Warning text |
| `toon_warning_tint` | `#F5ECD8` | Warning surface |
| `toon_warning_line` | `#E3CFA3` | Warning edge |
| `toon_warning_ring` | `#B7791F` | Warning state on the Chat control |
| `toon_danger` | `#9C3D2E` | Destructive/error action |
| `toon_danger_tint` | `#F0E1DB` | Destructive soft surface |
| `toon_scrim` | `#571F1D1A` | Modal backdrop |
| `toon_focus` | `#9C3D2E` | 2 dp visible focus ring |
| `toon_ripple` | `#229C3D2E` | Press feedback |

### 3.2 Charcoal — dark

| Semantic token | Value | Purpose |
|---|---|---|
| `toon_bg` | `#1C1B19` | Window / page background |
| `toon_surface` | `#262522` | Card, sheet, row surface |
| `toon_surface_alt` | `#302F2B` | Secondary surface / input fill |
| `toon_ink` | `#E8E4DA` | Main heading and primary text |
| `toon_body` | `#E8E4DA` | Body text |
| `toon_muted` | `#9A958A` | Secondary text |
| `toon_faint` | `#6F6A60` | Non-essential metadata only |
| `toon_line` | `#2EE8E4DA` | Subtle hairline |
| `toon_line_strong` | `#45E8E4DA` | Control edge |
| `toon_accent` | `#C9AE7C` | Active mode, focus, main CTA |
| `toon_accent_tint` | `#3A3328` | Selected/soft accent surface |
| `toon_on_accent` | `#1C1B19` | Text on accent |
| `toon_success` | `#7FB08A` | Success marker |
| `toon_success_tint` | `#26352B` | Success surface |
| `toon_success_ink` | `#A4C9AA` | Success text |
| `toon_warning` | `#C9AE7C` | Warning text |
| `toon_warning_tint` | `#3A3328` | Warning surface |
| `toon_warning_line` | `#6B5F46` | Warning edge |
| `toon_warning_ring` | `#C9AE7C` | Warning state on the Chat control |
| `toon_danger` | `#E5826F` | Destructive/error action |
| `toon_danger_tint` | `#432923` | Destructive soft surface |
| `toon_scrim` | `#991C1B19` | Modal backdrop |
| `toon_focus` | `#C9AE7C` | 2 dp visible focus ring |
| `toon_ripple` | `#33E8E4DA` | Press feedback |

### 3.3 Accent presets

Default follows the base palette (Seal Red in Paper, warm bronze in Charcoal). Appearance settings may select one of these presets; the token factory derives an appropriate foreground, tint, focus ring, and ripple from the selected accent. The token reader checks the saved `uiAccentPreset` preference; an empty value retains the theme default.

| Preset | Hex |
|---|---|
| Seal Red | `#9C3D2E` |
| Deep Teal | `#2F6B5E` |
| Slate Blue | `#3D5A80` |
| Warm Bronze | `#8A5A12` |
| Dusk Purple | `#6B4E8C` |
| Ink | `#1F1D1A` |

Mode identity uses these accents unless the exact mode board specifies otherwise. Do not introduce ad-hoc hues.

### 3.4 Contrast use

Measured against the supplied base palette:
- Paper ink on background: 14.91:1.
- Paper muted on background: 5.05:1.
- Paper accent on white: 6.73:1.
- Paper warning ink on warning tint: 8.15:1.
- Charcoal ink on background: 13.56:1.
- Charcoal muted on surface: 5.14:1.
- Charcoal accent on its on-color: 8.06:1.
- Charcoal danger on background: 6.35:1.

The dark `toon_faint` color is about 3.2:1 against the dark background. It is reserved for non-essential metadata/decorative use and must not carry an instruction, error, label, or status by itself. The translucent line tokens are for non-interactive separators; use the accent focus token for active/focused controls.

## 4. Typography

The app owns only its own chrome and saved-content UI. Do not inject these typefaces into website pages.

| Token | Size | Weight | Face / purpose |
|---|---:|---|---|
| `micro` | 11 sp | 500 | Small count / compact badge |
| `caption` | 12 sp | 400 | Supporting detail |
| `meta` | 13 sp | 400 | Secondary metadata |
| `label` | 14 sp | 500 | Buttons and compact controls |
| `body` | 15 sp | 400 | Default app text |
| `bodyLarge` | 17 sp | 400 | Emphasized / high-readability body |
| `bodyLine` | 22 sp | — | Body baseline line-height target |
| `reader` | 19 sp | 400 | Saved translation reading |
| `title` | 20 sp | 500 | Screen and sheet titles |
| `titleLarge` | 22 sp | 500 | Major heading |
| `display` | 26 sp | 600 | Wordmark / primary editorial heading |

Font rules:
- App chrome uses the Android system sans family until the approved Inter font asset is bundled.
- Bengali app-owned text uses the bundled Hind face as an offline-compatible fallback.
- Saved translation reading may use the bundled Tiro serif face where appropriate.
- The existing Galada face is reserved for decorative brand use, not dense interface labels.
- Do not introduce an unlicensed or network-only font dependency.
- Do not use all-caps for long labels; small uppercase section markers are limited to short labels.
- Use sp for every text size. Never set user-facing text size in raw px.

## 5. Spacing, radius, stroke, elevation

### Spacing tokens (dp)

| Token | dp |
|---|---:|
| `xxs` | 4 |
| `xs` | 8 |
| `sm` | 12 |
| `md` | 16 |
| `lg` | 20 |
| `xl` | 24 |
| `xxl` | 32 |

### Radius tokens (dp)

| Token | dp | Use |
|---|---:|---|
| `badge` | 8 | Count / compact tag |
| `field` | 14 | Input |
| `row` | 16 | List grouping |
| `card` | 18 | Standard surface |
| `hero` | 20 | Large featured surface |
| `sheet` | 24 | Bottom-sheet top corners |
| `pill` | height-based | Buttons and chips |

- Stroke: 1 dp neutral border; 2 dp accent focus ring.
- Elevation: 0 dp for app-owned surfaces; use a scrim and border for modal separation.
- Interactive targets: at least 48 × 48 dp in the base components; never below the roadmap's 44 dp minimum.
- Keep 8 dp spacing between adjacent touch targets.

## 6. Motion

Only motion that communicates a user action or state transition is allowed.

| Token | Duration | Use |
|---|---:|---|
| `pressMs` | 150 ms | Button press feedback |
| `standardMs` | 180 ms | Finite fade / small translation |
| `exitMs` | 150 ms | Dismiss / return |
| `maxMs` | 200 ms | Hard cap for G1 motion |

Allowed: subtle scale/opacity or small translation on direct user interaction, easing out, with the final state rendered immediately when system animations are disabled.

Banned: infinite animation, spring/overshoot, pulse/blink/bounce, shimmer, spinning progress decoration, animated layout dimensions, and movement that is not backed by real work. Real task progress may update only when actual task state changes.

## 7. Component contracts

All new app-owned UI uses these factories. A screen may compose them but may not introduce a private color, type, spacing, radius, or motion value. Component-owned default labels are in `res/values/strings.xml`; screen text moves there as its screen is extracted.

| Component | Contract |
|---|---|
| `ToonButton` | Primary, secondary, ghost, danger; compact/regular; disabled and press states; 48 dp touch height; icon-only use requires an accessibility label |
| `ToonChip` | Selected/unselected/disabled; semantic selected fill; no emoji icons |
| `ToonCard` | Flat surface, line border, no elevation; clickable card requires accessibility label |
| `ToonInput` | Visible label, hint, focused border, keyboard input type, single/multiline |
| `ToonDialog` | MaterialAlertDialogBuilder wrapper; title/body/actions; destructive color; 24 dp shape |
| `ToonBottomSheet` | Material BottomSheetDialog wrapper; 24 dp top radius; safe dismiss / full-height option |
| `ToonToast` | Compatibility name only: persistent in-layout notice, never Android bottom Toast; explicit dismiss/action |
| `ToonStateView` | One state component for loading, empty, error, offline, permission |
| `ToonComponents` | Shared shape/ripple/text/focus/touch primitives |

Dialog action anatomy: 48 dp minimum actions, one clear primary action, one outline/secondary cancel where applicable. Choice-heavy actions use a bottom sheet rather than deep nested dialogs. Destructive confirmation must name the item/action clearly.

### Notification classes

- Action confirmation: update the pressed control in place; no popup and no auto-dismiss.
- Background-tab update: update the tab count.
- User intervention: a fixed top banner under the app bar, with explicit action and close; it remains until resolved/dismissed. One at a time.
- Information: append to the Tasks log.
- Progress narration: update the shared Status/Tasks model, not transient toasts.

## 8. Navigation and persistent chrome

- Keep the site WebView as the primary reader surface.
- Top bar: 56 dp; back, URL pill/reload-or-stop, tab counter, more. Hide on scroll; hide it in Split.
- Dock: about 66 dp; mode chip, previous, main mode action, next/ready check, chatbot ring.
- Mode chip opens the mode sheet. Chat tap cycles pane size; Chat long-press opens Tasks.
- Chatbot pane size cycles through the roadmap's peek/half/full states. Hiding it never destroys its WebView.
- Immersive reading hides normal bars; retain only the specified 3 dp top progress and a 56 dp return-to-controls affordance.
- Temporary-mode tools (Re-scan, chapter glossary) sit at the right above the dock; no bottom action bar.

## 9. State coverage

Every applicable screen must account for:
- Loading (truthful progress only)
- Empty (what is absent and how to create/populate it)
- Error (what failed and a recover/retry action)
- Offline / slow connection (where network-backed work is involved)
- Permission (where Android permission gates a feature)

A state may be recorded as not applicable only with a reason. Do not invent progress percentages; show real task counters, elapsed seconds, or task stages only where the underlying state exposes them.

## 10. Quality gate

A component/screen is not done until:
1. Light and dark palettes are checked.
2. 360 dp and at least 412 dp widths are checked; text scaling is checked.
3. Text contrast is at least 4.5:1; interactive strokes/focus must remain distinguishable. Document any non-text hairline exception.
4. Focus is visible for keyboard and TalkBack users.
5. Icon-only controls have meaningful `contentDescription`; controls expose correct selected/disabled state.
6. Touch targets are at least 48 dp where possible and never below 44 dp.
7. Motion is action-triggered, finite, and respects system animation settings.
8. Typography, colors, spacing, radii, borders, and motion use tokens only.
9. Loading/empty/error/offline/permission states are present or explicitly N/A.
10. Browser, WebView, OCR, translation, permissions, and services behave as before.
11. Build/test output and baseline screenshot/note are recorded for the current screen.

## 11. G1 implementation files

- `ui/theme/ToonTheme.kt` — semantic colors, accent presets, typography, spacing, radii, motion, elevation.
- `ui/components/ToonComponents.kt` — base shapes, ripples, text, focus, touch.
- `ui/components/ToonButton.kt`
- `ui/components/ToonChip.kt`
- `ui/components/ToonBadge.kt`
- `ui/components/ToonCard.kt`
- `ui/components/ToonInput.kt`
- `ui/components/ToonDialog.kt`
- `ui/components/ToonBottomSheet.kt`
- `ui/components/ToonToast.kt` — persistent inline notice shim only.
- `ui/components/ToonStateView.kt`
- `res/values/colors.xml` and `res/values-night/colors.xml` — Paper / Charcoal palettes.
- `res/values/styles.xml` and `res/values-night/styles.xml` — Material Components theme overlays.

G1 does not migrate existing `AlertDialog.Builder` call sites, restyle screens, or alter MainActivity. Those tasks belong to later groups.
