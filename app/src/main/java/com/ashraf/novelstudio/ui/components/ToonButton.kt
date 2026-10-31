package com.ashraf.novelstudio.ui.components

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.widget.TextView
import com.ashraf.novelstudio.ui.theme.ToonTheme

enum class ToonButtonVariant { PRIMARY, SECONDARY, GHOST, DANGER }
enum class ToonButtonSize { COMPACT, REGULAR }

object ToonButton {
    fun create(
        context: Context,
        label: CharSequence,
        variant: ToonButtonVariant = ToonButtonVariant.PRIMARY,
        size: ToonButtonSize = ToonButtonSize.REGULAR,
        enabled: Boolean = true,
        accessibilityLabel: String? = null,
        onClick: (() -> Unit)? = null
    ): TextView {
        require(label.isNotBlank() || !accessibilityLabel.isNullOrBlank()) {
            "Icon-only buttons require a contentDescription."
        }
        val colors = ToonTheme.colors(context)
        val fill: Int
        val text: Int
        val stroke: Int
        when (variant) {
            ToonButtonVariant.PRIMARY -> {
                fill = colors.accent
                text = colors.onAccent
                stroke = Color.TRANSPARENT
            }
            ToonButtonVariant.SECONDARY -> {
                fill = colors.surface
                text = colors.ink
                stroke = colors.lineStrong
            }
            ToonButtonVariant.GHOST -> {
                fill = Color.TRANSPARENT
                text = colors.body
                stroke = Color.TRANSPARENT
            }
            ToonButtonVariant.DANGER -> {
                fill = colors.dangerTint
                text = colors.danger
                stroke = colors.danger
            }
        }
        val radius = ToonTheme.Radius.pill
        val shape = ToonComponents.rounded(
            context,
            if (variant == ToonButtonVariant.GHOST) Color.TRANSPARENT else fill,
            radius,
            stroke
        )
        val view = TextView(context).apply {
            this.text = label
            textSize = ToonTheme.Type.label
            typeface = ToonTheme.Type.mediumTypeface()
            setTextColor(text)
            gravity = Gravity.CENTER
            includeFontPadding = false
            minHeight = ToonTheme.dp(context, ToonTheme.Metrics.minimumTouchDp)
            minWidth = ToonTheme.dp(context, ToonTheme.Metrics.minimumTouchDp)
            val horizontal = if (size == ToonButtonSize.COMPACT) ToonTheme.Space.md else ToonTheme.Space.xl
            setPadding(
                ToonTheme.dp(context, horizontal),
                ToonTheme.dp(context, ToonTheme.Space.xs),
                ToonTheme.dp(context, horizontal),
                ToonTheme.dp(context, ToonTheme.Space.xs)
            )
            background = ToonComponents.ripple(context, shape, radius)
            isEnabled = enabled
            isClickable = onClick != null
            isFocusable = onClick != null
            isFocusableInTouchMode = onClick != null
            if (onClick != null) ToonComponents.applyFocusOutline(this, context, shape, stroke)
            this.contentDescription = accessibilityLabel?.takeIf { it.isNotBlank() }
            alpha = if (enabled) 1f else 0.48f
            onClick?.let { action -> setOnClickListener { if (isEnabled) action() } }
            if (onClick != null) ToonComponents.exposeAsButton(this)
            stateListAnimator = null

        }
        return view
    }
}
