package com.ashraf.novelstudio.ui.components

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.ashraf.novelstudio.R

object ToonBottomSheet {
    fun create(
        context: Context,
        content: View,
        cancelable: Boolean = true,
        fullHeight: Boolean = false
    ): BottomSheetDialog {
        val dialog = BottomSheetDialog(context, R.style.ThemeOverlay_ToonSensei_BottomSheetDialog)
        dialog.setContentView(content)
        dialog.setCancelable(cancelable)
        dialog.setCanceledOnTouchOutside(cancelable)
        dialog.setOnShowListener {
            dialog.window?.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                if (fullHeight) ViewGroup.LayoutParams.MATCH_PARENT
                else WindowManager.LayoutParams.WRAP_CONTENT
            )
        }
        return dialog
    }

    fun show(
        context: Context,
        content: View,
        cancelable: Boolean = true,
        fullHeight: Boolean = false
    ): BottomSheetDialog = create(context, content, cancelable, fullHeight).also { it.show() }
}
