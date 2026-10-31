package com.ashraf.novelstudio.ui.components

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.ashraf.novelstudio.Ic
import com.ashraf.novelstudio.IconDrawable
import com.ashraf.novelstudio.ui.theme.ToonTheme

/**
 * Inline notice primitive. It deliberately does not call android.widget.Toast:
 * notices are embedded at the top of their host and remain until explicitly resolved/dismissed.
 */
object ToonToast {
    enum class Tone { INFO, SUCCESS, WARNING, ERROR }

    fun create(
        context: Context,
        message: CharSequence,
        tone: Tone = Tone.INFO,
        actionLabel: CharSequence? = null,
        onAction: (() -> Unit)? = null,
        onDismiss: (() -> Unit)? = null
    ): View {
        val colors = ToonTheme.colors(context)
        val (fill, ink, border) = when (tone) {
            Tone.INFO -> Triple(colors.surfaceAlt, colors.ink, colors.line)
            Tone.SUCCESS -> Triple(colors.successTint, colors.successInk, colors.success)
            Tone.WARNING -> Triple(colors.warningTint, colors.warning, colors.warningLine)
            Tone.ERROR -> Triple(colors.dangerTint, colors.danger, colors.danger)
        }
        val root = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            background = ToonComponents.rounded(
                context,
                fill,
                ToonTheme.Radius.card,
                border
            )
            setPadding(
                ToonTheme.dp(context, ToonTheme.Space.md),
                ToonTheme.dp(context, ToonTheme.Space.sm),
                ToonTheme.dp(context, ToonTheme.Space.xs),
                ToonTheme.dp(context, ToonTheme.Space.sm)
            )
            isFocusable = true
            importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_YES
            accessibilityLiveRegion = View.ACCESSIBILITY_LIVE_REGION_POLITE
        }
        val body = ToonComponents.text(
            context,
            message,
            ToonTheme.Type.body,
            ink
        ).apply {
            gravity = Gravity.CENTER_VERTICAL
        }
        root.addView(
            body,
            LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        )
        if (!actionLabel.isNullOrBlank() && onAction != null) {
            val action = TextView(context).apply {
                text = actionLabel
                textSize = ToonTheme.Type.label
                typeface = ToonTheme.Type.mediumTypeface()
                setTextColor(ink)
                gravity = Gravity.CENTER
                minHeight = ToonTheme.dp(context, ToonTheme.Metrics.minimumTouchDp)
                minWidth = ToonTheme.dp(context, ToonTheme.Metrics.minimumTouchDp)
                setPadding(
                    ToonTheme.dp(context, ToonTheme.Space.sm),
                    0,
                    ToonTheme.dp(context, ToonTheme.Space.sm),
                    0
                )
                isFocusable = true
                background = ToonComponents.ripple(
                    context,
                    GradientDrawable().apply {
                        setColor(Color.TRANSPARENT)
                        cornerRadius = ToonTheme.dp(context, ToonTheme.Radius.pill).toFloat()
                    },
                    ToonTheme.Radius.pill
                )
                setOnClickListener { onAction() }
            }
            root.addView(action)
        }
        if (onDismiss != null) {
            val close = ImageView(context).apply {
                setImageDrawable(IconDrawable(Ic.CLOSE, ink))
                contentDescription = context.getString(com.ashraf.novelstudio.R.string.toon_action_dismiss)
                scaleType = ImageView.ScaleType.CENTER_INSIDE
                setPadding(
                    ToonTheme.dp(context, ToonTheme.Space.md),
                    ToonTheme.dp(context, ToonTheme.Space.md),
                    ToonTheme.dp(context, ToonTheme.Space.md),
                    ToonTheme.dp(context, ToonTheme.Space.md)
                )
                minWidth = ToonTheme.dp(context, 48)
                minHeight = ToonTheme.dp(context, 48)
                isFocusable = true
                background = ToonComponents.ripple(
                    context,
                    GradientDrawable().apply {
                        setColor(Color.TRANSPARENT)
                        cornerRadius = ToonTheme.dp(context, ToonTheme.Radius.pill).toFloat()
                    },
                    ToonTheme.Radius.pill
                )
                setOnClickListener { onDismiss() }
            }
            root.addView(close)
        }
        return root
    }
}
