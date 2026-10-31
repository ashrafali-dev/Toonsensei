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
        confirmLabel: CharSequence = "Confirm",
        cancelLabel: CharSequence = "Cancel",
        destructive: Boolean = false,
        onConfirm: () -> Unit
    ): AlertDialog = builder(context)
        .setTitle(title)
        .setMessage(message)
        .setNegativeButton(cancelLabel, null)
        .setPositiveButton(confirmLabel) { _, _ -> onConfirm() }
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
        actionLabel: CharSequence = "Got it"
    ): AlertDialog = builder(context)
        .setTitle(title)
        .setMessage(message)
        .setPositiveButton(actionLabel, null)
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
