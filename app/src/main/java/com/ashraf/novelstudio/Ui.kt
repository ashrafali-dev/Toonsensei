package com.ashraf.novelstudio

import android.animation.ValueAnimator
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.os.Build
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import android.widget.ScrollView
import com.ashraf.novelstudio.ui.theme.ToonTheme
import kotlin.math.cos
import kotlin.math.sin

/**
 * Novel Studio's shared Forest Luxe / Paper design system.
 * Semantic colours, flat surfaces, vector icons, progress controls and sheet helpers
 * live here so the browser chrome and app-owned screens share one palette.
 */
object UiTheme {
    var bg = 0xFF0B0F0D.toInt()
        private set
    var sheet = 0xFF1F2A1F.toInt()
        private set
    var surfaceAlt = 0xFF3E5A3F.toInt()
        private set
    var text = 0xFFE9E7DE.toInt()
        private set
    var dim = 0xFF8CA98C.toInt()
        private set
    var faint = 0xFF6B7F67.toInt()
        private set
    var iconIdle = 0xFFE9E7DE.toInt()
        private set
    var accentA = 0xFFD9C7A3.toInt()
        private set
    var accentB = 0xFFD9C7A3.toInt()
        private set
    var accent = 0xFFD9C7A3.toInt()
        private set
    var accentTint = 0xFF3E5A3F.toInt()
        private set
    var onAccent = 0xFF0B0F0D.toInt()
        private set
    var ok = 0xFF8CA98C.toInt()
        private set
    var danger = 0xFFE58C79.toInt()
        private set
    var stroke = 0xFF6B7F67.toInt()
        private set
    var strokeSoft = 0xFF3E5A3F.toInt()
        private set

    /** Bridge the app's legacy View UI to the shared G1 semantic palette. */
    fun apply(context: Context) {
        val c = ToonTheme.colors(context)
        bg = c.background
        sheet = c.surface
        surfaceAlt = c.surfaceAlt
        text = c.ink
        dim = c.muted
        faint = c.faint
        iconIdle = c.ink
        accentA = c.accent
        accentB = c.accent
        accent = c.accent
        accentTint = c.accentTint
        onAccent = c.onAccent
        ok = c.success
        danger = c.danger
        stroke = c.lineStrong
        strokeSoft = c.line
    }
}

internal fun Context.dpi(v: Number): Int = (v.toFloat() * resources.displayMetrics.density + 0.5f).toInt()
internal fun Context.dpf(v: Number): Float = v.toFloat() * resources.displayMetrics.density

// ============================================================================ icons

enum class Ic {
    BACK, FORWARD, RELOAD, HOME, GRID, DOTS, PLUS, CLOSE, SHARE, SEARCH, BOLT, BOOKMARK, BOOK,
    CHAT, SPLIT, SPARK, SAVE, STOP, GLOBE, CHECK, LOCK, CLOCK, SLIDERS, MOON, SHIELD, TEXT, CHEVRON
}

/** 24x24 stroke icons, drawn at any size. */
class IconDrawable(private val ic: Ic, private var color: Int, private val strokeUnits: Float = 1.9f) : Drawable() {
    private val stroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    private val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val path = Path()
    private val rect = RectF()

    private fun poly(c: Canvas, vararg p: Float, close: Boolean = false) {
        path.reset()
        path.moveTo(p[0], p[1])
        var i = 2
        while (i + 1 < p.size) { path.lineTo(p[i], p[i + 1]); i += 2 }
        if (close) path.close()
        c.drawPath(path, stroke)
    }

    private fun line(c: Canvas, x1: Float, y1: Float, x2: Float, y2: Float) = c.drawLine(x1, y1, x2, y2, stroke)

    private fun arrowHead(c: Canvas, tx: Float, ty: Float, dx: Float, dy: Float, len: Float) {
        val a = 0.75
        for (sign in intArrayOf(-1, 1)) {
            val cs = Math.cos(a * sign).toFloat()
            val sn = Math.sin(a * sign).toFloat()
            val rx = dx * cs - dy * sn
            val ry = dx * sn + dy * cs
            c.drawLine(tx, ty, tx - rx * len, ty - ry * len, stroke)
        }
    }

