package com.ashraf.novelstudio

import android.animation.ValueAnimator
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.RectF
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
import android.widget.ImageView
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import kotlin.math.cos
import kotlin.math.sin

/**
 * Toon Sensei design system ("Paper & Seal" family, charcoal flavour).
 *
 * The app renders chrome in the calm paper tones of the white edition; at runtime we use the
 * charcoal adaptation chosen for night reading. Surfaces are flat: no gradients, no glow, 1 dp
 * hairline borders, generous radii, one accent ("Seal" red), green "Sage" for success and a warm
 * "Amber" for attention. All shared visuals live here: colours, canvas icons, surfaces, the
 * extract pill, sheet helpers, the mascot and basic text/button builders.
 */
object UiTheme {
    // --- charcoal palette (see ui spec ROADMAP section 3 mapping) ---
    val bg = 0xFF1C1B19.toInt()          // Charcoal background
    val surface1 = 0xFF26251F.toInt()    // cards, sheets
    val surface2 = 0xFF2E2C26.toInt()    // inputs, fields, pressed chips
    val sheet = surface1                 // legacy alias kept for callers
    val text = 0xFFF6F1E7.toInt()        // Paper ink-on-dark
    val dim = 0xFFD8D2C4.toInt()         // inkSoft
    val faint = 0xFFA9A294.toInt()       // inkFaint
    val iconIdle = dim
    // accents
    val seal = 0xFFC4694F.toInt()        // Seal red (charcoal)
    val accent = seal
    val accentA = seal                   // legacy: gradients collapsed to the flat accent
    val accentB = seal
    val sealSoft = 0xFF5A2F25.toInt()    // tinted background of the stop/danger states
    val sealInk = 0xFFEAC2B4.toInt()     // warm light text/icons on sealSoft
    val sage = 0xFF86A489.toInt()
    val sageSoft = 0xFF2C3A2E.toInt()
    val sageInk = 0xFFB7D0BA.toInt()
    val amber = 0xFFC8964E.toInt()
    val amberSoft = 0xFF4A3A24.toInt()
    val amberInk = 0xFFE9CD96.toInt()
    val ok = sage
    val warn = amber
    val danger = seal                    // maroon maps to the accent in charcoal
    // hairlines
    val stroke = 0xFF3A3830.toInt()      // line
    val strokeSoft = 0xFF2E2C26.toInt()  // lineSoft
    val ripple = 0x26FFFFFF              // ripple over dark surfaces
}

internal fun Context.dpi(v: Number): Int = (v.toFloat() * resources.displayMetrics.density + 0.5f).toInt()
internal fun Context.dpf(v: Number): Float = v.toFloat() * resources.displayMetrics.density

// ============================================================================ icons

enum class Ic {
    BACK, FORWARD, RELOAD, HOME, GRID, DOTS, PLUS, CLOSE, SHARE, SEARCH, BOLT, BOOKMARK, BOOK,
    CHAT, SPLIT, SPARK, SAVE, STOP, GLOBE, CHECK, LOCK, CLOCK, SLIDERS, MOON, SHIELD, TEXT, CHEVRON,
    CHEVDOWN, SCAN, CROSSHAIR, CURSOR, LINK, LIBRARY, MENU, INFO, TASKS
}

