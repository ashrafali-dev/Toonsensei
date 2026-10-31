package com.ashraf.novelstudio

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import android.view.View
import android.content.Context

/**
 * The brand mascot: a striped worm with round reading glasses resting on a book, drawn on a
 * 120×120 grid from the reference mockups (Main.html / Loading.html). Pure Canvas; one instance per
 * screen slot. Used by splash, Home header and empty/error states.
 */
class MascotPainter(private val c: ToonColors) {
    private val ink = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; strokeJoin = Paint.Join.ROUND; strokeCap = Paint.Cap.ROUND }
    private val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val body = if (c.dark) 0xFF7F9A84.toInt() else 0xFF9DB8A0.toInt()
    private val paper = if (c.dark) c.surface else 0xFFFBFAF6.toInt()
    private val line = c.title

    private val bookPath = Path().apply {
        moveTo(16f, 92f)
        cubicTo(31f, 85f, 45f, 85f, 60f, 92f)
        cubicTo(75f, 85f, 89f, 85f, 104f, 92f)
        lineTo(104f, 101f)
        cubicTo(89f, 94f, 75f, 94f, 60f, 101f)
        cubicTo(45f, 94f, 31f, 94f, 16f, 101f)
        close()
    }

    fun draw(canvas: Canvas, cx: Float, cy: Float, size: Float) {
        val s = size / 120f
        canvas.save()
        canvas.translate(cx - size / 2f, cy - size / 2f)
        canvas.scale(s, s)

        // book
        fill.color = paper
        canvas.drawPath(bookPath, fill)
        ink.color = line; ink.strokeWidth = 2.5f
        canvas.drawPath(bookPath, ink)

        // worm tail → head: overlapping segments
        val segs = floatArrayOf(48f, 86f, 12f, 61f, 79f, 13f, 68f, 64f, 14f, 63f, 43f, 20f)
        for (i in 0 until segs.size step 3) {
            fill.color = body
            canvas.drawCircle(segs[i], segs[i + 1], segs[i + 2], fill)
            ink.strokeWidth = 2.5f
            canvas.drawCircle(segs[i], segs[i + 1], segs[i + 2], ink)
        }

        // glasses
        fill.color = paper
        ink.strokeWidth = 2f
        canvas.drawCircle(56f, 42f, 6.5f, fill); canvas.drawCircle(56f, 42f, 6.5f, ink)
        canvas.drawCircle(71f, 42f, 6.5f, fill); canvas.drawCircle(71f, 42f, 6.5f, ink)
        canvas.drawLine(62.5f, 42f, 64.5f, 42f, ink)

        // eyes
        fill.color = line
        canvas.drawCircle(57f, 43f, 2f, fill)
        canvas.drawCircle(72f, 43f, 2f, fill)

        // smile
        val smile = Path().apply {
            moveTo(57f, 52f)
            quadTo(64f, 57f, 71f, 52f)
        }
        canvas.drawPath(smile, ink)

        canvas.restore()
    }
}

/** A [View] that renders the mascot, so states can drop it in like an ImageView. */
class MascotView(ctx: Context, private val sizeDp: Int) : View(ctx) {
    private val painter = MascotPainter(ToonTheme.colors(ctx))
    init {
        importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_NO
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val px = ToonDp.px(ctx, sizeDp)
        setMeasuredDimension(resolveSize(px, widthMeasureSpec), resolveSize(px, heightMeasureSpec))
    }

    override fun onDraw(canvas: Canvas) {
        painter.draw(canvas, width / 2f, height / 2f, minOf(width, height).toFloat())
    }
}

// ============================================================================ small helpers

/** dp/sp conversions for the design system. */
object ToonDp {
    fun px(ctx: Context, v: Number): Int = (v.toFloat() * ctx.resources.displayMetrics.density + 0.5f).toInt()
    fun sp(ctx: Context, v: Number): Float = v.toFloat() * ctx.resources.displayMetrics.scaledDensity
}

/** Standard text factories shared by all components (see DESIGN.md §3). */
object ToonText {
    private val sansMedium = Typeface.create("sans-serif-medium", Typeface.NORMAL)
    private val sansBold = Typeface.create("sans-serif-medium", Typeface.BOLD)
    private val sans = Typeface.create("sans-serif", Typeface.NORMAL)
    private val serif = Typeface.create("serif", Typeface.NORMAL)

    fun title(ctx: Context, c: ToonColors, text: String = ""): android.widget.TextView =
        base(ctx, c, ToonTheme.Type.title, sansMedium, c.title).apply { this.text = text }

    fun display(ctx: Context, c: ToonColors, text: String = ""): android.widget.TextView =
        base(ctx, c, ToonTheme.Type.display, serif, c.title).apply {
            this.text = text
            letterSpacing = 0.02f
        }

    fun section(ctx: Context, c: ToonColors, text: String = ""): android.widget.TextView =
        base(ctx, c, ToonTheme.Type.section, sansMedium, c.muted).apply {
            this.text = text.uppercase()
            letterSpacing = ToonTheme.SECTION_TRACK
        }

    fun body(ctx: Context, c: ToonColors, text: String = ""): android.widget.TextView =
        base(ctx, c, ToonTheme.Type.body, sans, c.text).apply { this.text = text }

    fun bodyStrong(ctx: Context, c: ToonColors, text: String = ""): android.widget.TextView =
        base(ctx, c, ToonTheme.Type.body, sansMedium, c.title).apply { this.text = text }

    fun label(ctx: Context, c: ToonColors, text: String = ""): android.widget.TextView =
        base(ctx, c, ToonTheme.Type.label, sansMedium, c.text).apply { this.text = text }

    fun caption(ctx: Context, c: ToonColors, text: String = ""): android.widget.TextView =
        base(ctx, c, ToonTheme.Type.caption, sans, c.muted).apply { this.text = text }

    fun micro(ctx: Context, c: ToonColors, text: String = ""): android.widget.TextView =
        base(ctx, c, ToonTheme.Type.micro, sansMedium, c.muted).apply { this.text = text }

    fun reader(ctx: Context, c: ToonColors, text: String = ""): android.widget.TextView =
        base(ctx, c, ToonTheme.Type.reader, serif, c.body).apply {
            this.text = text
            setLineSpacing(0f, 1.9f)
        }

    private fun base(ctx: Context, c: ToonColors, sizeSp: Float, tf: Typeface, color: Int) =
        android.widget.TextView(ctx).apply {
            textSize = sizeSp
            typeface = tf
            setTextColor(color)
            includeFontPadding = false
        }
}

/** Rounded-rect mask used for ripples and shapes. */
internal fun Context.toonRect(radiusDp: Number, color: Int): android.graphics.drawable.GradientDrawable =
    android.graphics.drawable.GradientDrawable().apply {
        cornerRadius = ToonDp.px(this@toonRect, radiusDp).toFloat()
        setColor(color)
    }

internal fun RectF.setAt(cx: Float, cy: Float, r: Float): RectF {
    set(cx - r, cy - r, cx + r, cy + r)
    return this
}