    override fun draw(canvas: Canvas) {
        val b = bounds
        if (b.isEmpty) return
        val size = minOf(b.width(), b.height()).toFloat()
        val s = size / 24f
        stroke.color = color
        stroke.strokeWidth = strokeUnits
        fill.color = color
        canvas.save()
        canvas.translate(b.left + (b.width() - size) / 2f, b.top + (b.height() - size) / 2f)
        canvas.scale(s, s)
        when (ic) {
            Ic.BACK -> poly(canvas, 15f, 5f, 8f, 12f, 15f, 19f)
            Ic.FORWARD -> poly(canvas, 9f, 5f, 16f, 12f, 9f, 19f)
            Ic.CHEVRON -> poly(canvas, 9f, 6f, 15f, 12f, 9f, 18f)
            Ic.RELOAD -> {
                rect.set(5f, 5f, 19f, 19f)
                canvas.drawArc(rect, -40f, 290f, false, stroke)
                // clockwise tangent at the arc end (250 degrees)
                val th = Math.toRadians(250.0)
                val tx = 12f + 7f * Math.cos(th).toFloat()
                val ty = 12f + 7f * Math.sin(th).toFloat()
                arrowHead(canvas, tx, ty, -Math.sin(th).toFloat(), Math.cos(th).toFloat(), 3.8f)
            }
            Ic.HOME -> {
                poly(canvas, 4f, 11f, 12f, 4f, 20f, 11f)
                poly(canvas, 6f, 10f, 6f, 20f, 18f, 20f, 18f, 10f)
            }
            Ic.GRID -> {
                rect.set(4.5f, 4.5f, 10.5f, 10.5f); canvas.drawRoundRect(rect, 1.8f, 1.8f, stroke)
                rect.set(13.5f, 4.5f, 19.5f, 10.5f); canvas.drawRoundRect(rect, 1.8f, 1.8f, stroke)
                rect.set(4.5f, 13.5f, 10.5f, 19.5f); canvas.drawRoundRect(rect, 1.8f, 1.8f, stroke)
                rect.set(13.5f, 13.5f, 19.5f, 19.5f); canvas.drawRoundRect(rect, 1.8f, 1.8f, stroke)
            }
            Ic.DOTS -> {
                canvas.drawCircle(5.5f, 12f, 1.8f, fill)
                canvas.drawCircle(12f, 12f, 1.8f, fill)
                canvas.drawCircle(18.5f, 12f, 1.8f, fill)
            }
            Ic.PLUS -> { line(canvas, 12f, 5f, 12f, 19f); line(canvas, 5f, 12f, 19f, 12f) }
            Ic.CLOSE -> { line(canvas, 6f, 6f, 18f, 18f); line(canvas, 18f, 6f, 6f, 18f) }
            Ic.SHARE -> {
                poly(canvas, 6f, 12f, 6f, 19f, 18f, 19f, 18f, 12f)
                line(canvas, 12f, 4f, 12f, 15f)
                poly(canvas, 8.5f, 7.5f, 12f, 4f, 15.5f, 7.5f)
            }
            Ic.SEARCH -> {
                canvas.drawCircle(10.5f, 10.5f, 5.5f, stroke)
                line(canvas, 15f, 15f, 20f, 20f)
            }
            Ic.BOLT -> {
                path.reset()
                path.moveTo(13f, 2.5f); path.lineTo(5f, 13.5f); path.lineTo(11f, 13.5f)
                path.lineTo(10f, 21.5f); path.lineTo(19f, 10f); path.lineTo(13f, 10f); path.close()
                canvas.drawPath(path, fill)
                stroke.strokeWidth = 1.4f
                canvas.drawPath(path, stroke)
            }
            Ic.BOOKMARK -> poly(canvas, 7f, 4f, 17f, 4f, 17f, 20f, 12f, 16f, 7f, 20f, close = true)
            Ic.BOOK -> {
                path.reset()
                path.moveTo(12f, 6f)
                path.cubicTo(9f, 4f, 6f, 4f, 3.5f, 5f)
                path.lineTo(3.5f, 19f)
                path.cubicTo(6f, 18f, 9f, 18f, 12f, 20f)
                path.cubicTo(15f, 18f, 18f, 18f, 20.5f, 19f)
                path.lineTo(20.5f, 5f)
                path.cubicTo(18f, 4f, 15f, 4f, 12f, 6f)
                path.close()
                canvas.drawPath(path, stroke)
                line(canvas, 12f, 6f, 12f, 20f)
            }
            Ic.CHAT -> {
                rect.set(4f, 5f, 20f, 16f); canvas.drawRoundRect(rect, 4f, 4f, stroke)
                poly(canvas, 9f, 16f, 9f, 20f, 13.5f, 16f)
            }
            Ic.SPLIT -> {
                rect.set(4f, 5f, 20f, 19f); canvas.drawRoundRect(rect, 4f, 4f, stroke)
                line(canvas, 4f, 12f, 20f, 12f)
            }
            Ic.SPARK -> {
                path.reset()
                path.moveTo(12f, 3f); path.lineTo(14f, 10f); path.lineTo(21f, 12f); path.lineTo(14f, 14f)
                path.lineTo(12f, 21f); path.lineTo(10f, 14f); path.lineTo(3f, 12f); path.lineTo(10f, 10f); path.close()
                canvas.drawPath(path, fill)
                stroke.strokeWidth = 1.2f
                canvas.drawPath(path, stroke)
            }
            Ic.SAVE -> {
                line(canvas, 12f, 4f, 12f, 15f)
                poly(canvas, 8f, 11f, 12f, 15f, 16f, 11f)
                poly(canvas, 5f, 16f, 5f, 20f, 19f, 20f, 19f, 16f)
            }
            Ic.STOP -> {
                rect.set(7f, 7f, 17f, 17f)
                canvas.drawRoundRect(rect, 2.5f, 2.5f, fill)
            }
            Ic.GLOBE -> {
                canvas.drawCircle(12f, 12f, 8f, stroke)
                line(canvas, 4f, 12f, 20f, 12f)
                rect.set(8f, 4f, 16f, 20f); canvas.drawOval(rect, stroke)
            }
            Ic.CHECK -> poly(canvas, 5f, 12.5f, 10f, 17.5f, 19f, 7f)
            Ic.LOCK -> {
                rect.set(6f, 11f, 18f, 20f); canvas.drawRoundRect(rect, 2.5f, 2.5f, stroke)
                rect.set(8.5f, 4f, 15.5f, 14f); canvas.drawArc(rect, 180f, 180f, false, stroke)
            }
            Ic.CLOCK -> {
                canvas.drawCircle(12f, 12f, 8f, stroke)
                poly(canvas, 12f, 7f, 12f, 12f, 15.5f, 14f)
            }
            Ic.SLIDERS -> {
                line(canvas, 4f, 7f, 20f, 7f); line(canvas, 4f, 12f, 20f, 12f); line(canvas, 4f, 17f, 20f, 17f)
                fill.color = UiTheme.sheet
                canvas.drawCircle(9f, 7f, 2.3f, fill); canvas.drawCircle(15f, 12f, 2.3f, fill); canvas.drawCircle(8f, 17f, 2.3f, fill)
                canvas.drawCircle(9f, 7f, 2.3f, stroke); canvas.drawCircle(15f, 12f, 2.3f, stroke); canvas.drawCircle(8f, 17f, 2.3f, stroke)
            }
            Ic.MOON -> {
                val outer = Path().apply { addCircle(12f, 12f, 8.2f, Path.Direction.CW) }
                val cut = Path().apply { addCircle(16.2f, 8.6f, 7f, Path.Direction.CW) }
                outer.op(cut, Path.Op.DIFFERENCE)
                canvas.drawPath(outer, fill)
            }
            Ic.SHIELD -> {
                path.reset()
                path.moveTo(12f, 3f); path.lineTo(19f, 6f); path.lineTo(19f, 12f)
                path.cubicTo(19f, 16.5f, 16f, 19.5f, 12f, 21f)
                path.cubicTo(8f, 19.5f, 5f, 16.5f, 5f, 12f)
                path.lineTo(5f, 6f); path.close()
                canvas.drawPath(path, stroke)
            }
            Ic.TEXT -> {
                fill.typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)
                fill.textAlign = Paint.Align.CENTER
                fill.textSize = 13f
                canvas.drawText("Aa", 12f, 16.4f, fill)
            }
        }
        canvas.restore()
    }

    override fun setAlpha(alpha: Int) { stroke.alpha = alpha; fill.alpha = alpha }
    override fun setColorFilter(cf: android.graphics.ColorFilter?) { stroke.colorFilter = cf; fill.colorFilter = cf }
    @Suppress("OVERRIDE_DEPRECATION")
    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT
}