/** 24x24 stroke icons (lucide geometry, 1.7 units), drawn at any size. */
class IconDrawable(private val ic: Ic, private var color: Int, private val strokeUnits: Float = 1.7f) : Drawable() {
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
            Ic.CHEVDOWN -> poly(canvas, 7f, 10f, 12f, 15f, 17f, 10f)
            Ic.RELOAD -> {
                rect.set(5f, 5f, 19f, 19f)
                canvas.drawArc(rect, -40f, 290f, false, stroke)
                // clockwise tangent at the arc end (250 degrees)
                val th = Math.toRadians(250.0)
                val tx = 12f + 7f * Math.cos(th).toFloat()
                val ty = 12f + 7f * Math.sin(th).toFloat()
                arrowHead(canvas, tx, ty, -Math.sin(th).toFloat(), Math.cos(th).toFloat(), 5f)
            }
            Ic.HOME -> { poly(canvas, 4f, 11f, 12f, 4f, 20f, 11f); poly(canvas, 6f, 9.5f, 6f, 20f, 18f, 20f, 18f, 9.5f) }
            Ic.GRID -> { rect.set(4f, 4f, 20f, 20f); canvas.drawRoundRect(rect, 3f, 3f, stroke); rect.set(4f, 4f, 12f, 12f); canvas.drawRoundRect(rect, 2f, 2f, stroke); rect.set(12f, 12f, 20f, 20f); canvas.drawRoundRect(rect, 2f, 2f, stroke) }
            Ic.DOTS -> { for (x in floatArrayOf(5f, 12f, 19f)) canvas.drawCircle(x, 12f, 1.3f, fill) }
            Ic.PLUS -> { line(canvas, 12f, 5f, 12f, 19f); line(canvas, 5f, 12f, 19f, 12f) }
            Ic.CLOSE -> { line(canvas, 6f, 6f, 18f, 18f); line(canvas, 18f, 6f, 6f, 18f) }
            Ic.SHARE -> {
                canvas.drawCircle(6f, 12f, 2.4f, stroke); canvas.drawCircle(17f, 5.5f, 2.4f, stroke); canvas.drawCircle(17f, 18.5f, 2.4f, stroke)
                line(canvas, 8.2f, 10.9f, 14.8f, 6.6f); line(canvas, 8.2f, 13.1f, 14.8f, 17.4f)
            }
            Ic.SEARCH -> { canvas.drawCircle(11f, 11f, 6f, stroke); line(canvas, 15.5f, 15.5f, 20f, 20f) }
            Ic.BOLT -> {
                path.reset()
                path.moveTo(13f, 2.5f); path.lineTo(5f, 13.5f); path.lineTo(11f, 13.5f); path.lineTo(10f, 21.5f); path.lineTo(19f, 10f); path.lineTo(12.5f, 10f); path.close()
                canvas.drawPath(path, fill)
            }
            Ic.BOOKMARK -> { path.reset(); path.moveTo(7f, 4f); path.lineTo(17f, 4f); path.lineTo(17f, 20f); path.lineTo(12f, 16.5f); path.lineTo(7f, 20f); path.close(); canvas.drawPath(path, stroke) }
            Ic.BOOK -> { path.reset(); path.moveTo(4f, 5f); path.cubicTo(7f, 3.5f, 10f, 3.5f, 12f, 5f); path.cubicTo(14f, 3.5f, 17f, 3.5f, 20f, 5f); path.lineTo(20f, 19f); path.cubicTo(17f, 17.5f, 14f, 17.5f, 12f, 19f); path.cubicTo(10f, 17.5f, 7f, 17.5f, 4f, 19f); path.close(); canvas.drawPath(path, stroke); line(canvas, 12f, 5f, 12f, 19f) }
            Ic.CHAT -> { path.reset(); path.moveTo(12f, 4f); rect.set(4f, 4f, 20f, 16f); canvas.drawRoundRect(rect, 5f, 5f, stroke); poly(canvas, 8f, 15f, 8f, 20f, 12.5f, 16f) }
            Ic.SPLIT -> { rect.set(4f, 5f, 11f, 19f); canvas.drawRoundRect(rect, 2f, 2f, stroke); rect.set(13f, 5f, 20f, 19f); canvas.drawRoundRect(rect, 2f, 2f, stroke) }
            Ic.SPARK -> {
                path.reset()
                path.moveTo(12f, 3f); path.lineTo(14f, 10f); path.lineTo(21f, 12f); path.lineTo(14f, 14f)
                path.lineTo(12f, 21f); path.lineTo(10f, 14f); path.lineTo(3f, 12f); path.lineTo(10f, 10f); path.close()
                canvas.drawPath(path, fill)
                stroke.strokeWidth = 1.2f
                canvas.drawPath(path, stroke)
            }
            Ic.SAVE -> { line(canvas, 12f, 4f, 12f, 15f); poly(canvas, 8f, 11f, 12f, 15f, 16f, 11f); poly(canvas, 5f, 16f, 5f, 20f, 19f, 20f, 19f, 16f) }
            Ic.STOP -> { rect.set(7f, 7f, 17f, 17f); canvas.drawRoundRect(rect, 2f, 2f, fill) }
            Ic.GLOBE -> { canvas.drawCircle(12f, 12f, 8f, stroke); line(canvas, 4f, 12f, 20f, 12f); rect.set(8f, 4f, 16f, 20f); canvas.drawOval(rect, stroke) }
            Ic.CHECK -> poly(canvas, 5f, 12.5f, 10f, 17.5f, 19f, 7f)
            Ic.LOCK -> { rect.set(6f, 11f, 18f, 20f); canvas.drawRoundRect(rect, 2.5f, 2.5f, stroke); rect.set(8.5f, 4f, 15.5f, 14f); canvas.drawArc(rect, 180f, 180f, false, stroke) }
            Ic.CLOCK -> { canvas.drawCircle(12f, 12f, 8f, stroke); poly(canvas, 12f, 7f, 12f, 12f, 15.5f, 14f) }
            Ic.SLIDERS -> {
                line(canvas, 4f, 7f, 20f, 7f); line(canvas, 4f, 12f, 20f, 12f); line(canvas, 4f, 17f, 20f, 17f)
                fill.color = UiTheme.bg
                canvas.drawCircle(9f, 7f, 2.3f, fill); canvas.drawCircle(15f, 12f, 2.3f, fill); canvas.drawCircle(8f, 17f, 2.3f, fill)
                canvas.drawCircle(9f, 7f, 2.3f, stroke); canvas.drawCircle(15f, 12f, 2.3f, stroke); canvas.drawCircle(8f, 17f, 2.3f, stroke)
            }
            Ic.MOON -> { path.reset(); path.moveTo(20f, 14f); rect.set(4f, 3f, 21f, 20f); canvas.drawArc(rect, 62f, 236f, false, stroke); line(canvas, 20f, 14f, 20f, 14f); path.reset(); path.moveTo(20f, 14f); path.arcTo(rect, -62f, -56f); canvas.drawPath(path, stroke) }
            Ic.SHIELD -> { path.reset(); path.moveTo(12f, 3f); path.lineTo(19f, 6f); path.lineTo(19f, 12f); path.cubicTo(19f, 16.5f, 16f, 19.5f, 12f, 21f); path.cubicTo(8f, 19.5f, 5f, 16.5f, 5f, 12f); path.lineTo(5f, 6f); path.close(); canvas.drawPath(path, stroke) }
            Ic.TEXT -> { line(canvas, 5f, 6f, 19f, 6f); line(canvas, 12f, 6f, 12f, 20f); line(canvas, 9f, 20f, 15f, 20f) }
            Ic.SCAN -> {
                poly(canvas, 4f, 9f, 4f, 4f, 9f, 4f)
                poly(canvas, 15f, 4f, 20f, 4f, 20f, 9f)
                poly(canvas, 4f, 15f, 4f, 20f, 9f, 20f)
                poly(canvas, 15f, 20f, 20f, 20f, 20f, 15f)
                line(canvas, 3f, 12f, 21f, 12f)
            }
            Ic.CROSSHAIR -> {
                canvas.drawCircle(12f, 12f, 3f, stroke)
                line(canvas, 12f, 3f, 12f, 7f); line(canvas, 12f, 17f, 12f, 21f)
                line(canvas, 3f, 12f, 7f, 12f); line(canvas, 17f, 12f, 21f, 12f)
            }
            Ic.CURSOR -> poly(canvas, 6f, 4f, 18f, 10f, 13f, 12f, 11f, 17f, close = true)
            Ic.LINK -> {
                rect.set(2.5f, 7f, 11f, 17f); canvas.drawArc(rect, 90f, 180f, false, stroke)
                rect.set(13f, 7f, 21.5f, 17f); canvas.drawArc(rect, -90f, 180f, false, stroke)
                line(canvas, 8.5f, 12f, 15.5f, 12f)
            }
            Ic.LIBRARY -> {
                rect.set(4f, 5f, 8f, 19f); canvas.drawRect(rect, stroke)
                rect.set(10f, 5f, 14f, 19f); canvas.drawRect(rect, stroke)
                path.reset(); path.moveTo(16f, 6f); path.lineTo(20f, 7f); path.lineTo(17f, 19f); path.lineTo(13f, 18f); path.close(); canvas.drawPath(path, stroke)
            }
            Ic.MENU -> { line(canvas, 4f, 7f, 20f, 7f); line(canvas, 4f, 12f, 20f, 12f); line(canvas, 4f, 17f, 16f, 17f) }
            Ic.INFO -> { canvas.drawCircle(12f, 12f, 9f, stroke); line(canvas, 12f, 11f, 12f, 16f); canvas.drawCircle(12f, 8f, 0.7f, fill) }
            Ic.TASKS -> {
                poly(canvas, 4f, 7f, 5.5f, 8.5f, 8f, 5.5f); line(canvas, 11f, 7f, 20f, 7f)
                poly(canvas, 4f, 13f, 5.5f, 14.5f, 8f, 11.5f); line(canvas, 11f, 13f, 20f, 13f)
                line(canvas, 4f, 19f, 20f, 19f)
            }
        }
        canvas.restore()
    }

    override fun setAlpha(alpha: Int) { stroke.alpha = alpha; fill.alpha = alpha }
    override fun setColorFilter(colorFilter: android.graphics.ColorFilter?) { stroke.colorFilter = colorFilter; fill.colorFilter = colorFilter }
    @Deprecated("deprecated", ReplaceWith("opacity", "android.graphics.drawable.Drawable"))
    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT
}

