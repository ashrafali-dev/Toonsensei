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
        onClick: (() -> Unit)? = null
    ): FrameLayout {
        val colors = ToonTheme.colors(context)
        return FrameLayout(context).apply {
            background = ToonComponents.rounded(
                context,
                colors.surface,
                radiusDp,
                colors.line
            )
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
                LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
            )
            isClickable = clickable || onClick != null
            isFocusable = clickable || onClick != null
            if (onClick != null) {
                background = ToonComponents.ripple(
                    context,
                    ToonComponents.rounded(context, colors.surface, radiusDp, colors.line),
                    radiusDp
                )
                setOnClickListener { onClick() }
                contentDescription = content?.contentDescription
            }
            stateListAnimator = null
        }
    }
}
