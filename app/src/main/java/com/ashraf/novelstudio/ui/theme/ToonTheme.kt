package com.ashraf.novelstudio.ui.theme

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.provider.Settings
import com.ashraf.novelstudio.Prefs
import com.ashraf.novelstudio.R

/**
 * Toon Sensei token source.
 *
 * Semantic colours are resource-backed so Android's values/ and values-night/
 * palettes remain the single source for both native widgets and Kotlin Views.
 */
object ToonTheme {
    data class Colors(
        val background: Int,
        val surface: Int,
        val surfaceAlt: Int,
        val ink: Int,
        val body: Int,
        val muted: Int,
        val faint: Int,
        val line: Int,
        val lineStrong: Int,
        val accent: Int,
        val accentTint: Int,
        val accentForeground: Int,
        val onAccent: Int,
        val success: Int,
        val successTint: Int,
        val successInk: Int,
        val warning: Int,
        val warningTint: Int,
        val warningLine: Int,
        val warningRing: Int,
        val danger: Int,
        val dangerTint: Int,
        val scrim: Int,
        val focus: Int,
        val ripple: Int,
        val dark: Boolean
    ) {
        val primary: Int get() = accent
        val secondary: Int get() = muted
        val error: Int get() = danger
    }

    fun colors(context: Context): Colors {
        val base = baseColors(context)
        val selected = Prefs.get(context, "uiAccentPreset", "").trim()
        val preset = AccentPreset.values().firstOrNull { it.name == selected }
        return if (preset == null) base else withAccent(base, preset)
    }

    private fun baseColors(context: Context): Colors {
        return Colors(
            background = context.getColor(R.color.toon_bg),
            surface = context.getColor(R.color.toon_surface),
            surfaceAlt = context.getColor(R.color.toon_surface_alt),
            ink = context.getColor(R.color.toon_ink),
            body = context.getColor(R.color.toon_body),
            muted = context.getColor(R.color.toon_muted),
            faint = context.getColor(R.color.toon_faint),
            line = context.getColor(R.color.toon_line),
            lineStrong = context.getColor(R.color.toon_line_strong),
            accent = context.getColor(R.color.toon_accent),
            accentTint = context.getColor(R.color.toon_accent_tint),
            accentForeground = context.getColor(R.color.toon_accent),
            onAccent = context.getColor(R.color.toon_on_accent),
            success = context.getColor(R.color.toon_success),
            successTint = context.getColor(R.color.toon_success_tint),
            successInk = context.getColor(R.color.toon_success_ink),
            warning = context.getColor(R.color.toon_warning),
            warningTint = context.getColor(R.color.toon_warning_tint),
            warningLine = context.getColor(R.color.toon_warning_line),
            warningRing = context.getColor(R.color.toon_warning_ring),
            danger = context.getColor(R.color.toon_danger),
            dangerTint = context.getColor(R.color.toon_danger_tint),
            scrim = context.getColor(R.color.toon_scrim),
            focus = context.getColor(R.color.toon_focus),
            ripple = context.getColor(R.color.toon_ripple),
            dark = isNight(context)
        )
    }

    fun isNight(context: Context): Boolean =
        (context.resources.configuration.uiMode and
            android.content.res.Configuration.UI_MODE_NIGHT_MASK) ==
            android.content.res.Configuration.UI_MODE_NIGHT_YES

    enum class AccentPreset(val argb: Int) {
        SEAL_RED(0xFF9C3D2E.toInt()),
        DEEP_TEAL(0xFF2F6B5E.toInt()),
        SLATE_BLUE(0xFF3D5A80.toInt()),
        WARM_BRONZE(0xFF8A5A12.toInt()),
        DUSK_PURPLE(0xFF6B4E8C.toInt()),
        INK(0xFF1F1D1A.toInt())
    }

    /** Apply a user-selected accent while preserving the semantic palette contract. */
    fun withAccent(base: Colors, preset: AccentPreset): Colors {
        val accent = preset.argb
        val surface = base.surface
        val tint = blend(surface, accent, if (base.dark) 0.22f else 0.12f)
        // Selected-chip labels and focus rings need contrast against their own surfaces,
        // not only against the window background. Dark-mode presets are therefore lifted
        // toward the existing ink token until the selected-fill pair reaches 4.5:1.
        val accentForeground = if (base.dark) {
            var candidate = accent
            var amount = 0.05f
            while (contrastRatio(candidate, tint) < 4.5 && amount <= 1f) {
                candidate = blend(accent, base.ink, amount)
                amount += 0.05f
            }
            candidate
        } else {
            accent
        }
        val onAccent = if (contrastRatio(accent, Color.WHITE) >=
            contrastRatio(accent, Color.BLACK)) Color.WHITE else Color.BLACK
        return base.copy(
            accent = accent,
            accentTint = tint,
            accentForeground = accentForeground,
            onAccent = onAccent,
            focus = accentForeground,
            ripple = (accent and 0x00FFFFFF) or 0x22000000
        )
    }