// ============================================================================ surfaces ("Glass" kept as the legacy name: now flat)

object Glass {
    /** Flat card, sheet or panel: surface1 fill + 1 dp hairline, rounded corners. */
    fun panel(c: Context, radiusDp: Number = 26): GradientDrawable =
        GradientDrawable().apply {
            cornerRadius = c.dpf(radiusDp)
            setColor(UiTheme.surface1)
            setStroke(c.dpi(1), UiTheme.stroke)
        }

    /** Input field: surface2 fill; the focus stroke switches to the accent. */
    fun field(c: Context, radiusDp: Number = 20, focused: Boolean = false): GradientDrawable =
        GradientDrawable().apply {
            cornerRadius = c.dpf(radiusDp)
            setColor(UiTheme.surface2)
            setStroke(c.dpi(1), if (focused) UiTheme.seal else UiTheme.stroke)
        }

    /** Flat accent fill (kept name; was a gradient). */
    fun accent(c: Context, radiusDp: Number, orientation: GradientDrawable.Orientation = GradientDrawable.Orientation.LEFT_RIGHT): GradientDrawable =
        GradientDrawable().apply {
            cornerRadius = c.dpf(radiusDp)
            setColor(UiTheme.seal)
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
        return RippleDrawable(ColorStateList.valueOf(UiTheme.ripple), content, mask)
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

// ============================================================================ shared view builders

object Ui {
    const val H1 = 26f
    const val H2 = 17f
    const val BODY = 15f
    const val SMALL = 13f
    const val CAPS = 11f

    fun text(c: Context, s: CharSequence, sizeSp: Float = BODY, color: Int = UiTheme.text, bold: Boolean = false): TextView =
        TextView(c).apply {
            text = s
            textSize = sizeSp
            setTextColor(color)
            typeface = if (bold) Typeface.create(Typeface.DEFAULT, Typeface.BOLD) else Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }

    fun caption(c: Context, s: CharSequence, color: Int = UiTheme.faint): TextView = text(c, s, SMALL, color)

    /** Flat primary button: paper-ink fill, charcoal label (48dp, radius 26). */
    fun btnPrimary(c: Context, label: CharSequence): TextView = TextView(c).apply {
        text = label
        textSize = BODY
        setTextColor(UiTheme.bg)
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        gravity = Gravity.CENTER
        minHeight = c.dpi(48)
        setPadding(c.dpi(24), 0, c.dpi(24), 0)
        background = Glass.ripple(c, Glass.solid(c, UiTheme.text, 26), 26)
    }

    /** Flat accent/danger button: seal-soft fill, seal hairline, warm label (48dp, radius 26). */
    fun btnDanger(c: Context, label: CharSequence): TextView = TextView(c).apply {
        text = label
        textSize = BODY
        setTextColor(UiTheme.sealInk)
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        gravity = Gravity.CENTER
        minHeight = c.dpi(48)
        setPadding(c.dpi(24), 0, c.dpi(24), 0)
        background = Glass.ripple(c, Glass.solid(c, UiTheme.sealSoft, 26, UiTheme.seal, 1), 26)
    }

    /** Flat outline button: 1 dp hairline, ink label (48dp, radius 26). */
    fun btnGhost(c: Context, label: CharSequence, bold: Boolean = false): TextView = TextView(c).apply {
        text = label
        textSize = BODY
        setTextColor(UiTheme.text)
        if (bold) typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        gravity = Gravity.CENTER
        minHeight = c.dpi(48)
        setPadding(c.dpi(24), 0, c.dpi(24), 0)
        background = Glass.ripple(c, Glass.solid(c, Color.TRANSPARENT, 26, UiTheme.stroke, 1), 26)
    }

    /** Standard sheet card: surface1, hairline, 16dp radius. */
    fun card(c: Context, radiusDp: Number = 16): GradientDrawable = Glass.solid(c, UiTheme.surface1, radiusDp, UiTheme.stroke, 1)

    /** Soft tinted card for attention (amber) or success (sage) notes. */
    fun tintCard(c: Context, tint: Int, tintLine: Int, radiusDp: Number = 16): GradientDrawable =
        Glass.solid(c, tint, radiusDp, tintLine, 1)

    fun divider(c: Context, soft: Boolean = true): View = View(c).apply {
        setBackgroundColor(if (soft) UiTheme.strokeSoft else UiTheme.stroke)
    }

    /** Drag handle of a bottom sheet: 36x4, inkFaint. */
    fun sheetHandle(c: Context): View = View(c).apply {
        background = Glass.solid(c, UiTheme.stroke, 2)
    }

    /** Flat android switch replacement: thin pill + knob, accent when on. */
    fun switchView(c: Context, checked: Boolean, onChange: (Boolean) -> Unit): SwitchFlat = SwitchFlat(c).apply {
        isChecked = checked
        onToggled = onChange
    }
}

/** 46x28 flat toggle: pill track + white knob, Seal when on (spec board 5). */
class SwitchFlat(ctx: Context) : View(ctx) {
    var isChecked = false
        set(v) { if (field != v) { field = v; invalidate() } }
    var onToggled: ((Boolean) -> Unit)? = null

    private val p = Paint(Paint.ANTI_ALIAS_FLAG)

    init {
        setOnClickListener { isChecked = !isChecked; onToggled?.invoke(isChecked) }
        contentDescription = "Toggle"
    }

    override fun onMeasure(w: Int, h: Int) {
        val d = resources.displayMetrics.density
        setMeasuredDimension((46 * d).toInt(), (28 * d).toInt())
    }

    override fun onDraw(c: Canvas) {
        val w = width.toFloat(); val h = height.toFloat(); val d = resources.displayMetrics.density
        p.color = if (isChecked) UiTheme.seal else UiTheme.surface2
        c.drawRoundRect(0f, 0f, w, h, h / 2f, h / 2f, p)
        if (!isChecked) { p.color = UiTheme.stroke; p.style = Paint.Style.STROKE; p.strokeWidth = d; c.drawRoundRect(d / 2, d / 2, w - d / 2, h - d / 2, h / 2f, h / 2f, p); p.style = Paint.Style.FILL }
        val r = h / 2f - 3f * d
        val cx = if (isChecked) w - 3f * d - r else 3f * d + r
        p.color = UiTheme.text
        c.drawCircle(cx, h / 2f, r, p)
    }
}

// ============================================================================ simple flat bar (kept for callers)

class GlowBar(ctx: Context) : View(ctx) {
    private val track = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = UiTheme.strokeSoft }
    private val fillP = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = UiTheme.text }
    private var shown = 0f
    private var target = 0
    private var move: ValueAnimator? = null

