package com.ashraf.novelstudio.ui.components

import android.content.Context
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.ashraf.novelstudio.R

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
                val button = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                if (destructive) {
                    button.setTextColor(context.getColor(R.color.toon_danger))
                }
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

    fun choices(
        context: Context,
        title: CharSequence,
        labels: Array<out CharSequence>,
        onChoice: (Int) -> Unit
    ): AlertDialog = builder(context)
        .setTitle(title)
        .setItems(labels) { _, which -> onChoice(which) }
        .create()
}
