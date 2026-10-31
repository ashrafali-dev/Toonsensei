package com.ashraf.novelstudio

import android.animation.ValueAnimator
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.RippleDrawable
import android.os.Build
import android.os.SystemClock
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

/**
 * Paper & Seal component library. Every redesigned screen builds from these; anatomy and states are
 * specified in docs/redesign/DESIGN.md §5. Components take the current [ToonColors] so both themes work.
 */
object Toon {

    /** True when the system asks for reduced motion; all component animations are then instant. */
    fun reducedMotion(ctx: Context): Boolean = try {
        Settings.Global.getFloat(ctx.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE) == 0f
    } catch (_: Throwable) {
        false
    }

    fun dur(ctx: Context, ms: Long): Long = if (reducedMotion(ctx)) 0L else ms

    private fun ripple(ctx: Context, mask: Gradientspec, c: ToonColors): RippleDrawable =
        RippleDrawable(ColorStateList.valueOf(ToonTheme.withAlpha(c.accent, 0x1F)), null, mask.build(ctx))

    /** A gradient drawable builder used for ripple masks. */
    class Gradientspec(val radiusDp: Number?, val circle: Boolean = false) {
        fun build(ctx: Context): GradientDrawable = GradientDrawable().apply {
            if (circle) shape = GradientDrawable.OVAL else cornerRadius = ToonDp.px(ctx, radiusDp ?: 0).toFloat()
            setColor(android.graphics.Color.WHITE)
        }
    }

    /** Press feedback: scale to [pressed] with the fast duration, unless reduced motion. */
    private fun pressScale(ctx: Context, v: View) {
        v.setOnTouchListener { view, e ->
            when (e.actionMasked) {
                android.view.MotionEvent.ACTION_DOWN ->
                    view.animate().scaleX(0.97f).scaleY(0.97f).setDuration(dur(ctx, ToonTheme.Motion.fast)).start()
                android.view.MotionEvent.ACTION_UP, android.view.MotionEvent.ACTION_CANCEL ->
                    view.animate().scaleX(1f).scaleY(1f).setDuration(dur(ctx, ToonTheme.Motion.fast)).start()
            }
            false
        }
    }

    /**
     * Focus ring: 2 dp accent stroke drawn outside the child's padding box on focus. Attach to any
     * focusable control; the ring is drawn by the wrapper returned here, so swap it in at build time.
     */
    class FocusFrame(ctx: Context, private val c: ToonColors, private val radiusDp: Int) : FrameLayout(ctx) {
        private val ring = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; strokeWidth = ToonDp.px(context, 2).toFloat() }
        private val rect = RectF()
        var ringed = false
            set(v) { field = v; invalidate() }

        fun content(v: View) {
            addView(v, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            if (!ringed) return
            ring.color = c.accent
            val o = ring.strokeWidth / 2f
            rect.set(o, o, width - o, height - o)
            val r = ToonDp.px(context, radiusDp).toFloat()
            canvas.drawRoundRect(rect, r, r, ring)
        }
    }

    // ================================================================== buttons

    /** Primary filled button (ink), 48 dp pill. */
    fun buttonPrimary(ctx: Context, c: ToonColors, text: String, onClick: () -> Unit): TextView =
        buttonBase(ctx, c, text, c.ink, c.onInk, null, onClick)

    /** Secondary outlined button, 48 dp pill. */
    fun buttonSecondary(ctx: Context, c: ToonColors, text: String, onClick: () -> Unit): TextView =
        buttonBase(ctx, c, text, c.surface, c.title, c.lineStrong, onClick)

    /** Destructive confined button used in confirm sheets. */
    fun buttonDanger(ctx: Context, c: ToonColors, text: String, onClick: () -> Unit): TextView =
        buttonBase(ctx, c, text, c.dangerBg, c.dangerInk, c.dangerInk, onClick)

