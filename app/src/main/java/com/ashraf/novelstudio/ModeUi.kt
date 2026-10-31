package com.ashraf.novelstudio

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.view.MotionEvent
import android.view.View
import android.view.animation.LinearInterpolator
import android.view.animation.DecelerateInterpolator
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

/**
 * Reading-mode identity shared by the dock capsule, the extract button and the centre animation.
 * Index 0 = Novel, 1 = Scan (WebToon scanning), 2 = Live (WebToon real-time).
 */
object ModeUi {
    val names = arrayOf("Novel", "Scan", "Live")
    private val starts = intArrayOf(UiTheme.accent, UiTheme.ok, UiTheme.accent)
    private val ends = intArrayOf(UiTheme.accent, UiTheme.ok, UiTheme.accent)

    fun start(i: Int): Int = starts[i.coerceIn(0, 2)]
    fun end(i: Int): Int = ends[i.coerceIn(0, 2)]
    fun mid(i: Int): Int = lerp(start(i), end(i), 0.5f)

    /** [c] with its alpha replaced by [a] (0..255). */
    fun withAlpha(c: Int, a: Int): Int = (c and 0x00FFFFFF) or (a.coerceIn(0, 255) shl 24)

    fun lerp(a: Int, b: Int, t: Float): Int {
        val k = t.coerceIn(0f, 1f)
        return Color.argb(
            (Color.alpha(a) + (Color.alpha(b) - Color.alpha(a)) * k).toInt(),
            (Color.red(a) + (Color.red(b) - Color.red(a)) * k).toInt(),
            (Color.green(a) + (Color.green(b) - Color.green(a)) * k).toInt(),
            (Color.blue(a) + (Color.blue(b) - Color.blue(a)) * k).toInt()
        )
    }
}

/** Draws the three mode glyphs (book / scan frame / broadcast) on a 24-unit grid. */
class ModeGlyphPainter {
    private val stroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    private val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val path = Path()
    private val rect = RectF()

    fun draw(c: Canvas, mode: Int, cx: Float, cy: Float, size: Float, color: Int) {
        val s = size / 24f
        c.save()
        c.translate(cx - size / 2f, cy - size / 2f)
        c.scale(s, s)
        stroke.color = color
        stroke.strokeWidth = 1.9f
        fill.color = color
        when (mode) {
            0 -> {
                path.reset()
                path.moveTo(12f, 6.5f)
                path.cubicTo(10f, 4.8f, 6.5f, 4.5f, 3.5f, 5.2f)
                path.lineTo(3.5f, 18.2f)
                path.cubicTo(6.5f, 17.5f, 10f, 17.8f, 12f, 19.5f)
                path.cubicTo(14f, 17.8f, 17.5f, 17.5f, 20.5f, 18.2f)
                path.lineTo(20.5f, 5.2f)
                path.cubicTo(17.5f, 4.5f, 14f, 4.8f, 12f, 6.5f)
                path.close()
                c.drawPath(path, stroke)
                c.drawLine(12f, 6.5f, 12f, 19.5f, stroke)
            }
            1 -> {
                path.reset()
                path.moveTo(4f, 9f); path.lineTo(4f, 4f); path.lineTo(9f, 4f)
                path.moveTo(15f, 4f); path.lineTo(20f, 4f); path.lineTo(20f, 9f)
                path.moveTo(4f, 15f); path.lineTo(4f, 20f); path.lineTo(9f, 20f)
                path.moveTo(15f, 20f); path.lineTo(20f, 20f); path.lineTo(20f, 15f)
                c.drawPath(path, stroke)
                c.drawLine(3f, 12f, 21f, 12f, stroke)
            }
            else -> {
                c.drawCircle(12f, 12f, 2.2f, fill)
                for (r in floatArrayOf(5.5f, 9.5f)) {
                    rect.set(12f - r, 12f - r, 12f + r, 12f + r)
                    c.drawArc(rect, -42f, 84f, false, stroke)
                    c.drawArc(rect, 138f, 84f, false, stroke)
                }
            }
        }
        c.restore()
    }
}

// ============================================================================ dock capsule

/** Three-way reading-mode switch: icons only, a coloured thumb that slides with a little spring. */
class ModeCapsule(ctx: Context) : View(ctx) {
    var onSelect: ((Int) -> Unit)? = null

