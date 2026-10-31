# ToonSensei Android Views project guide

This project is a native Android application implemented with Kotlin and programmatic Android Views. It is not a web UI, Jetpack Compose app, XML-layout app, or React Native app.

## Hard architecture constraints

- Preserve the existing Kotlin/View architecture.
- Do not introduce Compose, XML screen layouts, React, WebView replacements, or an alternate UI framework.
- Build reusable UI with small token-driven View factories and explicit screen builders.
- Keep WebView, OCR, translation, services, permission, navigation, persistence, and browser behavior unchanged unless the owner explicitly requests a behavior change.
- The website displayed by `novelWv` is the real novel/comic reader. Never invent an app-rendered novel reader.
- The chatbot pane is the real provider website. Never replace it with fabricated chat messages or a mock chat UI.
- Manual Lite is Real-time with Auto OFF, not a separate OCR engine.
- Never implement Hub or reintroduce a removed DeepSeek real-time UI row.

## Source-of-truth hierarchy

1. The owner's current request and explicit architecture decisions.
2. `toon-sensei-ui-spec/ROADMAP.md` for behavior, priorities, bans, button placement, and scope.
3. The matching numbered HTML board for visual appearance, labels, geometry, spacing, colors, and component order.
4. The exact target branch's current Kotlin source for actual behavior and wiring.

If the roadmap and a board conflict, report the exact disagreement instead of silently choosing.

## Design direction and banned patterns

Use the uploaded Paper & Charcoal palette, calm editorial hierarchy, restrained accent seals, flat surfaces, 1 dp rules, compact sans-serif chrome, and Bengali-capable reader/display font roles where app-owned text is involved.

Do not use gradients, blur/glass, glow, heavy elevation, shimmer, spinning rings, pulsing/blinking/bouncing dots, infinite animations, overshoot/spring motion, bolt/spark icons, emoji in chrome, bottom toasts, losable auto-dismiss notices, or bottom action bars on temporary modes.

Status must remain understandable when the chatbot WebView is hidden. Show honest progress only: real counters, elapsed seconds, and determinate progress backed by actual task state.

## Native implementation rules

- Use dp for geometry and sp for text through centralized tokens.
- Use `MaterialAlertDialogBuilder` for common dialogs, with styles from the app's selected Material Components dependency.
- Use native focus, keyboard, TalkBack, and touch semantics.
- Minimum touch target: 48 dp where the project-specific screen brief permits; never below the roadmap minimum of 44 dp.
- Every icon-only action needs a meaningful `contentDescription`.
- Support narrow phones first; do not clip long translated text, language names, URLs, or provider labels.
- Respect system remove-animations / animator duration scale.
- Animations are finite, action-triggered fade or small translation only, 150–200 ms unless the board/roadmap says otherwise.
- Keep all UI strings externalized in one resource owner; do not scatter user-facing literals across builders.
- Avoid new dependencies except those required by the approved architecture. Confirm compileSdk/AGP compatibility before changing a library version.

## Verification gate

For each screen/component:
- Build debug APK.
- Verify light/dark as applicable to the current phase.
- Verify 360 dp and wider layouts and 130% font scaling.
- Verify loading/empty/error/offline/permission states or record why a state is not applicable.
- Check contrast, focus, TalkBack descriptions, touch targets, text wrapping, and system reduced motion.
- Confirm WebView/OCR/translation interactions and callbacks remain intact.
- Record source branch, baseline screenshot/note, changed tokens, and test result.

The upstream UI/UX Pro Max catalog does not provide a programmatic Android Views stack profile. Do not select the Jetpack Compose profile as a substitute. Use its product/style/color/typography/UX/motion/icon datasets for design decisions, and use this guide plus the repository's actual Kotlin Views implementation for code-level recommendations.
