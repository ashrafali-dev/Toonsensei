package com.ashraf.novelstudio.ui.components

import android.content.Context
import android.view.View
import android.widget.FrameLayout
import com.ashraf.novelstudio.ui.theme.ToonTheme

object ToonCard {
    fun create(
        context: Context,
        content: View? = null,
        paddingDp: Int = ToonTheme.Space.md,
        radiusDp: Int = ToonTheme.Radius.card,
        clickable: Boolean = false,
        accessibilityLabel: CharSequence? = null,
        onClick: (() -> Unit)? = null
    ): FrameLayout {
        require(!clickable || onClick != null) { "Clickable cards require an onClick action." }
        val isInteractive = onClick != null
        require(!isInteractive || !accessibilityLabel.isNullOrBlank()) {
            "Interactive cards require an accessibilityLabel."
        }
        val colors = ToonTheme.colors(context)
        val shape = ToonComponents.rounded(context, colors.surface, radiusDp, colors.line)
        return FrameLayout(context).apply {
            background = shape
            setPadding(
                ToonTheme.dp(context, paddingDp),
                ToonTheme.dp(context, paddingDp),
                ToonTheme.dp(context, paddingDp),
                ToonTheme.dp(context, paddingDp)
            )
            clipToOutline = true
            elevation = 0f
            if (content != null) addView(
                content,
                FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT)
            )
            isClickable = isInteractive
            isFocusable = isInteractive
            isFocusableInTouchMode = isInteractive
            if (isInteractive) {
                ToonComponents.touchTarget(this, context)
                ToonComponents.applyFocusOutline(this, context, shape, colors.line)
            }
            contentDescription = accessibilityLabel?.toString()
            if (onClick != null) {
                ToonComponents.exposeAsButton(this)
                background = ToonComponents.ripple(context, shape, radiusDp)
                setOnClickListener { onClick() }
                
            }
            stateListAnimator = null
        }
    }
}
