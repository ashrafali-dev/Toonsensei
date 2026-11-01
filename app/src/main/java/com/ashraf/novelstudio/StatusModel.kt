package com.ashraf.novelstudio

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import kotlin.math.cos
import kotlin.math.sin

/**
 * The single source of truth for "what is the app doing" (ui spec ROADMAP section 6).
 * The chat ring, the Tasks tray, the immersive ring and the top progress line all read only
 * from this model; MainActivity fills it from the existing lane collectors. No 1.4 s cycling:
 * the top lane is chosen once, by priority.
 */
internal class StatusModel {

    /** One working lane (OCR, translation, queue ...). [key] is a lane key of activityDotStatuses(). */
    data class Lane(val key: String, val label: String, val color: Int, val blinking: Boolean = true) {
        val busy: Boolean get() = key != "idle"
    }

    /** One row of the Tasks tray (spec board 5). [pct] < 0 hides the determinate bar. */
    data class TrayRow(val title: String, val sub: String, val rightTop: String, val rightBottom: String, val dot: Int, val pct: Int = -1)

    /** Highest-priority non-idle lane, or null when nothing works. */
    var top: Lane? = null
        private set
    var rows: List<TrayRow> = emptyList()
        private set
    /** Determinate chatbot progress 0..100, -1 when nothing runs. */
    var chatPct: Int = -1
        private set
    /** Any lane working (drives the indeterminate ring sweep). */
    var busy: Boolean = false
        private set
    /** A failure needs the user's hand (chatbot login / CAPTCHA / send failure). Amber ring + banner. */
    var needsAttention: Boolean = false
        private set
    var attentionMessage: String = ""
        private set

    /** Called on the UI thread whenever something changed. */
    var listener: (() -> Unit)? = null

    private var lastSig = ""

    fun update(
        lanes: List<Lane>,
        rows: List<TrayRow>,
        chatPct: Int,
        attention: Boolean,
        attentionMessage: String
    ) {
        val sig = lanes.joinToString("|") { it.key } + "#" + rows.joinToString("|") { it.title + it.sub + it.pct } +
            "#$chatPct#$attention#$attentionMessage"
        if (sig == lastSig) return
        lastSig = sig
        top = lanes.firstOrNull { it.busy }
        this.rows = rows
        this.chatPct = chatPct
        this.busy = top != null
        this.needsAttention = attention
        this.attentionMessage = attentionMessage
        listener?.invoke()
    }

    fun clearAttention() {
        if (!needsAttention) return
        lastSig = ""                       // force the next update() over the same lanes
        needsAttention = false
        attentionMessage = ""
        listener?.invoke()
    }

    companion object {
        /** Display priority; lower wins. Mirrors the old chip's fixed precedence, minus the cycling. */
        private val PRIORITY = listOf(
            "manual-extract", "sending", "visible-ocr", "next-chapter-ocr", "read-ahead-ocr",
            "placement", "chatbot-translation", "ahead-translation", "prefetch-translation",
            "next-chapter-load", "prefetch-queued", "chatbot-queued", "idle"
        )

        fun sortLanes(lanes: List<Lane>): List<Lane> =
            lanes.sortedBy { PRIORITY.indexOf(it.key).let { i -> if (i < 0) PRIORITY.size else i } }
    }
}

// ============================================================================ chat ring

/**
 * The dock's rightmost button: a 36 dp ring (spec board 3). Fill = current chatbot progress
 * (Seal arc + percentage number). Amber + "!" when attention is needed. A short arc rotates
 * while any lane works but has no determinate number yet. Idle: a quiet chat glyph.
 */
internal class ChatRingView(ctx: Context) : View(ctx) {
    var pct: Int = -1
        set(v) { if (field != v) { field = v; invalidate() } }
    var busy: Boolean = false
        set(v) { if (field != v) { field = v; syncAnim(); invalidate() } }
    var attention: Boolean = false
        set(v) { if (field != v) { field = v; invalidate() } }