    /** 0..100, animated. */
    var progress: Int
        get() = target
        set(v) {
            val t = v.coerceIn(0, 100)
            if (t == target) return
            target = t
            move?.cancel()
            move = ValueAnimator.ofFloat(shown, t.toFloat()).apply {
                duration = 220
                interpolator = DecelerateInterpolator()
                addUpdateListener { shown = it.animatedValue as Float; invalidate() }
                start()
            }
        }

    override fun onDetachedFromWindow() { move?.cancel(); super.onDetachedFromWindow() }

    override fun onDraw(c: Canvas) {
        val w = width.toFloat()
        val h = height.toFloat()
        val r = h / 2f
        c.drawRoundRect(0f, 0f, w, h, r, r, track)
        val fw = maxOf(w * shown / 100f, if (shown > 0f) h else 0f)
        if (fw <= 0f) return
        c.drawRoundRect(0f, 0f, fw, h, r, r, fillP)
    }
}

// ============================================================================ extract pill (flat)

/**
 * The centre action of the dock: a flat 44dp pill.
 * Idle: surface fill + 1 dp ink hairline, bolt + "Translate".
 * Running: seal-tinted fill + seal hairline, stop square + "Stop".
 * Public API kept from the old gradient button so the feeding code is untouched.
 */
class ExtractButton(ctx: Context) : View(ctx) {
    /** -1 = no ring, 0 = busy indeterminate, 1..100 = determinate. Progress now lives on the chat ring. */
    var ringProgress: Int = -1
        set(v) { if (field != v) { field = v; invalidate() } }
    var autoOn: Boolean = true
        set(v) { if (field != v) { field = v; invalidate() } }

