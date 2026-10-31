package com.ashraf.novelstudio

/**
 * Legacy names for the "Aurora Glass" palette.
 *
 * Kept so existing call sites compile unchanged while screens migrate to [ToonTheme] one group at a
 * time. They hold **no colours** themselves: every holder reads the current [ToonTheme] palette, so a
 * screen that has not been redesigned yet still picks up the Paper & Seal colours. Do not add new uses;
 * new code reads [ToonTheme.colors] directly.
 */
object UiTheme {
    /** Mirror of [Glass] kept for `UiTheme.accentDefault`-style helpers. */
    val palette: ToonColors get() = ToonTheme.day

    val bg: Int get() = palette.bg
    val sheet: Int get() = palette.surface
    val text: Int get() = palette.title
    val dim: Int get() = palette.muted
    val faint: Int get() = palette.muted
    val iconIdle: Int get() = palette.text
    val accentA: Int get() = palette.accent
    val accentB: Int get() = palette.accent
    val accent: Int get() = palette.accent
    val ok: Int get() = palette.success
    val danger: Int get() = palette.dangerInk
    val stroke: Int get() = (ToonTheme.withAlpha(palette.title, 0x2E))
    val strokeSoft: Int get() = (ToonTheme.withAlpha(palette.title, 0x1A))
}
