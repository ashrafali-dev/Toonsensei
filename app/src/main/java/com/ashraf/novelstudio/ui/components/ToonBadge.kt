package com.ashraf.novelstudio.ui.components

import android.content.Context
import android.graphics.Color
import android.widget.TextView
import android.view.Gravity
import com.ashraf.novelstudio.ui.theme.ToonTheme

enum class ToonBadgeTone { NEUTRAL, ACCENT, SUCCESS, WARNING, ERROR }

object ToonBadge {
    fun create(
        context: Context,
        label: CharSequence,
        tone: ToonBadgeTone = ToonBadgeTone.NEUTRAL
    ): TextView {
        val colors = ToonTheme.colors(context)
        val (fill, ink, stroke) = when (tone) {
            ToonBadgeTone.NEUTRAL -> Triple(colors.surfaceAlt, colors.body, colors.line)
            ToonBadgeTone.ACCENT -> Triple(colors.accentTint, colors.accentForeground, colors.accentForeground)
            ToonBadgeTone.SUCCESS -> Triple(colors.successTint, colors.successInk, colors.success)
            ToonBadgeTone.WARNING -> Triple(colors.warningTint, colors.warning, colors.warningLine)
            ToonBadgeTone.ERROR -> Triple(colors.dangerTint, colors.danger, colors.danger)
        }
        return TextView(context).apply {
            text = label
            textSize = ToonTheme.Type.micro
            typeface = ToonTheme.Type.mediumTypeface()
            setTextColor(ink)
            gravity = Gravity.CENTER
            includeFontPadding = false
            minHeight = ToonTheme.dp(context, ToonTheme.Metrics.badgeHeightDp)
            setPadding(
                ToonTheme.dp(context, ToonTheme.Space.xs),
                ToonTheme.dp(context, ToonTheme.Space.xxs),
                ToonTheme.dp(context, ToonTheme.Space.xs),
                ToonTheme.dp(context, ToonTheme.Space.xxs)
            )
            background = ToonComponents.rounded(
                context,
                fill,
                ToonTheme.Radius.badge,
                if (stroke == Color.TRANSPARENT) Color.TRANSPARENT else stroke
            )
            isFocusable = false
            isClickable = false
        }
    }
}
