package com.ashraf.novelstudio.ui.components

import android.content.Context
import android.text.InputType
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import com.ashraf.novelstudio.ui.theme.ToonTheme

object ToonInput {
    data class Field(
        val container: LinearLayout,
        val editText: EditText
    )

    fun create(
        context: Context,
        label: CharSequence,
        hint: CharSequence = "",
        initialValue: CharSequence = "",
        inputType: Int = InputType.TYPE_CLASS_TEXT,
        singleLine: Boolean = true
    ): Field {
        val colors = ToonTheme.colors(context)
        val labelView = ToonComponents.text(
            context,
            label,
            ToonTheme.Type.caption,
            colors.muted,
            medium = true
        )
        val edit = EditText(context).apply {
            id = View.generateViewId()
            this.inputType = inputType
            setText(initialValue)
            this.hint = hint
            setHintTextColor(colors.faint)
            setTextColor(colors.ink)
            textSize = ToonTheme.Type.body
            typeface = ToonTheme.Type.uiTypeface()
            isSingleLine = singleLine
            minHeight = ToonTheme.dp(context, 48)
            setPadding(
                ToonTheme.dp(context, ToonTheme.Space.md),
                ToonTheme.dp(context, ToonTheme.Space.xs),
                ToonTheme.dp(context, ToonTheme.Space.md),
                ToonTheme.dp(context, ToonTheme.Space.xs)
            )
            background = ToonComponents.rounded(
                context,
                colors.surfaceAlt,
                ToonTheme.Radius.field,
                colors.line
            )
            contentDescription = label.toString()
            setSelectAllOnFocus(false)
            if (!singleLine) {
                gravity = android.view.Gravity.TOP or android.view.Gravity.START
                minLines = 3
            }
        }
        labelView.labelFor = edit.id
        ToonComponents.applyFocusState(
            edit,
            context,
            ToonTheme.Radius.field,
            colors.surfaceAlt,
            colors.line
        )
        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            addView(
                labelView,
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            )
            addView(
                edit,
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = ToonTheme.dp(context, ToonTheme.Space.xs)
                }
            )
        }
        return Field(container, edit)
    }
}