// ============================================================================ glass surfaces

object Glass {
    /** Flat semantic surface with a hairline edge. */
    fun panel(c: Context, radiusDp: Number, strong: Boolean = false): GradientDrawable =
        GradientDrawable().apply {
            cornerRadius = c.dpf(radiusDp)
            setColor(if (strong) UiTheme.surfaceAlt else UiTheme.sheet)
            setStroke(c.dpi(1), UiTheme.stroke)
        }

    fun field(c: Context, radiusDp: Number, focused: Boolean): GradientDrawable =
        GradientDrawable().apply {
            cornerRadius = c.dpf(radiusDp)
            setColor(UiTheme.sheet)
            setStroke(c.dpi(if (focused) 2 else 1), if (focused) UiTheme.accent else UiTheme.strokeSoft)
        }

    fun accent(c: Context, radiusDp: Number, orientation: GradientDrawable.Orientation = GradientDrawable.Orientation.LEFT_RIGHT): GradientDrawable =
        GradientDrawable().apply {
            cornerRadius = c.dpf(radiusDp)
            setColor(UiTheme.accent)
        }

    fun solid(c: Context, color: Int, radiusDp: Number, strokeColor: Int = 0, strokeDp: Number = 1): GradientDrawable =
        GradientDrawable().apply {
            cornerRadius = c.dpf(radiusDp)
            setColor(color)
            if (strokeColor != 0) setStroke(c.dpi(strokeDp), strokeColor)
        }

