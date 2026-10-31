package com.ashraf.novelstudio.ui.components

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.view.View
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.TextView
import com.ashraf.novelstudio.ui.theme.ToonTheme

/** Shared, flat drawing and accessibility helpers for programmatic Views. */
object ToonComponents {
    fun rounded(
        context: Context,
        fill: Int,
        radiusDp: Int,
        stroke: Int = Color.TRANSPARENT,
        strokeDp: Int = ToonTheme.Metrics.hairlineDp
    ): GradientDrawable = GradientDrawable().apply {
        shape = GradientDrawable.RECTANGLE
        setColor(fill)
        cornerRadius = ToonTheme.dp(context, radiusDp).toFloat()
        if (stroke != Color.TRANSPARENT) {
            setStroke(ToonTheme.dp(context, strokeDp), stroke)
        }
    }

    fun ripple(context: Context, content: android.graphics.drawable.Drawable, radiusDp: Int): RippleDrawable {
        val mask = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            setColor(Color.WHITE)
            cornerRadius = ToonTheme.dp(context, radiusDp).toFloat()
        }
        return RippleDrawable(
            ColorStateList.valueOf(ToonTheme.colors(context).ripple),
            content,
            mask
        )
    }

    fun applyFocusState(view: View, context: Context, radiusDp: Int, fill: Int, normalStroke: Int) {
        fun background(focused: Boolean) = rounded(
            context,
            fill,
            radiusDp,
            if (focused) ToonTheme.colors(context).focus else normalStroke,
            if (focused) ToonTheme.Metrics.focusStrokeDp else ToonTheme.Metrics.hairlineDp
        )
        view.background = background(view.hasFocus())
        view.isFocusable = true
        view.isFocusableInTouchMode = true
        view.setOnFocusChangeListener { target, focused ->
            target.background = background(focused)
        }
    }

    fun text(
        context: Context,
        value: CharSequence,
        sizeSp: Float = ToonTheme.Type.body,
        color: Int = ToonTheme.colors(context).ink,
        medium: Boolean = false
    ): TextView = TextView(context).apply {
        text = value
        textSize = sizeSp
        setTextColor(color)
        typeface = if (medium) ToonTheme.Type.mediumTypeface() else ToonTheme.Type.uiTypeface()
        includeFontPadding = true
        setLineSpacing(0f, ToonTheme.Type.lineSpacingMultiplier)
    }

    fun exposeAsButton(view: View) {
        view.accessibilityDelegate = object : View.AccessibilityDelegate() {
            override fun onInitializeAccessibilityNodeInfo(host: View, info: AccessibilityNodeInfo) {
                super.onInitializeAccessibilityNodeInfo(host, info)
                info.className = android.widget.Button::class.java.name
            }
        }
    }

    fun touchTarget(view: View, context: Context, minDp: Int = ToonTheme.Metrics.minimumTouchDp) {
        view.minimumWidth = ToonTheme.dp(context, minDp)
        view.minimumHeight = ToonTheme.dp(context, minDp)
        view.isFocusable = true
    }
}