    private val dens = ctx.resources.displayMetrics.density
    private val fillP = Paint(Paint.ANTI_ALIAS_FLAG)
    private val lineP = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; strokeWidth = 1f * dens }
    private val bolt = IconDrawable(Ic.BOLT, UiTheme.bg)
    private val stopIc = IconDrawable(Ic.STOP, UiTheme.sealInk)
    private val txt = Paint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 15f * dens; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD) }
    private val oval = RectF()

    /** 0 = Novel, 1 = Scan, 2 = Live. Kept for API compatibility; the pill is mode-neutral. */
    var modeIndex: Int = 0
        set(v) { if (field != v) { field = v; invalidate() } }

    // Old "activity light": a non-null colour means a lane is working -> the pill shows Stop.
    private var activityIndicatorColor: Int? = null
    private var activityIndicatorBlinking = false
    private var activityDesc = "Extract chapter"

    fun setActivityIndicator(color: Int, blinking: Boolean, desc: String = "") {
        val newBusy: Boolean
        if (color == Color.TRANSPARENT) { activityIndicatorColor = null; activityIndicatorBlinking = false; newBusy = false }
        else { activityIndicatorColor = color; activityIndicatorBlinking = blinking; newBusy = true }
        if (desc.isNotEmpty() && desc != activityDesc) { activityDesc = desc; contentDescription = desc }
        busy = newBusy
    }

    /** True while any lane works: the pill offers Stop. */
    var busy: Boolean = false
        set(v) { if (field != v) { field = v; updateColors() } }

    private fun updateColors() {
        if (busy) {
            fillP.color = UiTheme.sealSoft
            lineP.color = UiTheme.seal
            txt.color = UiTheme.sealInk
        } else {
            fillP.color = UiTheme.surface1
            lineP.color = UiTheme.dim
            txt.color = UiTheme.text
        }
        contentDescription = if (busy) "Stop" else "Extract chapter"
        invalidate()
    }

    init { updateColors() }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val w = MeasureSpec.getSize(widthMeasureSpec)
        val h = MeasureSpec.getSize(heightMeasureSpec)
        setMeasuredDimension(w, h)
    }

    override fun onDraw(c: Canvas) {
        val w = width.toFloat(); val h = height.toFloat()
        val r = h / 2f
        oval.set(lineP.strokeWidth / 2f, lineP.strokeWidth / 2f, w - lineP.strokeWidth / 2f, h - lineP.strokeWidth / 2f)
        c.drawRoundRect(oval, r, r, fillP)
        c.drawRoundRect(oval, r, r, lineP)
        // icon + label centred
        val label = if (busy) "Stop" else "Translate"
        val tw = txt.measureText(label)
        val iw = 20f * dens; val gap = 8f * dens
        val total = iw + gap + tw
        var x = (w - total) / 2f
        val icon = if (busy) stopIc else bolt
        val isz = iw.toInt()
        icon.setBounds(x.toInt(), ((h - isz) / 2f).toInt(), (x + iw).toInt(), ((h + isz) / 2f).toInt())
        icon.draw(c)
        x += iw + gap
        val ty = h / 2f - (txt.descent() + txt.ascent()) / 2f
        c.drawText(label, x, ty, txt)
    }
}