    fun oval(color: Int, strokeColor: Int = 0, strokePx: Int = 0): GradientDrawable =
        GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(color)
            if (strokeColor != 0) setStroke(strokePx, strokeColor)
        }

    /** Touch ripple over [content] (optional), round or rounded-rectangle. */
    fun ripple(c: Context, content: Drawable?, radiusDp: Number? = null): RippleDrawable {
        val mask = GradientDrawable().apply {
            setColor(Color.WHITE)
            if (radiusDp == null) shape = GradientDrawable.OVAL else cornerRadius = c.dpf(radiusDp)
        }
        return RippleDrawable(ColorStateList.valueOf(0x40FFFFFF), content, mask)
    }
}

/** Square icon button with a round ripple. */
internal fun Context.iconButton(ic: Ic, desc: String, sizeDp: Int = 22, boxDp: Int = 44, color: Int = UiTheme.iconIdle, onClick: () -> Unit): ImageView =
    ImageView(this).apply {
        setImageDrawable(IconDrawable(ic, color))
        val pad = dpi((boxDp - sizeDp) / 2f)
        setPadding(pad, pad, pad, pad)
        scaleType = ImageView.ScaleType.FIT_CENTER
        background = Glass.ripple(this@iconButton, null)
        contentDescription = desc
        isClickable = true
        isFocusable = true
        setOnClickListener { onClick() }
    }

// ============================================================================ flat progress bar

class GlowBar(ctx: Context) : View(ctx) {
    private val track = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = UiTheme.strokeSoft }
    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = UiTheme.accent }
    private var shown = 0f
    private var target = 0
    private var move: ValueAnimator? = null

    /** 0..100 determinate progress. */
    var progress: Int
        get() = target
        set(value) {
            val next = value.coerceIn(0, 100)
            if (next == target) return
            target = next
            move?.cancel()
            if (!ToonTheme.Motion.enabled(context)) {
                shown = next.toFloat()
                invalidate()
            } else {
                move = ValueAnimator.ofFloat(shown, next.toFloat()).apply {
                    duration = ToonTheme.Motion.standardMs
                    interpolator = DecelerateInterpolator()
                    addUpdateListener {
                        shown = it.animatedValue as Float
                        invalidate()
                    }
                    start()
                }
            }
        }

    override fun onDetachedFromWindow() {
        move?.cancel()
        super.onDetachedFromWindow()
    }

    override fun onDraw(canvas: Canvas) {
        val w = width.toFloat()
        val h = height.toFloat()
        val radius = h / 2f
        canvas.drawRoundRect(0f, 0f, w, h, radius, radius, track)
        val filled = w * shown / 100f
        if (filled <= 0f) return
        fillPaint.color = UiTheme.accent
        canvas.drawRoundRect(0f, 0f, filled, h, radius, radius, fillPaint)
    }
}

// ============================================================================ extract action

/** Main dock action. Activity state is shown with a steady status dot and real determinate progress. */
class ExtractButton(ctx: Context) : View(ctx) {
    /** -1 = idle, 0 = busy/indeterminate, 1..100 = determinate progress. */
    var ringProgress: Int = -1
        set(value) { field = value.coerceIn(-1, 100); invalidate() }

    var autoOn: Boolean = true
        set(value) { field = value; invalidate() }

