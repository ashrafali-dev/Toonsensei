package com.ashraf.novelstudio

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color

/**
 * ToonSensei design system — "Paper & Seal".
 *
 * The single source of truth for colour, type, spacing, radius and motion. See
 * docs/redesign/DESIGN.md for the specification and the measured contrast table.
 * Screens must not hard-code colours; read them from [ToonTheme.colors].
 */
object ToonTheme {

    /** Whether the app chrome is drawn on the charcoal palette. */
    fun dark(ctx: Context): Boolean = (ctx.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
        Configuration.UI_MODE_NIGHT_YES

    fun colors(ctx: Context): ToonColors = if (dark(ctx)) night else day

    // ------------------------------------------------------------------ colours

    /** Day palette: warm paper. */
    val day = ToonColors(
        dark = false,
        bg = 0xFFF7F4EE.toInt(),
        surface = 0xFFFBFAF6.toInt(),
        surfaceAlt = 0xFFE9E4D8.toInt(),
        line = 0xFFE3DED3.toInt(),
        lineStrong = 0xFFCFC8BA.toInt(),
        title = 0xFF1F1D1A.toInt(),
        body = 0xFF3D3832.toInt(),
        text = 0xFF4F4A44.toInt(),
        subtext = 0xFF5A544B.toInt(),
        muted = 0xFF6B665E.toInt(),
        disabled = 0xFF8F897D.toInt(),
        accent = 0xFF2C6650.toInt(),
        onAccent = 0xFFFFFFFF.toInt(),
        novel = 0xFF9C3D2E.toInt(),
        scan = 0xFF2E6B47.toInt(),
        live = 0xFF8C2C1D.toInt(),
        manual = 0xFF1A4FB4.toInt(),
        success = 0xFF2C6650.toInt(),
        warnBg = 0xFFF6E8C9.toInt(),
        warnInk = 0xFF5E3F0B.toInt(),
        dangerBg = 0xFFF5E3DE.toInt(),
        dangerInk = 0xFFA33A28.toInt(),
        infoBg = 0xFFE6ECF8.toInt(),
        infoInk = 0xFF1A4FB4.toInt(),
        ink = 0xFF1F1D1A.toInt(),
        onInk = 0xFFF7F4EE.toInt(),
        scrim = 0x66000000,
        skeleton = 0xFFEDE8DD.toInt()
    )

    /** Night palette: the same book after dark. */
    val night = ToonColors(
        dark = true,
        bg = 0xFF121211.toInt(),
        surface = 0xFF1B1B1A.toInt(),
        surfaceAlt = 0xFF242322.toInt(),
        line = 0xFF2E2C29.toInt(),
        lineStrong = 0xFF3C3934.toInt(),
        title = 0xFFE9E5DC.toInt(),
        body = 0xFFE8E3D9.toInt(),
        text = 0xFFC9C3B6.toInt(),
        subtext = 0xFFBDB7AA.toInt(),
        muted = 0xFF9B9488.toInt(),
        disabled = 0xFF7A7469.toInt(),
        accent = 0xFFE07A5F.toInt(),
        onAccent = 0xFF121211.toInt(),
        novel = 0xFFE8A08E.toInt(),
        scan = 0xFF9CC5A1.toInt(),
        live = 0xFFFF8A75.toInt(),
        manual = 0xFF7FA6F0.toInt(),
        success = 0xFF9FE0B5.toInt(),
        warnBg = 0xFF2A2213.toInt(),
        warnInk = 0xFFF0D6A8.toInt(),
        dangerBg = 0xFF2A1C18.toInt(),
        dangerInk = 0xFFF2CFC4.toInt(),
        infoBg = 0xFF1A2333.toInt(),
        infoInk = 0xFFA9C4F5.toInt(),
        ink = 0xFFE9E5DC.toInt(),
        onInk = 0xFF121211.toInt(),
        scrim = 0x99000000,
        skeleton = 0xFF262422.toInt()
    )

    // ------------------------------------------------------------------ modes

    /** Index order matches ModeUi / ModeCapsule: 0 Novel, 1 Scan, 2 Live, 3 Manual Lite. */
    val modeNames = arrayOf("Novel", "Scanning", "Real-time", "Manual Lite")

    fun modeColor(c: ToonColors, i: Int): Int = when (i) {
        0 -> c.novel
        1 -> c.scan
        2 -> c.live
        else -> c.manual
    }

    fun modeDescription(i: Int): String = when (i) {
        0 -> "Reads the chapter text and translates it on the page."
        1 -> "Prepare a whole comic chapter."
        2 -> "Translate as you scroll."
        else -> "Read one screen when you tap. Light on battery and RAM."
    }

    // ------------------------------------------------------------------ type (sp)

    object Type {
        const val display = 26f
        const val title = 20f
        const val section = 12f
        const val body = 15f
        const val label = 14f
        const val caption = 13f
        const val micro = 11f
        const val reader = 20f
        const val readerMetric = 17f
    }

    const val SECTION_TRACK = 0.08f

    // ------------------------------------------------------------------ space (dp)

    object Space {
        const val s1 = 4
        const val s2 = 8
        const val s3 = 12
        const val s4 = 16
        const val s5 = 20
        const val s6 = 24
        const val s8 = 32
        /** Screen side margin; 16 on ≥ 390 dp, 20 on 360 dp. */
        fun edge(ctx: Context): Int = if (ctx.resources.displayMetrics.widthPixels / ctx.resources.displayMetrics.density < 390f) s5 else s4
    }

    // ------------------------------------------------------------------ radius (dp)

    object Radius {
        const val field = 12
        const val badge = 8
        const val card = 16
        const val group = 16
        const val sheet = 22
        const val dialog = 20
    }

    // ------------------------------------------------------------------ motion (ms)

    object Motion {
        const val fast = 110L
        const val base = 220L
        const val slow = 260L
        const val stagger = 40L
        const val spring = 1.4f
    }

    // ------------------------------------------------------------------ helpers

    /** [c] with its alpha replaced by [a] (0..255). */
    fun withAlpha(c: Int, a: Int): Int = (c and 0x00FFFFFF) or (a.coerceIn(0, 255) shl 24)

    /** Blend two ARGB colours; [t] 0 → [a], 1 → [b]. */
    fun lerp(a: Int, b: Int, t: Float): Int {
        val k = t.coerceIn(0f, 1f)
        return Color.argb(
            (Color.alpha(a) + (Color.alpha(b) - Color.alpha(a)) * k).toInt(),
            (Color.red(a) + (Color.red(b) - Color.red(a)) * k).toInt(),
            (Color.green(a) + (Color.green(b) - Color.green(a)) * k).toInt(),
            (Color.blue(a) + (Color.blue(b) - Color.blue(a)) * k).toInt()
        )
    }
}

/** Immutable palette record; one instance per mode (see [ToonTheme.day] / [ToonTheme.night]). */
data class ToonColors(
    val dark: Boolean,
    val bg: Int,
    val surface: Int,
    val surfaceAlt: Int,
    val line: Int,
    val lineStrong: Int,
    val title: Int,
    val body: Int,
    val text: Int,
    val subtext: Int,
    val muted: Int,
    val disabled: Int,
    val accent: Int,
    val onAccent: Int,
    val novel: Int,
    val scan: Int,
    val live: Int,
    val manual: Int,
    val success: Int,
    val warnBg: Int,
    val warnInk: Int,
    val dangerBg: Int,
    val dangerInk: Int,
    val infoBg: Int,
    val infoInk: Int,
    val ink: Int,
    val onInk: Int,
    val scrim: Int,
    val skeleton: Int
)
