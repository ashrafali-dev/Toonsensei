package com.ashraf.novelstudio.ui.components

import android.content.Context
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.ashraf.novelstudio.R
import com.ashraf.novelstudio.ui.theme.ToonTheme

/** Central dialog factory. Existing call sites migrate to this in the overlays group. */
object ToonDialog {
    fun builder(context: Context): MaterialAlertDialogBuilder =
        MaterialAlertDialogBuilder(context, R.style.ThemeOverlay_ToonSensei_MaterialAlertDialog)

    fun confirm(
        context: Context,
        title: CharSequence,
        message: CharSequence,
        confirmLabel: CharSequence? = null,
        cancelLabel: CharSequence? = null,
        destructive: Boolean = false,
        onConfirm: () -> Unit
    ): AlertDialog = builder(context)
        .setTitle(title)
        .setMessage(message)
        .setNegativeButton(cancelLabel ?: context.getString(R.string.toon_action_cancel), null)
        .setPositiveButton(confirmLabel ?: context.getString(R.string.toon_action_confirm)) { _, _ -> onConfirm() }
        .create()
        .also { dialog ->
            dialog.setOnShowListener {
                val colors = ToonTheme.colors(context)
                dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(
                    if (destructive) colors.danger else colors.accentForeground
                )
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.setTextColor(colors.muted)
            }
        }

    fun info(
        context: Context,
        title: CharSequence,
        message: CharSequence,
        actionLabel: CharSequence? = null
    ): AlertDialog = builder(context)
        .setTitle(title)
        .setMessage(message)
        .setPositiveButton(actionLabel ?: context.getString(R.string.toon_action_got_it), null)
        .create()
        .also { dialog ->
            dialog.setOnShowListener {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(
                    ToonTheme.colors(context).accentForeground
                )
            }
        }

    fun choices(
        context: Context,
        title: CharSequence,
        labels: Array<out CharSequence>,
        onChoice: (Int) -> Unit
    ): AlertDialog = builder(context)
        .setTitle(title)
        .setItems(labels) { _, which -> onChoice(which) }
        .create()
        .also { dialog ->
            dialog.setOnShowListener {
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.setTextColor(
                    ToonTheme.colors(context).muted
                )
            }
        }
}