    /** Text-only button for dialog actions and inline actions. */
    fun buttonGhost(ctx: Context, c: ToonColors, text: String, color: Int = c.title, onClick: () -> Unit): TextView =
        TextView(ctx).apply {
            this.text = text
            textSize = ToonTheme.Type.label
            typeface = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.NORMAL)
            setTextColor(color)
            gravity = Gravity.CENTER
            isClickable = true; isFocusable = true
            minimumHeight = ToonDp.px(ctx, 48)
            minWidth = ToonDp.px(ctx, 64)
            setPadding(ToonDp.px(ctx, 16), 0, ToonDp.px(ctx, 16), 0)
            background = ripple(ctx, Gradientspec(ToonTheme.Radius.field), c)
            pressScale(ctx, this)
            setOnClickListener { onClick() }
        }

    private fun buttonBase(ctx: Context, c: ToonColors, text: String, bg: Int, fg: Int, stroke: Int?, onClick: () -> Unit): TextView =
        TextView(ctx).apply {
            this.text = text
            textSize = 15f
            typeface = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.NORMAL)
            setTextColor(fg)
            gravity = Gravity.CENTER
            isClickable = true; isFocusable = true
            includeFontPadding = false
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ToonDp.px(ctx, 48))
            background = ripple(ctx, Gradientspec(24), c).apply { setDrawable(0, null) }.let { r ->
                // base fill under ripple: shape in layer 0, mask already provided by ripple
                val shape = GradientDrawable().apply {
                    cornerRadius = ToonDp.px(ctx, 24).toFloat()
                    setColor(bg)
                    if (stroke != null) setStroke(ToonDp.px(ctx, 1).coerceAtLeast(1), stroke)
                }
                RippleDrawable(ColorStateList.valueOf(ToonTheme.withAlpha(fg, 0x1A)), shape, Gradientspec(24).build(ctx)).also { r.background = it }
            }
            pressScale(ctx, this)
            setOnClickListener { onClick() }
        }

    /** 44 visible / 48 hit icon button; [desc] is mandatory for TalkBack. */
    fun iconButton(ctx: Context, c: ToonColors, ic: ToonIc, desc: String, color: Int = c.title, sizeDp: Int = 44, iconDp: Int = 22, onClick: () -> Unit): ImageView =
        ImageView(ctx).apply {
            setImageDrawable(ToonIconDrawable(ic, color))
            scaleType = ImageView.ScaleType.FIT_CENTER
            val box = ToonDp.px(ctx, sizeDp)
            val pad = (box - ToonDp.px(ctx, iconDp)) / 2
            setPadding(pad, pad, pad, pad)
            minimumWidth = ToonDp.px(ctx, 48); minimumHeight = ToonDp.px(ctx, 48)
            layoutParams = LinearLayout.LayoutParams(box, box)
            background = ripple(ctx, Gradientspec(null, circle = true), c)
            contentDescription = desc
            isClickable = true; isFocusable = true
            pressScale(ctx, this)
            setOnClickListener { onClick() }
        }

    // ================================================================== chips & segments

    /** Pill chip 36 dp (site shortcuts, filters). */
    fun chip(ctx: Context, c: ToonColors, text: String, selected: Boolean = false, onClick: () -> Unit): TextView =
        TextView(ctx).apply {
            this.text = text
            textSize = ToonTheme.Type.label
            typeface = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.NORMAL)
            maxLines = 1
            gravity = Gravity.CENTER
            setTextColor(if (selected) c.onInk else c.text)
            val fill = if (selected) c.ink else c.surfaceAlt
            background = RippleDrawable(
                ColorStateList.valueOf(ToonTheme.withAlpha(if (selected) c.onInk else c.title, 0x1A)),
                GradientDrawable().apply { cornerRadius = ToonDp.px(ctx, 18).toFloat(); setColor(fill); if (!selected) setStroke(ToonDp.px(ctx, 1).coerceAtLeast(1), c.line) },
                Gradientspec(18).build(ctx)
            )
            setPadding(ToonDp.px(ctx, 14), 0, ToonDp.px(ctx, 14), 0)
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ToonDp.px(ctx, 36))
            minWidth = ToonDp.px(ctx, 48)
            isClickable = true; isFocusable = true
            setOnClickListener { onClick() }
        }

    /** Home mode chip: icon + label, active = seal tinted fill + seal border. */
    fun modeChip(ctx: Context, c: ToonColors, mode: Int, active: Boolean, onClick: () -> Unit): LinearLayout {
        val seal = ToonTheme.modeColor(c, mode)
        return LinearLayout(ctx).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            isClickable = true; isFocusable = true
            val fg = if (active) seal else c.title
            setPadding(ToonDp.px(ctx, 14), 0, ToonDp.px(ctx, 16), 0)
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ToonDp.px(ctx, 44))
            contentDescription = "${ToonTheme.modeNames[mode]}, ${if (active) "selected" else "not selected"}"
            background = RippleDrawable(
                ColorStateList.valueOf(ToonTheme.withAlpha(seal, 0x1A)),
                GradientDrawable().apply {
                    cornerRadius = ToonDp.px(ctx, 22).toFloat()
                    setColor(if (active) ToonTheme.withAlpha(seal, 0x1F) else c.surfaceAlt)
                    setStroke(ToonDp.px(ctx, 1).coerceAtLeast(1), if (active) seal else c.line)
                },
                Gradientspec(22).build(ctx)
            )
            addView(ImageView(ctx).apply { setImageDrawable(ToonIconDrawable(modeIcon(mode), fg)) },
                LinearLayout.LayoutParams(ToonDp.px(ctx, 18), ToonDp.px(ctx, 18)).also { it.marginEnd = ToonDp.px(ctx, 8) })
            addView(TextView(ctx).apply {
                text = ToonTheme.modeNames[mode]
                textSize = ToonTheme.Type.label
                typeface = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.NORMAL)
                setTextColor(fg)
                includeFontPadding = false
            })
            pressScale(ctx, this)
            setOnClickListener { onClick() }
        }
    }

    fun modeIcon(mode: Int): ToonIc = when (mode) {
        0 -> ToonIc.BOOK
        1 -> ToonIc.SCAN
        2 -> ToonIc.BROADCAST
        else -> ToonIc.CROSSHAIR
    }

    /**
     * Segmented control (2–4 options). Selected thumb is a surface pill with the seal colour ink and
     * springs to the new position. Height 40.
     */
    class Segment(ctx: Context, private val c: ToonColors, private val labels: List<String>, private val seal: Int, private val onPick: (Int) -> Unit) : FrameLayout(ctx) {
        private val labelsViews = ArrayList<TextView>()
        private var index = 0
        private val thumb = View(ctx).apply { background = GradientDrawable().apply { cornerRadius = ToonDp.px(ctx, 11).toFloat(); setColor(c.surface); setStroke(ToonDp.px(ctx, 1).coerceAtLeast(1), c.lineStrong) } }
        private var anim: ValueAnimator? = null

        init {
            background = GradientDrawable().apply { cornerRadius = ToonDp.px(ctx, 14).toFloat(); setColor(c.surfaceAlt) }
            val pad = ToonDp.px(ctx, 3)
            setPadding(pad, pad, pad, pad)
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ToonDp.px(ctx, 40))
            addView(thumb, LayoutParams(0, LayoutParams.MATCH_PARENT))
            val row = LinearLayout(ctx).apply { orientation = LinearLayout.HORIZONTAL }
            labels.forEachIndexed { i, label ->
                val t = TextView(ctx).apply {
                    text = label
                    textSize = ToonTheme.Type.label
                    gravity = Gravity.CENTER
                    includeFontPadding = false
                    setTextColor(c.muted)
                    isClickable = true; isFocusable = true
                    background = ripple(ctx, Gradientspec(11), c)
                    contentDescription = "$label, tab ${i + 1} of ${labels.size}"
                    setOnClickListener {
                        if (index != i) { index = i; onPick(i); restyle(); slide() }
                    }
                }
                labelsViews.add(t)
                row.addView(t, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f))
            }
            addView(row, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
            post { thumb.layoutParams = LayoutParams(Math.max(0, width - paddingLeft - paddingRight) / labels.size, LayoutParams.MATCH_PARENT); thumb.requestLayout() }
        }

        fun select(i: Int, animate: Boolean) {
            index = i.coerceIn(0, labels.size - 1)
            restyle()
            if (animate) slide() else snap()
        }

        private fun restyle() {
            labelsViews.forEachIndexed { i, t ->
                t.setTextColor(if (i == index) seal else c.muted)
                t.typeface = android.graphics.Typeface.create("sans-serif-medium", if (i == index) android.graphics.Typeface.BOLD else android.graphics.Typeface.NORMAL)
            }
        }

        private fun snap() {
            anim?.cancel()
            val w = (width - paddingLeft - paddingRight).toFloat() / labels.size
            thumb.x = paddingLeft + w * index
        }

        private fun slide() {
            anim?.cancel()
            val w = (width - paddingLeft - paddingRight).toFloat() / labels.size
            if (w <= 0f) return
            val from = thumb.x
            val to = paddingLeft + w * index
            anim = ValueAnimator.ofFloat(from, to).apply {
                duration = dur(context, ToonTheme.Motion.base)
                interpolator = OvershootInterpolator(ToonTheme.Motion.spring)
                addUpdateListener { thumb.x = it.animatedValue as Float }
                start()
            }
        }

        override fun onSizeChanged(w: Int, h: Int, ow: Int, oh: Int) {
            super.onSizeChanged(w, h, ow, oh)
            thumb.layoutParams = LayoutParams((w - paddingLeft - paddingRight) / labels.size, LayoutParams.MATCH_PARENT)
            snap()
        }
    }

    // ================================================================== surfaces

    /** Card panel: surface fill, 1 dp line, radius 16, padding 16. */
    fun card(ctx: Context, c: ToonColors, padDp: Int = 16, bordered: Boolean = true, rad: Int = ToonTheme.Radius.card): LinearLayout =
        LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(ToonDp.px(ctx, padDp), ToonDp.px(ctx, padDp), ToonDp.px(ctx, padDp), ToonDp.px(ctx, padDp))
            background = GradientDrawable().apply {
                cornerRadius = ToonDp.px(ctx, rad).toFloat()
                setColor(c.surface)
                if (bordered) setStroke(ToonDp.px(ctx, 1).coerceAtLeast(1), c.line)
            }
        }

    /** Grouped-list container: surface, radius 16, rows added by [listRow]; dividers inset 16. */
    fun group(ctx: Context, c: ToonColors): LinearLayout =
        LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            background = GradientDrawable().apply { cornerRadius = ToonDp.px(ctx, ToonTheme.Radius.group).toFloat(); setColor(c.surface) }
        }

    fun divider(ctx: Context, c: ToonColors, insetStart: Int = 16): View =
        View(ctx).apply {
            setBackgroundColor(c.line)
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ToonDp.px(ctx, 1)).also { it.marginStart = ToonDp.px(ctx, insetStart) }
        }

    /**
     * Grouped list row: optional leading icon, title, value text or toggle, chevron. Min height 52.
     * [toggle] non-null renders a switch instead of a chevron; returns the row and lets the caller
     * refresh the value text with [Row.setValue].
     */
    class Row(ctx: Context, val title: TextView, val value: TextView) : LinearLayout(ctx) {
        fun setValue(v: String) { value.text = v }
    }

    fun listRow(
        ctx: Context,
        c: ToonColors,
        titleText: String,
        valueText: String? = null,
        icon: ToonIc? = null,
        destructive: Boolean = false,
        detail: String? = null,
        chevron: Boolean = true,
        onClick: (() -> Unit)? = null
    ): Row {
        val titleView = TextView(ctx).apply {
            text = titleText
            textSize = ToonTheme.Type.body
            typeface = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.NORMAL)
            setTextColor(if (destructive) c.dangerInk else c.title)
            includeFontPadding = false
            maxLines = 2
        }
        val valueView = TextView(ctx).apply {
            text = valueText ?: ""
            textSize = ToonTheme.Type.caption
            setTextColor(c.muted)
            includeFontPadding = false
            maxLines = 1
            gravity = Gravity.END
            visibility = if (valueText.isNullOrEmpty() && !chevron) View.GONE else View.VISIBLE
        }
        val row = Row(ctx, titleView, valueView).apply {
            orientation = HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            minimumHeight = ToonDp.px(ctx, if (detail != null) 64 else 52)
            val padH = ToonDp.px(ctx, 16)
            setPadding(padH, ToonDp.px(ctx, 12), padH, ToonDp.px(ctx, 12))
            if (onClick != null) {
                isClickable = true; isFocusable = true
                background = ripple(ctx, Gradientspec(0), c)
                setOnClickListener { onClick() }
            }
            if (icon != null) {
                addView(ImageView(ctx).apply { setImageDrawable(ToonIconDrawable(icon, if (destructive) c.dangerInk else c.muted)) },
                    LayoutParams(ToonDp.px(ctx, 22), ToonDp.px(ctx, 22)).also { it.marginEnd = ToonDp.px(ctx, 14) })
            }
            val lines = LinearLayout(ctx).apply { orientation = VERTICAL; gravity = Gravity.CENTER_VERTICAL }
            lines.addView(titleView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT))
            if (detail != null) lines.addView(TextView(ctx).apply {
                text = detail
                textSize = ToonTheme.Type.caption
                setTextColor(c.muted)
                includeFontPadding = false
                setPadding(0, ToonDp.px(ctx, 2), 0, 0)
            }, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT))
            addView(lines, LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f))
            if (!valueText.isNullOrEmpty()) addView(valueView, LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).also { it.marginStart = ToonDp.px(ctx, 12) })
            if (chevron) addView(ImageView(ctx).apply { setImageDrawable(ToonIconDrawable(ToonIc.CHEVRON, c.muted)) },
                LayoutParams(ToonDp.px(ctx, 16), ToonDp.px(ctx, 16)).also { it.marginStart = ToonDp.px(ctx, 8) })
        }
        return row
    }

    /** Toggle row: title + optional detail, trailing switch. [onChanged] receives the new state. */
    fun toggleRow(ctx: Context, c: ToonColors, titleText: String, detail: String?, initial: Boolean, onChanged: (Boolean) -> Unit): Row {
        var on = initial
        val row = listRow(ctx, c, titleText, null, null, false, detail, chevron = false)
        val sw = SwitchView(ctx, c, on)
        row.addView(sw, LayoutParams(ToonDp.px(ctx, 44), ToonDp.px(ctx, 26)).also { it.marginStart = ToonDp.px(ctx, 12) })
        row.isClickable = true; row.isFocusable = true
        row.background = ripple(ctx, Gradientspec(0), c)
        row.setOnClickListener {
            on = !on
            sw.setChecked(on, animate = true)
            onChanged(on)
        }
        sw.contentDescription = titleText
        return row
    }

    /** Small 44×26 pill switch. */
    class SwitchView(ctx: Context, private val c: ToonColors, checked: Boolean) : View(ctx) {
        private var on = checked
        private var pos = if (checked) 1f else 0f
        private val track = Paint(Paint.ANTI_ALIAS_FLAG)
        private val knob = Paint(Paint.ANTI_ALIAS_FLAG)
        private val rect = RectF()

        init { importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_NO }

        fun setChecked(v: Boolean, animate: Boolean) {
            if (on == v) return
            on = v
            if (!animate || Toon.reducedMotion(context)) pos = if (v) 1f else 0f else ValueAnimator.ofFloat(pos, if (v) 1f else 0f).apply {
                duration = dur(context, ToonTheme.Motion.base)
                interpolator = DecelerateInterpolator()
                addUpdateListener { pos = it.animatedValue as Float; invalidate() }
                start()
            }
            invalidate()
        }

        override fun onDraw(canvas: Canvas) {
            val h = height.toFloat()
            val r = h / 2f
            track.color = ToonTheme.lerp(c.lineStrong, c.accent, pos)
            rect.set(0f, 0f, width.toFloat(), h)
            canvas.drawRoundRect(rect, r, r, track)
            val kr = r - ToonDp.px(context, 2)
            val cx = r + (width - h) * pos
            knob.color = c.surface
            canvas.drawCircle(cx, r, kr, knob)
        }
    }

    /** Section label in the margin (all caps, letterspaced). */
    fun sectionLabel(ctx: Context, c: ToonColors, text: String): TextView =
        ToonText.section(ctx, c, text).apply {
            setPadding(ToonDp.px(ctx, 4), ToonDp.px(ctx, 18), ToonDp.px(ctx, 4), ToonDp.px(ctx, 8))
        }

    /** Search / text field, 48 dp. Returns the EditText; styling includes focus stroke via state list. */
    fun field(ctx: Context, c: ToonColors, hint: String, multiLine: Boolean = false): android.widget.EditText =
        android.widget.EditText(ctx).apply {
            this.hint = hint
            textSize = ToonTheme.Type.body
            setTextColor(c.title)
            setHintTextColor(c.muted)
            includeFontPadding = false
            setPadding(ToonDp.px(ctx, 14), ToonDp.px(ctx, 12), ToonDp.px(ctx, 14), ToonDp.px(ctx, 12))
            background = GradientDrawable().apply { cornerRadius = ToonDp.px(ctx, ToonTheme.Radius.field).toFloat(); setColor(c.surface); setStroke(ToonDp.px(ctx, 1).coerceAtLeast(1), c.lineStrong) }
            if (multiLine) {
                gravity = Gravity.TOP or Gravity.START
                minLines = 6
                inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE
            } else setSingleLine(true)
        }

    /** Snackbar: ink pill, one at a time, optional action. */
    class Snack(ctx: Context, private val c: ToonColors) : LinearLayout(ctx) {
        val text: TextView = TextView(ctx).apply {
            textSize = ToonTheme.Type.caption
            setTextColor(c.onInk)
            includeFontPadding = false
            maxLines = 3
        }
        var action: TextView? = null

        init {
            orientation = HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(ToonDp.px(ctx, 16), ToonDp.px(ctx, 12), ToonDp.px(ctx, 8), ToonDp.px(ctx, 12))
            minimumHeight = ToonDp.px(ctx, 48)
            background = GradientDrawable().apply { cornerRadius = ToonDp.px(ctx, ToonTheme.Radius.field).toFloat(); setColor(c.ink) }
            elevation = ToonDp.px(ctx, 6).toFloat()
            addView(text, LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f))
            visibility = GONE
        }

        fun show(msg: String, actionLabel: String?, onAction: (() -> Unit)?) {
            text.text = msg
            action?.let { removeView(it) }
            action = if (actionLabel != null) TextView(ctx).apply {
                text = actionLabel
                textSize = ToonTheme.Type.caption
                typeface = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.BOLD)
                setTextColor(c.bg)
                setPadding(ToonDp.px(ctx, 12), ToonDp.px(ctx, 8), ToonDp.px(ctx, 12), ToonDp.px(ctx, 8))
                isClickable = true; isFocusable = true
                setOnClickListener { onAction?.invoke() }
            } else null
            action?.let { addView(it) }
            animate().cancel()
            visibility = VISIBLE
            alpha = 0f; translationY = ToonDp.px(ctx, 12).toFloat()
            animate().alpha(1f).translationY(0f).setDuration(dur(ctx, ToonTheme.Motion.base)).setInterpolator(DecelerateInterpolator()).start()
        }

        fun hide() {
            animate().cancel()
            animate().alpha(0f).translationY(ToonDp.px(ctx, 12).toFloat()).setDuration(dur(ctx, ToonTheme.Motion.base)).withEndAction {
                visibility = GONE
            }.start()
        }
    }

    /** Inline banner: warn / info / danger. */
    fun banner(ctx: Context, c: ToonColors, kind: Int, text: String, actionLabel: String? = null, onAction: (() -> Unit)? = null): LinearLayout {
        val bg = when (kind) { 1 -> c.infoBg; 2 -> c.dangerBg; else -> c.warnBg }
        val ink = when (kind) { 1 -> c.infoInk; 2 -> c.dangerInk; else -> c.warnInk }
        return LinearLayout(ctx).apply {
            orientation = HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(ToonDp.px(ctx, 12), ToonDp.px(ctx, 10), ToonDp.px(ctx, 8), ToonDp.px(ctx, 10))
            background = GradientDrawable().apply { cornerRadius = ToonDp.px(ctx, 14).toFloat(); setColor(bg) }
            addView(ImageView(ctx).apply { setImageDrawable(ToonIconDrawable(if (kind == 1) ToonIc.INFO else ToonIc.WARNING, ink)) },
                LinearLayout.LayoutParams(ToonDp.px(ctx, 18), ToonDp.px(ctx, 18)).also { it.marginEnd = ToonDp.px(ctx, 10) })
            addView(TextView(ctx).apply {
                this.text = text
                textSize = ToonTheme.Type.caption
                setTextColor(ink)
                includeFontPadding = false
            }, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
            if (actionLabel != null) addView(TextView(ctx).apply {
                text = actionLabel
                textSize = ToonTheme.Type.caption
                typeface = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.BOLD)
                setTextColor(ink)
                setPadding(ToonDp.px(ctx, 12), ToonDp.px(ctx, 8), ToonDp.px(ctx, 8), ToonDp.px(ctx, 8))
                isClickable = true
                setOnClickListener { onAction?.invoke() }
            })
        }
    }

    /** Badge: neutral count or ON/OFF state. */
    fun badge(ctx: Context, c: ToonColors, text: String, kind: Int = 0): TextView =
        TextView(ctx).apply {
            this.text = text
            textSize = ToonTheme.Type.micro
            typeface = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.BOLD)
            gravity = Gravity.CENTER
            includeFontPadding = false
            val bg = when (kind) { 1 -> ToonTheme.withAlpha(c.success, 0x24); 2 -> c.dangerBg; else -> c.surfaceAlt }
            val fg = when (kind) { 1 -> c.success; 2 -> c.dangerInk; else -> c.muted }
            setTextColor(fg)
            setPadding(ToonDp.px(ctx, 8), ToonDp.px(ctx, 3), ToonDp.px(ctx, 8), ToonDp.px(ctx, 3))
            background = GradientDrawable().apply { cornerRadius = ToonDp.px(ctx, ToonTheme.Radius.badge).toFloat(); setColor(bg) }
            minHeight = ToonDp.px(ctx, 20)
        }

    // ================================================================== states

    /** Centred empty/error block: mascot, title, caption, optional action. */
    fun stateBlock(ctx: Context, c: ToonColors, titleText: String, captionText: String, actionLabel: String? = null, onAction: (() -> Unit)? = null): LinearLayout =
        LinearLayout(ctx).apply {
            orientation = VERTICAL
            gravity = Gravity.CENTER
            setPadding(ToonDp.px(ctx, 24), ToonDp.px(ctx, 40), ToonDp.px(ctx, 24), ToonDp.px(ctx, 40))
            addView(MascotView(ctx, 96), LinearLayout.LayoutParams(ToonDp.px(ctx, 96), ToonDp.px(ctx, 96)))
            addView(TextView(ctx).apply {
                text = titleText
                textSize = 17f
                typeface = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.NORMAL)
                setTextColor(c.title)
                gravity = Gravity.CENTER
                setPadding(0, ToonDp.px(ctx, 12), 0, ToonDp.px(ctx, 4))
            })
            addView(TextView(ctx).apply {
                text = captionText
                textSize = ToonTheme.Type.label
                setTextColor(c.muted)
                gravity = Gravity.CENTER
                setLineSpacing(0f, 1.4f)
            })
            if (actionLabel != null) addView(buttonPrimary(ctx, c, actionLabel) { onAction?.invoke() },
                LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ToonDp.px(ctx, 48)).also { it.topMargin = ToonDp.px(ctx, 20); it.gravity = Gravity.CENTER_HORIZONTAL }).also { lp ->
                (lp as? LinearLayout.LayoutParams)?.let { wp ->
                    wp.minWidth = ToonDp.px(ctx, 160)
                }
            })
        }

    /** Shimmering skeleton block. Animates only while attached and not in reduced motion. */
    class Skeleton(ctx: Context, private val c: ToonColors, private val radDp: Int = 8) : View(ctx) {
        private val p = Paint(Paint.ANTI_ALIAS_FLAG)
        private var phase = 0f
        private val anim = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 1200; repeatCount = ValueAnimator.INFINITE; interpolator = LinearInterpolator()
            addUpdateListener { phase = it.animatedValue as Float; invalidate() }
        }

        init { importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_NO }

        override fun onAttachedToWindow() {
            super.onAttachedToWindow()
            if (!Toon.reducedMotion(context)) anim.start()
        }

        override fun onDetachedFromWindow() {
            anim.cancel(); super.onDetachedFromWindow()
        }

        override fun onDraw(canvas: Canvas) {
            val r = ToonDp.px(context, radDp).toFloat()
            p.color = c.skeleton
            val rect = RectF(0f, 0f, width.toFloat(), height.toFloat())
            canvas.drawRoundRect(rect, r, r, p)
            val w = width.toFloat()
            val band = w * 0.35f
            val x = -band + (w + band) * phase
            p.shader = android.graphics.LinearGradient(x, 0f, x + band, 0f,
                intArrayOf(0x00FFFFFF, if (c.dark) 0x0FFFFFFF else 0x66FFFFFF, 0x00FFFFFF), null, android.graphics.Shader.TileMode.CLAMP)
            canvas.drawRoundRect(rect, r, r, p)
            p.shader = null
        }
    }

    /** Thin progress line, 2 dp (top of page) or 6 dp (card). Determinate 0..100 or indeterminate. */
    class ProgressLine(ctx: Context, private val c: ToonColors, private val thickDp: Int = 2) : View(ctx) {
        private val track = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = c.line }
        private val fillP = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = c.accent }
        private var shown = 0f
        var progress: Int = 0
            set(v) { field = v.coerceIn(0, 100); invalidate() }
        var indeterminate = false
            set(v) { field = v; invalidate(); if (v) anim.start() else anim.cancel() }
        private val anim = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 1000; repeatCount = ValueAnimator.INFINITE; interpolator = LinearInterpolator()
            addUpdateListener { shown = it.animatedValue as Float; invalidate() }
        }

        init { importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_NO }

        override fun onDetachedFromWindow() { anim.cancel(); super.onDetachedFromWindow() }

        override fun onDraw(canvas: Canvas) {
            val h = height.toFloat()
            canvas.drawRect(0f, 0f, width.toFloat(), h, track)
            if (indeterminate) {
                val band = width * 0.35f
                val x = -band + (width + band) * shown
                canvas.drawRect(maxOf(0f, x), 0f, minOf(width.toFloat(), x + band), h, fillP)
            } else {
                canvas.drawRect(0f, 0f, width * progress / 100f, h, fillP)
            }
        }

        override fun onMeasure(w: Int, h: Int) {
            setMeasuredDimension(resolveSize(0, w), ToonDp.px(context, thickDp))
        }
    }

    /** Chatbot ring for the dock: chapter progress arc, view glyph inside, 44 dp target. */
