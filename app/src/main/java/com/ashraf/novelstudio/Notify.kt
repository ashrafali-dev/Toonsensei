package com.ashraf.novelstudio

import android.content.Context
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView

/**
 * Notification policy of the redesign (ui spec ROADMAP section 5):
 *  - class A "user did something" -> top growl, auto-dismiss (the old snack, restyled in MainActivity)
 *  - class B "informational"      -> silently logged here, read in the Tasks tray
 *  - class C "needs your hand"    -> amber banner under the top bar + amber chat ring (buildBanner)
 */
internal object Notify {

    /** One quiet-log entry (class B). */
    data class Entry(val atMillis: Long, val text: String)

    private val entries = ArrayDeque<Entry>()

    @Synchronized
    fun log(text: String) {
        val t = text.trim()
        if (t.isEmpty()) return
        entries.addLast(Entry(System.currentTimeMillis(), t))
        while (entries.size > 40) entries.removeFirst()
    }

    /** Most recent first. */
    @Synchronized
    fun latest(limit: Int = 40): List<Entry> = entries.toList().asReversed().take(limit)

    /** "x min ago" style relative stamp for the log sheet. */
    fun ago(atMillis: Long): String {
        val s = ((System.currentTimeMillis() - atMillis) / 1000L).coerceAtLeast(0L)
        val m = s / 60L
        return when {
            m < 1 -> "just now"
            m < 60 -> "$m min ago"
            else -> "${m / 60L} h ago"
        }
    }

    /** Class C banner (spec board 13): amber soft card, message + action + dismiss. */
    fun buildBanner(
        ctx: Context,
        message: String,
        actionLabel: String?,
        onAction: (() -> Unit)?,
        onDismiss: () -> Unit
    ): View {
        val root = LinearLayout(ctx).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(ctx.dpi(16), ctx.dpi(6), ctx.dpi(6), ctx.dpi(6))
            background = Ui.tintCard(ctx, UiTheme.amberSoft, UiTheme.amber)
            contentDescription = message
        }
        val txt = Ui.text(ctx, message, 14f, UiTheme.amberInk, bold = false).apply { textSize = 14f }
        root.addView(txt, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).also { it.marginEnd = ctx.dpi(8) })
        if (actionLabel != null && onAction != null) {
            val btn = TextView(ctx).apply {
                text = actionLabel
                textSize = 14f
                setTextColor(UiTheme.bg)
                typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
                gravity = Gravity.CENTER
                minHeight = ctx.dpi(40)
                setPadding(ctx.dpi(16), 0, ctx.dpi(16), 0)
                background = Glass.ripple(ctx, Glass.solid(ctx, UiTheme.amberInk, 20), 20)
                setOnClickListener { onAction() }
            }
            root.addView(btn, LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, ctx.dpi(40)).also { it.marginEnd = ctx.dpi(4) })
        }
        val close = ctx.iconButton(Ic.CLOSE, "Dismiss", 18, 40, UiTheme.amberInk) { onDismiss() }
        root.addView(close, LinearLayout.LayoutParams(ctx.dpi(40), ctx.dpi(40)))
        return root
    }

    /** Quiet log sheet content (class B list, spec board 27 card 4). */
    fun buildLogSheet(ctx: Context): View {
        val root = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(ctx.dpi(20), ctx.dpi(12), ctx.dpi(20), ctx.dpi(24))
            background = Sheets.sheetBackground(ctx)
        }
        root.addView(Ui.sheetHandle(ctx), LinearLayout.LayoutParams(ctx.dpi(36), ctx.dpi(4)).also {
            it.gravity = Gravity.CENTER_HORIZONTAL; it.bottomMargin = ctx.dpi(12)
        })
        root.addView(Ui.text(ctx, "Messages", Ui.H2, UiTheme.text, bold = true), LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).also {
            it.bottomMargin = ctx.dpi(8)
        })
        val items = latest()
        if (items.isEmpty()) {
            root.addView(Ui.caption(ctx, "Nothing new. Informational messages land here."))
            return root
        }
        val cardCol = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(ctx.dpi(14), 0, ctx.dpi(14), 0)
            background = Ui.card(ctx)
        }
        items.take(12).forEachIndexed { i, e ->
            val col = LinearLayout(ctx).apply {
                orientation = LinearLayout.VERTICAL
                addView(Ui.text(ctx, e.text, 14f, UiTheme.text))
                addView(Ui.caption(ctx, ago(e.atMillis)), LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).also { it.topMargin = 0 })
            }
            cardCol.addView(col, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).also {
                it.topMargin = ctx.dpi(12); it.bottomMargin = ctx.dpi(12)
            })
            if (i < items.size - 1) cardCol.addView(Ui.divider(ctx), LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, maxOf(1, ctx.dpi(0.5))))
        }
        root.addView(cardCol)
        return root
    }
}