    private fun blend(background: Int, foreground: Int, amount: Float): Int {
        val t = amount.coerceIn(0f, 1f)
        val inverse = 1f - t
        val a = (Color.alpha(background) * inverse + Color.alpha(foreground) * t).toInt()
        val r = (Color.red(background) * inverse + Color.red(foreground) * t).toInt()
        val g = (Color.green(background) * inverse + Color.green(foreground) * t).toInt()
        val b = (Color.blue(background) * inverse + Color.blue(foreground) * t).toInt()
        return Color.argb(a, r, g, b)
    }

    private fun contrastRatio(first: Int, second: Int): Double {
        val a = luminance(first)
        val b = luminance(second)
        val light = maxOf(a, b)
        val dark = minOf(a, b)
        return (light + 0.05) / (dark + 0.05)
    }

    private fun luminance(color: Int): Double {
        fun channel(value: Int): Double {
            val scaled = value / 255.0
            return if (scaled <= 0.04045) scaled / 12.92
            else Math.pow((scaled + 0.055) / 1.055, 2.4)
        }
        return 0.2126 * channel(Color.red(color)) +
            0.7152 * channel(Color.green(color)) +
            0.0722 * channel(Color.blue(color))
    }

    object Space {
        const val xxs = 4
        const val xs = 8
        const val sm = 12
        const val md = 16
        const val lg = 20
        const val xl = 24
        const val xxl = 32
    }

    object Radius {
        const val badge = 8
        const val field = 14
        const val row = 16
        const val card = 18
        const val hero = 20
        const val sheet = 24
        const val pill = 999
    }

    object Type {
        const val micro = 11f
        const val caption = 12f
        const val meta = 13f
        const val label = 14f
        const val body = 15f
        const val bodyLarge = 17f
        const val bodyLine = 22f
        const val lineSpacingMultiplier = 1.15f
        const val reader = 19f
        const val section = 12f
        const val title = 20f
        const val titleLarge = 22f
        const val display = 26f

        fun uiTypeface(): Typeface = Typeface.create("sans-serif", Typeface.NORMAL)
        fun mediumTypeface(): Typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)

        /** Existing bundled Hind face is the offline fallback for app-owned Bengali strings. */
        fun bengaliTypeface(context: Context): Typeface = try {
            Typeface.createFromAsset(context.assets, "fonts/hind.ttf")
        } catch (_: Exception) {
            uiTypeface()
        }

        /** Existing bundled Tiro face is retained for the saved-translation reader. */
        fun readerTypeface(context: Context): Typeface = try {
            Typeface.createFromAsset(context.assets, "fonts/tiro.ttf")
        } catch (_: Exception) {
            Typeface.create("serif", Typeface.NORMAL)
        }
    }

    object Motion {
        const val pressMs = 150L
        const val standardMs = 180L
        const val exitMs = 150L
        const val maxMs = 200L

        fun enabled(context: Context): Boolean {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                return ValueAnimator.areAnimatorsEnabled()
            }
            return try {
                Settings.Global.getFloat(
                    context.contentResolver,
                    "animator_duration_scale",
                    1f
                ) > 0f
            } catch (_: Exception) {
                true
            }
        }
    }

    object Metrics {
        const val hairlineDp = 1
        const val focusStrokeDp = 2
        const val minimumTouchDp = 48
        const val badgeHeightDp = 24
        const val progressWidthDp = 144
        const val progressHeightDp = 3
        const val dialogRadiusDp = 24
        const val pressScale = 0.985f
        const val neutralScale = 1f
    }

    object Elevation {
        const val none = 0f
        const val overlay = 0f
    }

    fun dp(context: Context, value: Number): Int =
        (value.toFloat() * context.resources.displayMetrics.density + 0.5f).toInt()

    fun sp(context: Context, value: Number): Float =
        value.toFloat() * context.resources.displayMetrics.scaledDensity
}