class ChatRingButton(ctx: Context) : View(ctx) {
    var onClick: (() -> Unit)? = null
    /** -1 = no chapter loaded, otherwise 0..100. */
    var progress: Int = -1
        set(v) { field = v; contentDescription = describe(); invalidate() }
    /** 0 page only, 1 split, 2 chatbot only. */
    var viewMode: Int = 0
        set(v) { field = v; contentDescription = describe(); invalidate() }

    private val c = ToonTheme.colors(ctx)
    private val track = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; strokeCap = Paint.Cap.ROUND; color = c.line }
    private val arc = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; strokeCap = Paint.Cap.ROUND; color = c.accent }
    private val glyph = ToonIconDrawable(ToonIc.CHAT, c.text)

    init {
        isClickable = true; isFocusable = true
        contentDescription = describe()
        setOnClickListener { onClick?.invoke() }
    }

    private fun describe(): String = when (viewMode) {
        0 -> "Open chatbot side by side"
        1 -> "Show chatbot only"
        else -> "Show the page only"
    } + if (progress >= 0) ". Chapter progress $progress percent" else ""

    override fun onDraw(canvas: Canvas) {
        val d = resources.displayMetrics.density
        val stroke = 2.5f * d
        val r = minOf(width, height) / 2f - stroke
        val cx = width / 2f
        val cy = height / 2f
        track.strokeWidth = stroke
        arc.strokeWidth = stroke
        val rect = RectF(cx - r, cy - r, cx + r, cy + r)
        canvas.drawCircle(cx, cy, r, track)
        if (progress >= 0) canvas.drawArc(rect, -90f, 360f * progress / 100f, false, arc)
        val s = (r * 1.05f).toInt()
        glyph.setBounds((cx - s / 2f).toInt(), (cy - s / 2f).toInt(), (cx + s / 2f).toInt(), (cy + s / 2f).toInt())
        glyph.draw(canvas)
    }
}