    private val dens = ctx.resources.displayMetrics.density
    private val disc = Paint(Paint.ANTI_ALIAS_FLAG)
    private val ringTrack = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2f * dens
        color = UiTheme.strokeSoft
    }
    private val ringProgressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2.4f * dens
        strokeCap = Paint.Cap.ROUND
        color = UiTheme.accent
    }
    private val badge = Paint(Paint.ANTI_ALIAS_FLAG)
    private val glyph = IconDrawable(Ic.TEXT, UiTheme.onAccent)
    private val ring = RectF()
    private var mode = 0
    private var activityColor: Int? = null
    private var activityLabel = ""

    var modeIndex: Int
        get() = mode
        set(value) {
            mode = value.coerceIn(0, 2)
            invalidate()
        }

    fun setActivityIndicator(color: Int, blinking: Boolean, label: String) {
        // The old pulsing light is intentionally static; status stays visible without flashing.
        activityColor = color
        activityLabel = label
        contentDescription = if (label.isBlank()) "Extract chapter" else "Extract chapter. $label"
        invalidate()
    }

    init {
        isClickable = true
        isFocusable = true
        contentDescription = "Extract chapter"
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val size = (68f * dens).toInt()
        setMeasuredDimension(resolveSize(size, widthMeasureSpec), resolveSize(size, heightMeasureSpec))
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
    }

    override fun onDraw(canvas: Canvas) {
        val cx = width / 2f
        val cy = height / 2f
        val radius = minOf(width, height) / 2f
        val outer = radius - 2f * dens
        ring.set(cx - outer + 3f * dens, cy - outer + 3f * dens, cx + outer - 3f * dens, cy + outer - 3f * dens)

        canvas.drawCircle(cx, cy, outer, ringTrack)
        if (ringProgress > 0) {
            canvas.drawArc(ring, -90f, 360f * ringProgress / 100f, false, ringProgressPaint)
        }

        val discRadius = outer - 6f * dens
        disc.color = ModeUi.start(mode)
        canvas.drawCircle(cx, cy, discRadius, disc)

        val glyphSize = (discRadius * 1.05f).toInt()
        glyph.setBounds(
            (cx - glyphSize / 2f).toInt(),
            (cy - glyphSize / 2f).toInt(),
            (cx + glyphSize / 2f).toInt(),
            (cy + glyphSize / 2f).toInt()
        )
        glyph.draw(canvas)

        val badgeRadius = 5f * dens
        val bx = cx + outer * 0.68f
        val by = cy - outer * 0.68f
        badge.color = UiTheme.bg
        canvas.drawCircle(bx, by, badgeRadius + 1.5f * dens, badge)
        badge.color = activityColor ?: if (autoOn) UiTheme.ok else UiTheme.faint
        canvas.drawCircle(bx, by, badgeRadius, badge)
    }
}

// ============================================================================ dialogs / sheets

object Sheets {
    /** A borderless dialog hosting [content]; bottom-anchored, translucent, blurs the app behind it on Android 12+. */
    fun create(activity: Activity, content: View, fullScreen: Boolean = false, bottom: Boolean = true, dim: Float = 0.5f): Dialog {
        val d = Dialog(activity)
        d.requestWindowFeature(Window.FEATURE_NO_TITLE)
        d.setCanceledOnTouchOutside(true)
        d.setContentView(content)
        val w = d.window
        if (w != null) {
            w.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            w.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, if (fullScreen) ViewGroup.LayoutParams.MATCH_PARENT else ViewGroup.LayoutParams.WRAP_CONTENT)
            w.setGravity(if (bottom) Gravity.BOTTOM else Gravity.CENTER)
            w.setDimAmount(dim)
            w.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
            if (Build.VERSION.SDK_INT >= 31) {
                try {
                    if (activity.windowManager.isCrossWindowBlurEnabled) {
                        w.addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
                        val lp = w.attributes
                        lp.blurBehindRadius = 28
                        w.attributes = lp
                    }
                } catch (_: Throwable) {}
            }
        }
        d.setOnShowListener { slideUp(content) }
        return d
    }

    fun slideUp(v: View) {
        v.alpha = 0f
        v.post {
            v.translationY = v.height * 0.3f
            v.animate().translationY(0f).alpha(1f).setDuration(ToonTheme.Motion.standardMs).setInterpolator(DecelerateInterpolator()).start()
        }
    }

    /** Flat Forest Luxe / Paper surface with rounded top corners. */
    fun sheetBackground(c: Context, topRadiusDp: Number = 30): GradientDrawable {
        val r = c.dpf(topRadiusDp)
        return GradientDrawable().apply {
            setColor(UiTheme.sheet)
            cornerRadii = floatArrayOf(r, r, r, r, 0f, 0f, 0f, 0f)
            setStroke(c.dpi(1), UiTheme.stroke)
        }
    }
}

/** ScrollView that never grows past [maxHeightPx] (sheets keep a gap above them). */
class MaxHeightScroll(ctx: Context, private val maxHeightPx: Int) : ScrollView(ctx) {
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, View.MeasureSpec.makeMeasureSpec(maxHeightPx, View.MeasureSpec.AT_MOST))
    }
}