    private val dens = resources.displayMetrics.density
    private val track = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE; strokeWidth = 2.5f * dens; color = UiTheme.strokeSoft
    }
    private val arc = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE; strokeWidth = 2.5f * dens; strokeCap = Paint.Cap.ROUND
    }
    private val num = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 11f * dens; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD); textAlign = Paint.Align.CENTER
    }
    private val oval = RectF()
    private var sweepPos = 0f
    private var anim: android.animation.ValueAnimator? = null
    private val chatGlyph = IconDrawable(Ic.CHAT, UiTheme.faint)

    private fun syncAnim() {
        val want = busy && pct < 0 && visibility == VISIBLE && isAttachedToWindow
        if (want && anim == null) {
            anim = android.animation.ValueAnimator.ofFloat(0f, 1f).apply {
                duration = 1400
                repeatCount = android.animation.ValueAnimator.INFINITE
                interpolator = android.view.animation.LinearInterpolator()
                addUpdateListener { sweepPos = it.animatedValue as Float; invalidate() }
                start()
            }
        } else if (!want) {
            anim?.cancel(); anim = null
        }
    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) { super.onVisibilityChanged(changedView, visibility); syncAnim() }
    override fun onAttachedToWindow() { super.onAttachedToWindow(); syncAnim() }
    override fun onDetachedFromWindow() { anim?.cancel(); anim = null; super.onDetachedFromWindow() }

    override fun onDraw(c: Canvas) {
        val w = width.toFloat(); val h = height.toFloat()
        val cx = w / 2f; val cy = h / 2f
        val r = 15f * dens        // 36dp ring box like the board
        oval.set(cx - r, cy - r, cx + r, cy + r)
        when {
            attention -> {
                // full calm ring in amber + "!"
                arc.color = UiTheme.amber
                c.drawArc(oval, 0f, 360f, false, arc)
                num.color = UiTheme.amber
                c.drawText("!", cx, cy - (num.descent() + num.ascent()) / 2f, num)
            }
            pct >= 0 -> {
                c.drawArc(oval, 0f, 360f, false, track)
                arc.color = UiTheme.seal
                c.drawArc(oval, -90f, pct.coerceIn(0, 100) * 3.6f, false, arc)
                num.color = UiTheme.text
                c.drawText(pct.coerceIn(0, 100).toString(), cx, cy - (num.descent() + num.ascent()) / 2f, num)
            }
            busy -> {
                c.drawArc(oval, 0f, 360f, false, track)
                arc.color = UiTheme.seal
                c.drawArc(oval, sweepPos * 360f - 90f, 72f, false, arc)
                num.color = UiTheme.faint
                c.drawText("…", cx, cy - (num.descent() + num.ascent()) / 2f, num)
            }
            else -> {
                c.drawArc(oval, 0f, 360f, false, track)
                val s = (16 * dens).toInt()
                chatGlyph.setBounds((cx - s / 2f).toInt(), (cy - s / 2f).toInt(), (cx + s / 2f).toInt(), (cy + s / 2f).toInt())
                chatGlyph.draw(c)
            }
        }
    }
}

/** Sage "next chapter ready" badge drawn on the Next dock button (spec board 3). */
internal class NextBadgeView(ctx: Context) : View(ctx) {
    private val p = Paint(Paint.ANTI_ALIAS_FLAG)
    private val check = IconDrawable(Ic.CHECK, UiTheme.bg, 3.4f)
    override fun onDraw(c: Canvas) {
        val w = width.toFloat(); val h = height.toFloat(); val d = resources.displayMetrics.density
        val r = 7f * d
        p.color = UiTheme.bg
        c.drawCircle(w / 2f, h / 2f, r, p)                       // halo so the badge reads over the icon
        p.color = UiTheme.sage
        c.drawCircle(w / 2f, h / 2f, r - 1.2f * d, p)
        val s = (9f * d).toInt()
        check.setBounds(((w - s) / 2f).toInt(), ((h - s) / 2f).toInt(), ((w + s) / 2f).toInt(), ((h + s) / 2f).toInt())
        check.draw(c)
    }
}

/** The 3 dp progress line pinned to the top edge in the immersive state (spec board 4). */
internal class TopLineProgress(ctx: Context) : View(ctx) {
    var pct: Int = -1
        set(v) { if (field != v) { field = v; invalidate() } }
    var busy: Boolean = false
        set(v) { if (field != v) { field = v; invalidate() } }
    private val p = Paint(Paint.ANTI_ALIAS_FLAG)
    override fun onDraw(c: Canvas) {
        val w = width.toFloat(); val h = height.toFloat()
        p.color = UiTheme.strokeSoft
        c.drawRect(0f, 0f, w, h, p)
        if (pct >= 0) {
            p.color = UiTheme.seal
            c.drawRect(0f, 0f, w * pct.coerceIn(0, 100) / 100f, h, p)
        } else if (busy) {
            // waiting, no number yet: a quiet middle segment
            p.color = UiTheme.seal
            c.drawRect(w * 0.2f, 0f, w * 0.8f, h, p)
        }
    }
}