    private val dens = ctx.resources.displayMetrics.density
    private val glyph = ModeGlyphPainter()
    private val track = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL; color = UiTheme.sheet }
    private val trackStroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 1f * dens
        color = UiTheme.strokeSoft
    }
    private val thumb = Paint(Paint.ANTI_ALIAS_FLAG)
    private val rect = RectF()
    private val pad = 3f * dens

    private var selected = 0
    private var pos = 0f
    private var anim: ValueAnimator? = null

    init {
        isClickable = true
        isFocusable = true
        isHapticFeedbackEnabled = false
        contentDescription = "Reading mode: ${ModeUi.names[0]}"
    }

    /** Bounds of one icon slot in this view's own coordinates (used by the guided tour). */
    fun segmentBounds(i: Int): RectF {
        val segW = (width - 2f * pad) / 3f
        return RectF(pad + segW * i, pad, pad + segW * (i + 1), height - pad)
    }

    /** Returns true when the selection actually changed. */
    fun setSelectedIndex(i: Int, animate: Boolean): Boolean {
        val target = i.coerceIn(0, 2)
        if (target == selected) return false
        selected = target
        contentDescription = "Reading mode: ${ModeUi.names[target]}"
        anim?.cancel()
        if (animate && isAttachedToWindow && width > 0) {
            anim = ValueAnimator.ofFloat(pos, target.toFloat()).apply {
                duration = 180L
                interpolator = DecelerateInterpolator()
                addUpdateListener { pos = it.animatedValue as Float; invalidate() }
                start()
            }
        } else {
            pos = target.toFloat()
            invalidate()
        }
        return true
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(
            resolveSize((84f * dens).toInt(), widthMeasureSpec),
            resolveSize((38f * dens).toInt(), heightMeasureSpec)
        )
    }

    override fun onDetachedFromWindow() {
        anim?.cancel()
        pos = selected.toFloat()
        super.onDetachedFromWindow()
    }

    override fun onDraw(c: Canvas) {
        val w = width.toFloat()
        val h = height.toFloat()
        val r = h / 2f
        rect.set(0f, 0f, w, h)
        c.drawRoundRect(rect, r, r, track)
        val half = trackStroke.strokeWidth / 2f
        rect.set(half, half, w - half, h - half)
        c.drawRoundRect(rect, r - half, r - half, trackStroke)

        val segW = (w - 2f * pad) / 3f
        val th = h - 2f * pad
        val left = pad + pos * segW
        rect.set(left, pad, left + segW, pad + th)

        val a = pos.toInt().coerceIn(0, 1)
        val t = pos - a
        val c0 = ModeUi.lerp(ModeUi.start(a), ModeUi.start(a + 1), t)
        val c1 = ModeUi.lerp(ModeUi.end(a), ModeUi.end(a + 1), t)
        thumb.shader = null
        thumb.color = c0
        c.drawRoundRect(rect, th / 2f, th / 2f, thumb)

        for (i in 0..2) {
            val cx = pad + segW * (i + 0.5f)
            val d = min(1f, abs(pos - i))
            glyph.draw(c, i, cx, h / 2f, 17f * dens, ModeUi.lerp(UiTheme.onAccent, UiTheme.iconIdle, d))
        }
    }

    override fun onTouchEvent(e: MotionEvent): Boolean {
        when (e.actionMasked) {
            MotionEvent.ACTION_DOWN -> return true
            MotionEvent.ACTION_UP -> {
                val segW = (width - 2f * pad) / 3f
                val i = ((e.x - pad) / segW).toInt().coerceIn(0, 2)
                performClick()
                onSelect?.invoke(i)
                return true
            }
        }
        return super.onTouchEvent(e)
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }
}

// ============================================================================ centre-of-page animation

/**
 * Plays a short "scanner" style animation in the middle of the page when the reading mode changes:
 *  - Novel: a page writes itself line by line and gets a bookmark ribbon
 *  - Scan:  corner brackets snap onto a document while a light beam sweeps down and back
 *  - Live:  a pulsing dot sends out broadcast ripples
 * It never takes touches, so the page underneath stays usable.
 */
/** No modal mode animation: the dock itself shows the selected mode. */
class ModeSwitchOverlay(ctx: Context) : View(ctx) {
    init {
        visibility = GONE
        isClickable = false
        isFocusable = false
        importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_NO
    }

    fun play(mode: Int) {
        // Intentionally quiet; the actual mode control updates in the dock.
        visibility = GONE
    }
}
