package com.ashraf.novelstudio.ui.components

import android.content.Context
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import com.ashraf.novelstudio.ui.theme.ToonTheme

object ToonStateView {
    enum class State { LOADING, EMPTY, ERROR, OFFLINE, PERMISSION }

    fun create(
        context: Context,
        state: State,
        title: CharSequence,
        message: CharSequence? = null,
        actionLabel: CharSequence? = null,
        onAction: (() -> Unit)? = null,
        progressPercent: Int? = null
    ): FrameLayout {
        val colors = ToonTheme.colors(context)
        val root = FrameLayout(context).apply {
            setBackgroundColor(colors.background)
            importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_YES
        }
        val content = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(
                ToonTheme.dp(context, ToonTheme.Space.xl),
                ToonTheme.dp(context, ToonTheme.Space.xxl),
                ToonTheme.dp(context, ToonTheme.Space.xl),
                ToonTheme.dp(context, ToonTheme.Space.xxl)
            )
        }
        if (state == State.LOADING) {
            // Only render a bar when the caller supplies real determinate task progress.
            if (progressPercent != null) {
                val progress = ProgressBar(
                    context,
                    null,
                    android.R.attr.progressBarStyleHorizontal
                ).apply {
                    isIndeterminate = false
                    max = 100
                    progressTintList =
                        android.content.res.ColorStateList.valueOf(colors.accent)
                    progressBackgroundTintList =
                        android.content.res.ColorStateList.valueOf(colors.line)
                    progress = progressPercent.coerceIn(0, 100)
                    contentDescription = context.getString(com.ashraf.novelstudio.R.string.toon_progress_percent, progress)
                }
                content.addView(
                    progress,
                    LinearLayout.LayoutParams(
                        ToonTheme.dp(context, 144),
                        ToonTheme.dp(context, 3)
                    )
                )
            }
        } else {
            val eyebrow = when (state) {
                State.EMPTY -> context.getString(com.ashraf.novelstudio.R.string.toon_state_empty_eyebrow)
                State.ERROR -> context.getString(com.ashraf.novelstudio.R.string.toon_state_error_eyebrow)
                State.OFFLINE -> context.getString(com.ashraf.novelstudio.R.string.toon_state_offline_eyebrow)
                State.PERMISSION -> context.getString(com.ashraf.novelstudio.R.string.toon_state_permission_eyebrow)
                State.LOADING -> ""
            }
            content.addView(
                ToonComponents.text(
                    context,
                    eyebrow,
                    ToonTheme.Type.micro,
                    colors.muted,
                    medium = true
                ).apply { gravity = Gravity.CENTER }
            )
        }
        val heading = ToonComponents.text(
            context,
            title,
            ToonTheme.Type.title,
            colors.ink,
            medium = true
        ).apply {
            gravity = Gravity.CENTER
            contentDescription = title.toString()
        }
        content.addView(
            heading,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = ToonTheme.dp(context, ToonTheme.Space.md)
            }
        )
        if (!message.isNullOrBlank()) {
            content.addView(
                ToonComponents.text(
                    context,
                    message,
                    ToonTheme.Type.body,
                    colors.muted
                ).apply {
                    gravity = Gravity.CENTER
                },
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = ToonTheme.dp(context, ToonTheme.Space.xs)
                }
            )
        }
        if (!actionLabel.isNullOrBlank() && onAction != null) {
            content.addView(
                ToonButton.create(
                    context,
                    actionLabel,
                    ToonButtonVariant.PRIMARY,
                    onClick = onAction
                ),
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = ToonTheme.dp(context, ToonTheme.Space.lg)
                }
            )
        }
        root.addView(
            content,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER
            )
        )
        return root
    }
}