// ============================================================================ sheets

internal object Dock {

    /** One row of the Tasks tray (spec board 5): 8dp dot, title 15, sub 13 faint, right column. */
    private fun trayRow(ctx: Context, row: StatusModel.TrayRow): View {
        val dens = ctx.resources.displayMetrics.density
        val root = LinearLayout(ctx).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, ctx.dpi(12), 0, ctx.dpi(12))
        }
        val dot = View(ctx).apply {
            background = Glass.oval(row.dot)
        }
        root.addView(dot, LinearLayout.LayoutParams(ctx.dpi(8), ctx.dpi(8)).also { it.marginEnd = ctx.dpi(12) })
        val mid = LinearLayout(ctx).apply { orientation = LinearLayout.VERTICAL }
        mid.addView(Ui.text(ctx, row.title, Ui.BODY, UiTheme.text))
        mid.addView(Ui.caption(ctx, row.sub))
        root.addView(mid, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        val rightCol = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.END
        }
        if (row.rightTop.isNotEmpty()) rightCol.addView(Ui.text(ctx, row.rightTop, Ui.SMALL, UiTheme.faint))
        if (row.rightBottom.isNotEmpty()) rightCol.addView(Ui.text(ctx, row.rightBottom, Ui.SMALL, UiTheme.faint))
        root.addView(rightCol, LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT))
        if (row.pct >= 0) {
            val bar = GlowBar(ctx).apply { progress = row.pct }
            return LinearLayout(ctx).apply {
                orientation = LinearLayout.VERTICAL
                addView(root, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT))
                addView(bar, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (3 * dens).toInt()))
            }
        }
        return root
    }

    /**
     * Tasks tray (spec board 5), opened by long-pressing the chat ring.
     * [footer] is the old chip detail line ("OCR Lens · Detector on · AI painter on").
     * [log] lists quietly-logged messages (class B notices).
     */
    fun buildTasksTray(
        ctx: Context,
        model: StatusModel,
        aheadOn: Boolean,
        footer: String,
        log: List<String>,
        onStopAll: () -> Unit,
        onAhead: (Boolean) -> Unit,
        onResend: () -> Unit,
        onOpenChat: () -> Unit,
        onOpenLog: () -> Unit = {}
    ): View {
        val root = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(ctx.dpi(20), ctx.dpi(12), ctx.dpi(20), ctx.dpi(28))
            background = Sheets.sheetBackground(ctx)
        }
        root.addView(Ui.sheetHandle(ctx), LinearLayout.LayoutParams(ctx.dpi(36), ctx.dpi(4)).also {
            it.gravity = Gravity.CENTER_HORIZONTAL; it.bottomMargin = ctx.dpi(8)
        })
        // header: "Tasks" + "Stop all"
        val header = LinearLayout(ctx).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        header.addView(Ui.text(ctx, "Tasks", Ui.H2, UiTheme.text, bold = true), LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        val stopAll = Ui.text(ctx, "Stop all", 14f, UiTheme.seal, bold = true).apply {
            setPadding(ctx.dpi(12), ctx.dpi(10), ctx.dpi(4), ctx.dpi(10))
            contentDescription = "Stop all background work"
            setOnClickListener { onStopAll() }
        }
        header.addView(stopAll)
        root.addView(header, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ctx.dpi(48)))

        // lane rows
        model.rows.forEachIndexed { i, row ->
            root.addView(trayRow(ctx, row))
            if (i < model.rows.lastIndex) {
                root.addView(Ui.divider(ctx), LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, maxOf(1, ctx.dpi(0.5))))
            }
        }
        // Ahead toggle
        val aheadRow = LinearLayout(ctx).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, ctx.dpi(12), 0, ctx.dpi(4))
        }
        val aheadText = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            addView(Ui.text(ctx, "Ahead", Ui.BODY, UiTheme.text))
            addView(Ui.caption(ctx, "Translates the next chapter while you read. Uses more chatbot messages. Novel and Scanning only.").apply {
                setLineSpacing(0f, 1.16f)
            })
        }
        aheadRow.addView(aheadText, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        val sw = SwitchFlat(ctx).apply {
            isChecked = aheadOn
            onToggled = { onAhead(it) }
            contentDescription = "Ahead"
        }
        aheadRow.addView(sw)
        root.addView(aheadRow)
        // footer: engine line + quiet log peek
        root.addView(Ui.caption(ctx, footer), LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).also {
            it.topMargin = ctx.dpi(4); it.bottomMargin = ctx.dpi(16)
        })
        if (log.isNotEmpty()) {
            val logCard = LinearLayout(ctx).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(ctx.dpi(14), ctx.dpi(4), ctx.dpi(14), ctx.dpi(4))
                background = Ui.card(ctx)
            }
            log.takeLast(3).forEach { m ->
                logCard.addView(Ui.text(ctx, m, 14f, UiTheme.text), LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).also {
                    it.topMargin = ctx.dpi(10); it.bottomMargin = ctx.dpi(10)
                })
            }
            logCard.setOnClickListener { onOpenLog() }
            root.addView(logCard, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).also { it.bottomMargin = ctx.dpi(14) })
        }
        // buttons: Resend + Open chatbot
        val btns = LinearLayout(ctx).apply {
            orientation = LinearLayout.HORIZONTAL
        }
        val resend = Ui.btnGhost(ctx, "Resend")
        resend.setOnClickListener { onResend() }
        btns.addView(resend, LinearLayout.LayoutParams(0, ctx.dpi(48), 1f).also { it.marginEnd = ctx.dpi(10) })
        val open = Ui.btnPrimary(ctx, "Open chatbot")
        open.setOnClickListener { onOpenChat() }
        btns.addView(open, LinearLayout.LayoutParams(0, ctx.dpi(48), 1f))
        root.addView(btns)
        return root
    }

    /** Mode pick sheet (spec board 16): four rows, active one tinted Seal. */
    fun buildModeSheet(ctx: Context, activeIdx: Int, onPick: (Int) -> Unit): View {
        data class M(val name: String, val desc: String, val ic: Ic)
        val modes = listOf(
            M("Novel", "Translate chapter text on the page", Ic.BOOK),
            M("Scanning", "Prepare a whole comic chapter", Ic.SCAN),
            M("Real-time", "Translate as you scroll", Ic.CROSSHAIR),
            M("Manual Lite", "Read one screen when you tap. Light on battery and RAM", Ic.CURSOR)
        )
        val root = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(ctx.dpi(20), ctx.dpi(12), ctx.dpi(20), ctx.dpi(24))
            background = Sheets.sheetBackground(ctx)
        }
        root.addView(Ui.sheetHandle(ctx), LinearLayout.LayoutParams(ctx.dpi(36), ctx.dpi(4)).also {
            it.gravity = Gravity.CENTER_HORIZONTAL; it.bottomMargin = ctx.dpi(12)
        })
        modes.forEachIndexed { i, m ->
            val active = i == activeIdx
            val selColor = if (active) UiTheme.sealInk else UiTheme.text
            val row = LinearLayout(ctx).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                minimumHeight = ctx.dpi(64)
                setPadding(ctx.dpi(12), ctx.dpi(8), ctx.dpi(12), ctx.dpi(8))
                background = if (active) Glass.solid(ctx, UiTheme.sealSoft, 16) else Glass.ripple(ctx, null, 16)
                contentDescription = m.name + ". " + m.desc
            }
            val icon = android.widget.ImageView(ctx).apply {
                setImageDrawable(IconDrawable(m.ic, selColor))
            }
            row.addView(icon, LinearLayout.LayoutParams(ctx.dpi(24), ctx.dpi(24)).also { it.marginEnd = ctx.dpi(14) })
            val texts = LinearLayout(ctx).apply {
                orientation = LinearLayout.VERTICAL
                addView(Ui.text(ctx, m.name, Ui.BODY, selColor, bold = true))
                addView(Ui.caption(ctx, m.desc).apply { setLineSpacing(0f, 1.16f) })
            }
            row.addView(texts, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
            row.setOnClickListener { onPick(i) }
            root.addView(row, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).also {
                it.topMargin = if (i == 0) 0 else ctx.dpi(2)
            })
        }
        return root
    }
}