// ================================================================== dialog / sheet scaffolding

    /** Sheet shell: grabber, title, close, scrollable content, pinned footer. */
    class SheetShell(ctx: Context, val c: ToonColors, titleText: String, private val onClose: () -> Unit) : LinearLayout(ctx) {
        val content: LinearLayout = LinearLayout(ctx).apply { orientation = VERTICAL }
        val footer: LinearLayout = LinearLayout(ctx).apply { orientation = HORIZONTAL; gravity = Gravity.END }
        private val scroll = ScrollView(ctx).apply { isVerticalScrollBarEnabled = false }

        init {
            orientation = VERTICAL
            background = GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(c.surface, c.surface)).apply {
                cornerRadii = floatArrayOf(
                    ToonDp.px(ctx, ToonTheme.Radius.sheet).toFloat(), ToonDp.px(ctx, ToonTheme.Radius.sheet).toFloat(),
                    ToonDp.px(ctx, ToonTheme.Radius.sheet).toFloat(), ToonDp.px(ctx, ToonTheme.Radius.sheet).toFloat(),
                    0f, 0f, 0f, 0f
                )
                setStroke(ToonDp.px(ctx, 1).coerceAtLeast(1), c.line)
            }
            val grabber = View(ctx).apply { background = GradientDrawable().apply { cornerRadius = ToonDp.px(ctx, 2).toFloat(); setColor(c.lineStrong) } }
            addView(grabber, LayoutParams(ToonDp.px(ctx, 36), ToonDp.px(ctx, 4)).also { it.gravity = Gravity.CENTER_HORIZONTAL; it.topMargin = ToonDp.px(ctx, 8) })
            val head = LinearLayout(ctx).apply {
                orientation = HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(ToonDp.px(ctx, 16), ToonDp.px(ctx, 8), ToonDp.px(ctx, 8), ToonDp.px(ctx, 4))
                addView(ToonText.title(ctx, c, titleText).apply {
                    maxLines = 2
                }, LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f))
                addView(iconButton(ctx, c, ToonIc.CLOSE, "Close", c.muted) { onClose() })
            }
            addView(head, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT))
            scroll.addView(content, ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT))
            addView(scroll, LayoutParams(LayoutParams.MATCH_PARENT, 0, 1f))
            addView(footer, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).also {
                it.setMargins(ToonDp.px(ctx, 16), ToonDp.px(ctx, 8), ToonDp.px(ctx, 16), ToonDp.px(ctx, 16))
            })
        }

        fun addContent(v: View, lp: LayoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)) {
            content.addView(v, lp)
        }
    }

    /**
     * Bottom sheet dialog. Returns the shell; caller fills content and footer, then [SheetDialog.show].
     */
    class SheetDialog(val ctx: Context, val colors: ToonColors, titleText: String) {
        val shell: SheetShell = SheetShell(ctx, colors, titleText) { close() }
        private val dialog: android.app.Dialog = android.app.Dialog(ctx).apply {
            requestWindowFeature(android.view.Window.FEATURE_NO_TITLE)
            setCanceledOnTouchOutside(true)
            setContentView(shell)
            window?.apply {
                setBackgroundDrawable(android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT))
                setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                setGravity(Gravity.BOTTOM)
                setDimAmount(if (colors.dark) 0.6f else 0.4f)
                setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
                decorView.setPadding(0, 0, 0, 0)
            }
        }

        fun show() {
            dialog.show()
            val v = dialog.window?.decorView?.findViewById<View>(android.R.id.content)
            if (v != null && !Toon.reducedMotion(ctx)) {
                v.alpha = 0f
                v.post {
                    v.translationY = v.height * 0.25f
                    v.animate().translationY(0f).alpha(1f).setDuration(dur(ctx, ToonTheme.Motion.base)).setInterpolator(DecelerateInterpolator()).start()
                }
            }
        }

        fun close() = dialog.dismiss()
        val isShowing: Boolean get() = dialog.isShowing
    }

    /** Centre dialog: title, body, optional content, stacked actions. */
    class Dialog(ctx: Context, val colors: ToonColors, titleText: String, bodyText: String?) {
        val root: LinearLayout = LinearLayout(ctx).apply {
            orientation = VERTICAL
            setPadding(ToonDp.px(ctx, 20), ToonDp.px(ctx, 20), ToonDp.px(ctx, 20), ToonDp.px(ctx, 16))
            background = GradientDrawable().apply { cornerRadius = ToonDp.px(ctx, ToonTheme.Radius.dialog).toFloat(); setColor(colors.surface); setStroke(ToonDp.px(ctx, 1).coerceAtLeast(1), colors.line) }
            addView(ToonText.title(ctx, colors, titleText).apply { maxLines = 3 })
            if (bodyText != null) addView(TextView(ctx).apply {
                text = bodyText
                textSize = ToonTheme.Type.body
                setTextColor(colors.muted)
                setLineSpacing(0f, 1.35f)
                setPadding(0, ToonDp.px(ctx, 8), 0, 0)
            })
        }
        val actions: LinearLayout = LinearLayout(ctx).apply { orientation = VERTICAL; setPadding(0, ToonDp.px(ctx, 12), 0, 0) }
        private val dialog: android.app.Dialog = android.app.Dialog(ctx).apply {
            requestWindowFeature(android.view.Window.FEATURE_NO_TITLE)
            val wrap = LinearLayout(ctx).apply { setPadding(ToonDp.px(ctx, 20), 0, ToonDp.px(ctx, 20), 0); addView(root) }
            setContentView(wrap)
            window?.apply {
                setBackgroundDrawable(android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT))
                setLayout(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT)
                setGravity(Gravity.CENTER)
                setDimAmount(if (colors.dark) 0.6f else 0.4f)
            }
            setOnDismissListener { onDismissHook?.invoke() }
        }

        /** Called after the dialog is dismissed for any reason (button, back, app leaving). */
        var onDismissHook: (() -> Unit)? = null

        init { root.addView(actions, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)) }

        fun addAction(label: String, style: Int = 0, onClick: () -> Unit) {
            val v = when (style) {
                2 -> buttonDanger(ctx, colors, label, onClick)
                1 -> buttonSecondary(ctx, colors, label, onClick)
                else -> buttonPrimary(ctx, colors, label, onClick)
            }
            actions.addView(v, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).also { it.topMargin = ToonDp.px(ctx, 8) })
        }

        fun addActionRow(vararg buttons: View) {
            val row = LinearLayout(ctx).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.END }
            buttons.forEach { row.addView(it) }
            actions.addView(row, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT))
        }

        fun addContent(v: View) {
            root.addView(v, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).also { it.topMargin = ToonDp.px(ctx, 12) })
        }

        fun show() = dialog.show()
        fun dismiss() = dialog.dismiss()
        val isShowing: Boolean get() = dialog.isShowing
    }
}