// ============================================================================ mascot (flat bookworm, spec boards 1/2/29)

/**
 * The original Toon Sensei bookworm: a worm with glasses reading an open book.
 * Drawn from the design-board SVG (viewBox 120) with charcoal-adapted ink; never animated.
 */
class MascotDrawable : Drawable() {
    private val fillP = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val strokeP = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
    }
    private val path = Path()

    override fun draw(c: Canvas) {
        val b = bounds
        if (b.isEmpty) return
        val size = minOf(b.width(), b.height()).toFloat()
        val s = size / 120f
        c.save()
        c.translate(b.left + (b.width() - size) / 2f, b.top + (b.height() - size) / 2f)
        c.scale(s, s)
        val ink = UiTheme.text
        // open book
        path.reset()
        path.moveTo(16f, 92f)
        path.cubicTo(31f, 85f, 45f, 85f, 60f, 92f)
        path.cubicTo(75f, 85f, 89f, 85f, 104f, 92f)
        path.lineTo(104f, 101f)
        path.cubicTo(89f, 94f, 75f, 94f, 60f, 101f)
        path.cubicTo(45f, 94f, 31f, 94f, 16f, 101f)
        path.close()
        fillP.color = UiTheme.surface2
        c.drawPath(path, fillP)
        strokeP.color = ink; strokeP.strokeWidth = 2.5f
        c.drawPath(path, strokeP)
        // worm body
        fillP.color = UiTheme.sage
        strokeP.color = ink; strokeP.strokeWidth = 2.5f
        for (seg in arrayOf(floatArrayOf(48f, 86f, 12f), floatArrayOf(61f, 79f, 13f), floatArrayOf(68f, 64f, 14f), floatArrayOf(63f, 43f, 20f))) {
            c.drawCircle(seg[0], seg[1], seg[2], fillP)
            c.drawCircle(seg[0], seg[1], seg[2], strokeP)
        }
        // eyes (glasses)
        fillP.color = ink
        strokeP.strokeWidth = 2f
        c.drawCircle(56f, 42f, 6.5f, fillP)
        c.drawCircle(56f, 42f, 6.5f, strokeP)
        c.drawCircle(71f, 42f, 6.5f, fillP)
        c.drawCircle(71f, 42f, 6.5f, strokeP)
        c.drawLine(62.5f, 42f, 64.5f, 42f, strokeP)
        // pupils
        fillP.color = UiTheme.bg
        c.drawCircle(57f, 43f, 2f, fillP)
        c.drawCircle(72f, 43f, 2f, fillP)
        // smile
        path.reset()
        path.moveTo(57f, 52f)
        path.quadTo(64f, 57f, 71f, 52f)
        c.drawPath(path, strokeP)
        c.restore()
    }

    override fun setAlpha(alpha: Int) { fillP.alpha = alpha; strokeP.alpha = alpha }
    override fun setColorFilter(colorFilter: android.graphics.ColorFilter?) { fillP.colorFilter = colorFilter; strokeP.colorFilter = colorFilter }
    @Deprecated("deprecated", ReplaceWith("opacity", "android.graphics.drawable.Drawable"))
    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT
}

