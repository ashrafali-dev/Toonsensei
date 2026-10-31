package com.ashraf.novelstudio.ui.theme

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Typeface
import android.os.Build
import android.provider.Settings
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
        val onAccent: Int,
        val success: Int,
        val successTint: Int,
        val successInk: Int,
        val warning: Int,
        val warningTint: Int,
        val warningLine: Int,
        val danger: Int,
        val dangerTint: Int,
        val scrim: Int,
        val focus: Int,
        val ripple: Int,
        val dark: Boolean
    )

    fun colors(context: Context): Colors {
        val night = context.getColor(R.color.toon_bg)
        return Colors(
            background = night,
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
            onAccent = context.getColor(R.color.toon_on_accent),
            success = context.getColor(R.color.toon_success),
            successTint = context.getColor(R.color.toon_success_tint),
            successInk = context.getColor(R.color.toon_success_ink),
            warning = context.getColor(R.color.toon_warning),
            warningTint = context.getColor(R.color.toon_warning_tint),
            warningLine = context.getColor(R.color.toon_warning_line),
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
        const val bodyLine = 22f
        const val reader = 19f
        const val section = 12f
        const val title = 20f
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

    object Elevation {
        const val none = 0f
        const val overlay = 0f
    }

    fun dp(context: Context, value: Number): Int =
        (value.toFloat() * context.resources.displayMetrics.density + 0.5f).toInt()

    fun sp(context: Context, value: Number): Float =
        value.toFloat() * context.resources.displayMetrics.scaledDensity
}
