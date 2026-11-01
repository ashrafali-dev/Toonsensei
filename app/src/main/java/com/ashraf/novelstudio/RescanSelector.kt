package com.ashraf.novelstudio

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.DashPathEffect
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView

/**
 * "Scanner" frame for re-reading one speech bubble: a rectangle with six dots (four corners plus the
 * middle of the top and bottom edge). Drag a dot to resize, drag inside to move. Only the framed area is read again.
 * [onScan] receives the frame as fractions (left, top, right, bottom) of THIS view.
 */
class RescanSelector(
    ctx: Context,
    private val accent: Int,
    private val onScan: (Float, Float, Float, Float) -> Unit,
    private val onCancel: () -> Unit
) : FrameLayout(ctx) {
    private val d = ctx.resources.displayMetrics.density
    private var l = 0f
    private var t = 0f
    private var r = 0f
    private var b = 0f
    private var ready = false
    private val bar: LinearLayout
    private val baseBarMargin = (28 * d).toInt()
    private var lift = 0
    private var drag = -1                 // 0..5 = dot, 6 = whole frame
    private var lx = 0f
    private var ly = 0f

    private val dimPaint = Paint().apply { color = 0x70000000 }
    // Spec board 17: dashed Seal frame over a faint Seal tint; six dots are Seal with a light halo.
    private val framePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = accent
        style = Paint.Style.STROKE
        strokeWidth = 2f * d
        pathEffect = DashPathEffect(floatArrayOf(8 * d, 6 * d), 0f)
    }
    private val frameFill = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = ModeUi.withAlpha(accent, 0x12) }
    private val dotFill = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = accent }
    private val dotRing = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = UiTheme.text; style = Paint.Style.STROKE; strokeWidth = 2f * d }

    init {
        setWillNotDraw(false)
        isClickable = true
        bar = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
        }
        // Board 17: two circular actions stacked on the right edge - Read (Seal) and Cancel (surface).
        fun circleBtn(ic: Ic, label: String, filled: Boolean, desc: String, click: () -> Unit): View {
            val inner = LinearLayout(ctx).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                contentDescription = desc
                background = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(if (filled) accent else UiTheme.surface1)
                    if (!filled) setStroke((1 * d).toInt().coerceAtLeast(1), UiTheme.stroke)
                }
                addView(ImageView(ctx).apply {
                    setImageDrawable(IconDrawable(ic, if (filled) Color.WHITE else UiTheme.text))
                }, LinearLayout.LayoutParams((22 * d).toInt(), (22 * d).toInt()))
                addView(TextView(ctx).apply {
                    text = label
                    textSize = 11f
                    typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
                    setTextColor(if (filled) Color.WHITE else UiTheme.text)
                    gravity = Gravity.CENTER
                }, LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).also { it.topMargin = (2 * d).toInt() })
                setOnClickListener { click() }
            }
            return inner
        }
        bar.addView(circleBtn(Ic.CHECK, "Read", true, "Read this area") {
            if (width > 0 && height > 0) onScan(l / width, t / height, r / width, b / height)
        }, LinearLayout.LayoutParams((64 * d).toInt(), (64 * d).toInt()))
        bar.addView(circleBtn(Ic.CLOSE, "Cancel", false, "Cancel") { onCancel() }, LinearLayout.LayoutParams((64 * d).toInt(), (64 * d).toInt()).also {
            it.topMargin = (10 * d).toInt()
        })
        addView(bar, LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, Gravity.CENTER_VERTICAL or Gravity.END).also {
            it.rightMargin = (10 * d).toInt()
        })
    }

    /**
     * Kept API: lifts the action column to match the host's bottom offset (dock overlay mode).
     */
    fun setBarLift(px: Int) {
        val v = px.coerceAtLeast(0)
        if (v == lift) return
        lift = v
    }

    override fun onSizeChanged(w: Int, h: Int, ow: Int, oh: Int) {
        super.onSizeChanged(w, h, ow, oh)
        if (!ready && w > 0 && h > 0) {
            l = w * 0.10f; r = w * 0.90f
            t = h * 0.40f; b = h * 0.54f
            ready = true
        }
    }

    private fun dots(): Array<FloatArray> = arrayOf(
        floatArrayOf(l, t), floatArrayOf(r, t), floatArrayOf(l, b), floatArrayOf(r, b),
        floatArrayOf((l + r) / 2f, t), floatArrayOf((l + r) / 2f, b)
    )

    override fun onDraw(c: Canvas) {
        super.onDraw(c)
        if (!ready) return
        val w = width.toFloat()
        val h = height.toFloat()
        c.drawRect(0f, 0f, w, t, dimPaint)
        c.drawRect(0f, b, w, h, dimPaint)
        c.drawRect(0f, t, l, b, dimPaint)
        c.drawRect(r, t, w, b, dimPaint)
        c.drawRect(l, t, r, b, frameFill)
        c.drawRoundRect(android.graphics.RectF(l, t, r, b), 6 * d, 6 * d, framePaint)
        for (p in dots()) {
            c.drawCircle(p[0], p[1], 8 * d, dotRing)
            c.drawCircle(p[0], p[1], 8 * d - 1 * d, dotFill)
        }
    }

    override fun onTouchEvent(e: MotionEvent): Boolean {
        if (!ready) return true
        when (e.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                drag = -1
                var best = 34 * d
                dots().forEachIndexed { i, p ->
                    val dist = Math.hypot((e.x - p[0]).toDouble(), (e.y - p[1]).toDouble()).toFloat()
                    if (dist < best) { best = dist; drag = i }
                }
                if (drag < 0 && e.x in l..r && e.y in t..b) drag = 6
                lx = e.x; ly = e.y
            }
            MotionEvent.ACTION_MOVE -> {
                if (drag >= 0) {
                    val dx = e.x - lx
                    val dy = e.y - ly
                    val minW = 56 * d
                    val minH = 36 * d
                    when (drag) {
                        0 -> { l += dx; t += dy }
                        1 -> { r += dx; t += dy }
                        2 -> { l += dx; b += dy }
                        3 -> { r += dx; b += dy }
                        4 -> t += dy
                        5 -> b += dy
                        6 -> { l += dx; r += dx; t += dy; b += dy }
                    }
                    if (drag != 6) {
                        if (r - l < minW) { if (drag == 0 || drag == 2) l = r - minW else r = l + minW }
                        if (b - t < minH) { if (drag == 0 || drag == 1 || drag == 4) t = b - minH else b = t + minH }
                    }
                    // keep inside the view
                    val fw = r - l
                    val fh = b - t
                    if (l < 0f) { l = 0f; if (drag == 6) r = fw }
                    if (t < 0f) { t = 0f; if (drag == 6) b = fh }
                    if (r > width) { r = width.toFloat(); if (drag == 6) l = r - fw }
                    if (b > height) { b = height.toFloat(); if (drag == 6) t = b - fh }
                    lx = e.x; ly = e.y
                    invalidate()
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> drag = -1
        }
        return true
    }
}