/** Simple view hosting the mascot; sized by layout params. */
class MascotView(ctx: Context) : View(ctx) {
    private val d = MascotDrawable()
    init { contentDescription = "Toon Sensei" }
    override fun onDraw(c: Canvas) {
        d.setBounds(paddingLeft, paddingTop, width - paddingRight, height - paddingBottom)
        d.draw(c)
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
            val lp = w.attributes
            lp.width = WindowManager.LayoutParams.MATCH_PARENT
            lp.height = if (fullScreen) WindowManager.LayoutParams.MATCH_PARENT else WindowManager.LayoutParams.WRAP_CONTENT
            lp.gravity = if (bottom) Gravity.BOTTOM else Gravity.CENTER
            w.attributes = lp
            w.setDimAmount(dim)
            w.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
            if (Build.VERSION.SDK_INT >= 31) {
                try {
                    if (activity.windowManager.isCrossWindowBlurEnabled) {
                        w.addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
                        val lp2 = w.attributes
                        lp2.blurBehindRadius = 28
                        w.attributes = lp2
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
            v.animate().translationY(0f).alpha(1f).setDuration(260).setInterpolator(DecelerateInterpolator()).start()
        }
    }

    /** Flat surface1 top-rounded background for sheets (radius 24 like the spec sheets). */
    fun sheetBackground(c: Context, topRadiusDp: Number = 24): GradientDrawable {
        val r = c.dpf(topRadiusDp)
        return GradientDrawable().apply {
            setColor(UiTheme.surface1)
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

// ============================================================================ dialog helpers (spec board 28)

/**
 * Flat standard dialogs. New code should use these instead of AlertDialog; the 34 legacy
 * AlertDialogs migrate gradually, callbacks unchanged.
 */
object Dialogs {

    private fun modalFrame(a: Activity, content: View): Dialog {
        val frame = FrameLayout(a).apply {
            setPadding(a.dpi(40), 0, a.dpi(40), 0)
            addView(content, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        }
        val d = Dialog(a)
        d.requestWindowFeature(Window.FEATURE_NO_TITLE)
        d.setContentView(frame)
        d.window?.let { w ->
            w.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            val lp = w.attributes
            lp.width = WindowManager.LayoutParams.MATCH_PARENT
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT
            lp.gravity = Gravity.CENTER
            w.attributes = lp
            w.setDimAmount(0.5f)
        }
        d.setOnShowListener { Sheets.slideUp(frame) }
        return d
    }

    private fun buttonRow(c: Context, cancelLabel: String, okLabel: String, danger: Boolean, onCancel: () -> Unit, onOk: () -> Unit): View {
        val row = LinearLayout(c).apply { orientation = LinearLayout.HORIZONTAL }
        val cancel = Ui.btnGhost(c, cancelLabel)
        cancel.setOnClickListener { onCancel() }
        row.addView(cancel, LinearLayout.LayoutParams(0, c.dpi(48), 1f).also { it.marginEnd = c.dpi(10) })
        val ok = if (danger) {
            // destructive confirm: solid Seal, paper label
            TextView(c).apply {
                text = okLabel
                textSize = Ui.BODY
                setTextColor(Color.WHITE)
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                gravity = Gravity.CENTER
                setPadding(c.dpi(24), 0, c.dpi(24), 0)
                background = Glass.ripple(c, Glass.solid(c, UiTheme.seal, 26), 26)
            }
        } else Ui.btnPrimary(c, okLabel)
        ok.setOnClickListener { onOk() }
        row.addView(ok, LinearLayout.LayoutParams(0, c.dpi(48), 1f))
        return row
    }

    /** Centered confirm dialog: title 18, body 14, Cancel + OK (danger = solid Seal). */
    fun confirm(
        a: Activity, title: String, body: String?,
        okLabel: String = "OK", cancelLabel: String = "Cancel",
        danger: Boolean = false, onOk: () -> Unit
    ): Dialog {
        val col = LinearLayout(a).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(a.dpi(24), a.dpi(24), a.dpi(24), a.dpi(24))
            background = Glass.solid(a, UiTheme.surface1, 24, UiTheme.stroke, 1)
        }
        col.addView(Ui.text(a, title, 18f, UiTheme.text, bold = true))
        if (!body.isNullOrBlank()) col.addView(
            Ui.text(a, body, 14f, UiTheme.faint),
            LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).also { it.topMargin = a.dpi(8) }
        )
        lateinit var d: Dialog
        col.addView(
            buttonRow(a, cancelLabel, okLabel, danger, { d.dismiss() }, { d.dismiss(); onOk() }),
            LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).also { it.topMargin = a.dpi(22) }
        )
        d = modalFrame(a, col)
        d.show()
        return d
    }

    /** Centered input dialog: title 18, one field, Cancel + Save. */
    fun input(
        a: Activity, title: String, value: String = "",
        hint: String = "", okLabel: String = "Save",
        onOk: (String) -> Unit
    ): Dialog {
        val col = LinearLayout(a).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(a.dpi(24), a.dpi(24), a.dpi(24), a.dpi(24))
            background = Glass.solid(a, UiTheme.surface1, 24, UiTheme.stroke, 1)
        }
        col.addView(Ui.text(a, title, 18f, UiTheme.text, bold = true))
        val field = android.widget.EditText(a).apply {
            setText(value)
            this.hint = hint
            setTextColor(UiTheme.text)
            setHintTextColor(UiTheme.faint)
            textSize = 16f
            setPadding(a.dpi(16), 0, a.dpi(16), 0)
            background = Glass.field(a, 14, true)
        }
        col.addView(field, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, a.dpi(52)).also { it.topMargin = a.dpi(14) })
        lateinit var d: Dialog
        col.addView(
            buttonRow(a, "Cancel", okLabel, false, { d.dismiss() }, { d.dismiss(); onOk(field.text.toString()) }),
            LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).also { it.topMargin = a.dpi(22) }
        )
        d = modalFrame(a, col)
        d.show()
        return d
    }

