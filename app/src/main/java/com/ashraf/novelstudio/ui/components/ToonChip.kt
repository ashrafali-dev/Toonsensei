package com.ashraf.novelstudio.ui.components

import android.content.Context
import android.graphics.Color
import android.widget.TextView
import android.view.Gravity
import com.ashraf.novelstudio.ui.theme.ToonTheme

object ToonChip {
    fun create(
        context: Context,
        label: CharSequence,
        selected: Boolean = false,
        enabled: Boolean = true,
        onClick: (() -> Unit)? = null
    ): TextView {
        val colors = ToonTheme.colors(context)
        val fill = if (selected) colors.accentTint else colors.surfaceAlt
        val ink = if (selected) colors.accentForeground else colors.body
        val stroke = if (selected) colors.accentForeground else colors.line
        val shape = ToonComponents.rounded(context, fill, ToonTheme.Radius.pill, stroke)
        return TextView(context).apply {
            text = label
            textSize = ToonTheme.Type.caption
            typeface = ToonTheme.Type.mediumTypeface()
            setTextColor(ink)
            gravity = Gravity.CENTER
            includeFontPadding = false
            minHeight = ToonTheme.dp(context, ToonTheme.Metrics.minimumTouchDp)
            minWidth = ToonTheme.dp(context, ToonTheme.Metrics.minimumTouchDp)
            setPadding(
                ToonTheme.dp(context, ToonTheme.Space.md),
                ToonTheme.dp(context, ToonTheme.Space.xs),
                ToonTheme.dp(context, ToonTheme.Space.md),
                ToonTheme.dp(context, ToonTheme.Space.xs)
            )
            background = ToonComponents.ripple(context, shape, ToonTheme.Radius.pill)
            isEnabled = enabled
            isSelected = selected
            isClickable = onClick != null
            isFocusable = onClick != null
            isFocusableInTouchMode = onClick != null
            if (onClick != null) ToonComponents.applyFocusOutline(this, context, shape, stroke)
            alpha = if (enabled) 1f else 0.48f
            onClick?.let { action -> setOnClickListener { if (isEnabled) action() } }
            if (onClick != null) ToonComponents.exposeAsButton(this)
            stateListAnimator = null
        }
    }
}