    /** Bottom-sheet single choice: radio dots, tap to pick. */
    fun choice(
        a: Activity, title: String, options: List<String>,
        checked: Int = -1, onPick: (Int) -> Unit
    ): Dialog {
        val col = LinearLayout(a).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(a.dpi(24), a.dpi(12), a.dpi(24), a.dpi(24))
            background = Sheets.sheetBackground(a)
        }
        col.addView(Ui.sheetHandle(a), LinearLayout.LayoutParams(a.dpi(36), a.dpi(4)).also {
            it.gravity = Gravity.CENTER_HORIZONTAL; it.bottomMargin = a.dpi(10)
        })
        col.addView(Ui.text(a, title, 18f, UiTheme.text, bold = true), LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).also { it.bottomMargin = a.dpi(6) })
        val d = Sheets.create(a, col)
        options.forEachIndexed { i, label ->
            val radio = View(a).apply {
                background = Glass.oval(
                    if (i == checked) UiTheme.seal else UiTheme.surface1,
                    UiTheme.seal,
                    a.dpi(if (i == checked) 6 else 1)
                )
            }
            val row = LinearLayout(a).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                minimumHeight = a.dpi(52)
                background = Glass.ripple(a, null, 12)
                contentDescription = label
            }
            row.addView(radio, LinearLayout.LayoutParams(a.dpi(20), a.dpi(20)).also { it.marginEnd = a.dpi(14) })
            row.addView(Ui.text(a, label, Ui.BODY, UiTheme.text))
            row.setOnClickListener { d.dismiss(); onPick(i) }
            col.addView(row)
        }
        d.show()
        return d
    }
}
