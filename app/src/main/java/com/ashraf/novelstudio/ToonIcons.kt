package com.ashraf.novelstudio

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.view.View
import android.content.Context

/** Icons added by the redesign: same 24-unit grid, 1.7 stroke, round caps as [Ic]. */
enum class ToonIc {
    BACK, FORWARD, CHEVRON, RELOAD, CLOSE, PLUS, DOTS, SEARCH, GLOBE, CHECK,
    BOOK, LIBRARY, TABS, MENU, BOOKMARK, CLOCK, HISTORY, SPARK, BOLT, SCAN, BROADCAST, CROSSHAIR,
    SPLIT, CHAT, STOP, PAUSE, PLAY, SAVE, SHARE, SLIDERS, MOON, SUN, SHIELD, TEXT, LOCK, DOWNLOAD,
    EXTERNAL, IMAGE, CROP, TRANSLATE, WARNING, INFO, CIRCLE_CHECK, CIRCLE_CROSS, CALENDAR, TRASH, EDIT
}

/**
 * Draws a [ToonIc] into any bounds. Used by every redesigned screen; colour is passed per draw so a
 * single drawable can be re-tinted (selected/unselected) without rebuilding.
 */
class ToonIconDrawable(private var ic: ToonIc, private var color: Int, private val width: Float = 1.7f) : android.graphics.drawable.Drawable() {
    private val stroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE; strokeCap = Paint.Cap.ROUND; strokeJoin = Paint.Join.ROUND
    }
    private val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val path = Path()
    private val rect = RectF()

    fun tint(c: Int) { color = c }

    fun swap(i: ToonIc) { ic = i; invalidateSelf() }

    private fun poly(c: Canvas, close: Boolean = false, vararg p: Float) {
        path.reset(); path.moveTo(p[0], p[1])
        var i = 2
        while (i + 1 < p.size) { path.lineTo(p[i], p[i + 1]); i += 2 }
        if (close) path.close()
        c.drawPath(path, stroke)
    }

    override fun draw(c: Canvas) {
        val b = bounds
        if (b.isEmpty) return
        val size = minOf(b.width(), b.height()).toFloat()
        val s = size / 24f
        stroke.color = color
        stroke.strokeWidth = width
        fill.color = color
        c.save()
        c.translate(b.left + (b.width() - size) / 2f, b.top + (b.height() - size) / 2f)
        c.scale(s, s)
        when (ic) {
            ToonIc.BACK -> poly(c, false, 15f, 5f, 8f, 12f, 15f, 19f)
            ToonIc.FORWARD -> poly(c, false, 9f, 5f, 16f, 12f, 9f, 19f)
            ToonIc.CHEVRON -> poly(c, false, 9f, 6f, 15f, 12f, 9f, 18f)
            ToonIc.RELOAD -> {
                rect.set(5f, 5f, 19f, 19f)
                c.drawArc(rect, -40f, 290f, false, stroke)
                poly(c, false, 16.5f, 3.6f, 19.6f, 6.2f, 16.2f, 8.4f)
            }
            ToonIc.CLOSE -> { c.drawLine(6f, 6f, 18f, 18f, stroke); c.drawLine(18f, 6f, 6f, 18f, stroke) }
            ToonIc.PLUS -> { c.drawLine(12f, 5f, 12f, 19f, stroke); c.drawLine(5f, 12f, 19f, 12f, stroke) }
            ToonIc.DOTS -> {
                c.drawCircle(5.5f, 12f, 1.4f, fill); c.drawCircle(12f, 12f, 1.4f, fill); c.drawCircle(18.5f, 12f, 1.4f, fill)
            }
            ToonIc.SEARCH -> { c.drawCircle(10.5f, 10.5f, 6f, stroke); c.drawLine(15f, 15f, 20f, 20f, stroke) }
            ToonIc.GLOBE -> {
                c.drawCircle(12f, 12f, 8f, stroke)
                c.drawLine(4f, 12f, 20f, 12f, stroke)
                rect.set(8f, 4f, 16f, 20f); c.drawOval(rect, stroke)
                rect.set(4.6f, 7f, 19.4f, 17f); c.drawOval(rect, stroke)
            }
            ToonIc.CHECK -> poly(c, false, 5f, 12.5f, 10f, 17.5f, 19f, 7f)
            ToonIc.BOOK -> {
                path.reset()
                path.moveTo(12f, 6f)
                path.cubicTo(9f, 4f, 6f, 4f, 3.5f, 5f); path.lineTo(3.5f, 19f)
                path.cubicTo(6f, 18f, 9f, 18f, 12f, 20f)
                path.cubicTo(15f, 18f, 18f, 18f, 20.5f, 19f); path.lineTo(20.5f, 5f)
                path.cubicTo(18f, 4f, 15f, 4f, 12f, 6f); path.close()
                c.drawPath(path, stroke)
                c.drawLine(12f, 6f, 12f, 20f, stroke)
            }
            ToonIc.LIBRARY -> {
                rect.set(4f, 5f, 7f, 19f); c.drawRoundRect(rect, 0.8f, 0.8f, stroke)
                rect.set(9.5f, 5f, 12.5f, 19f); c.drawRoundRect(rect, 0.8f, 0.8f, stroke)
                poly(c, false, 15f, 6f, 20f, 7.2f, 17.6f, 19.4f, 14.4f, 18.4f)
                c.drawLine(14.4f, 18.4f, 15f, 6f, stroke)
            }
            ToonIc.TABS -> {
                rect.set(8f, 3.5f, 21f, 15f); c.drawRoundRect(rect, 2f, 2f, stroke)
                rect.set(3f, 9f, 16f, 20.5f); c.drawRoundRect(rect, 2f, 2f, stroke)
            }
            ToonIc.MENU -> { c.drawLine(4f, 7f, 20f, 7f, stroke); c.drawLine(4f, 12f, 20f, 12f, stroke); c.drawLine(4f, 17f, 14f, 17f, stroke) }
            ToonIc.BOOKMARK -> poly(c, true, 7f, 4f, 17f, 4f, 17f, 20f, 12f, 16f, 7f, 20f)
            ToonIc.CLOCK -> { c.drawCircle(12f, 12f, 8f, stroke); poly(c, false, 12f, 7f, 12f, 12f, 15.5f, 14f) }
            ToonIc.HISTORY -> {
                rect.set(4f, 4f, 20f, 20f)
                c.drawArc(rect, -50f, 300f, false, stroke)
                poly(c, false, 12.2f, 2.6f, 16.4f, 3.1f, 12.4f, 6.8f)
                poly(c, false, 12f, 8f, 12f, 12f, 15f, 13.8f)
            }
            ToonIc.SPARK -> {
                path.reset()
                path.moveTo(12f, 3f); path.lineTo(14f, 10f); path.lineTo(21f, 12f); path.lineTo(14f, 14f)
                path.lineTo(12f, 21f); path.lineTo(10f, 14f); path.lineTo(3f, 12f); path.lineTo(10f, 10f); path.close()
                c.drawPath(path, stroke)
            }
            ToonIc.BOLT -> {
                path.reset()
                path.moveTo(13f, 2.5f); path.lineTo(5f, 13.5f); path.lineTo(11f, 13.5f)
                path.lineTo(10f, 21.5f); path.lineTo(19f, 10f); path.lineTo(13f, 10f); path.close()
                c.drawPath(path, stroke)
            }
            ToonIc.SCAN -> {
                path.reset()
                path.moveTo(4f, 9f); path.lineTo(4f, 4f); path.lineTo(9f, 4f)
                path.moveTo(15f, 4f); path.lineTo(20f, 4f); path.lineTo(20f, 9f)
                path.moveTo(4f, 15f); path.lineTo(4f, 20f); path.lineTo(9f, 20f)
                path.moveTo(15f, 20f); path.lineTo(20f, 20f); path.lineTo(20f, 15f)
                c.drawPath(path, stroke)
                c.drawLine(3f, 12f, 21f, 12f, stroke)
            }
            ToonIc.BROADCAST -> {
                c.drawCircle(12f, 12f, 2f, fill)
                for (r in floatArrayOf(5.5f, 9.5f)) {
                    rect.set(12f - r, 12f - r, 12f + r, 12f + r)
                    c.drawArc(rect, -42f, 84f, false, stroke)
                    c.drawArc(rect, 138f, 84f, false, stroke)
                }
            }
            ToonIc.CROSSHAIR -> {
                c.drawCircle(12f, 12f, 4.5f, stroke)
                c.drawLine(12f, 2.5f, 12f, 7.5f, stroke); c.drawLine(12f, 16.5f, 12f, 21.5f, stroke)
                c.drawLine(2.5f, 12f, 7.5f, 12f, stroke); c.drawLine(16.5f, 12f, 21.5f, 12f, stroke)
            }
            ToonIc.SPLIT -> {
                rect.set(3.5f, 4.5f, 20.5f, 19.5f); c.drawRoundRect(rect, 3f, 3f, stroke)
                c.drawLine(3.5f, 12f, 20.5f, 12f, stroke)
            }
            ToonIc.CHAT -> {
                rect.set(4f, 5f, 20f, 16f); c.drawRoundRect(rect, 4f, 4f, stroke)
                poly(c, false, 9f, 16f, 9f, 20f, 13.5f, 16f)
            }
            ToonIc.STOP -> { rect.set(7f, 7f, 17f, 17f); c.drawRoundRect(rect, 2.5f, 2.5f, fill) }
            ToonIc.PAUSE -> {
                rect.set(8f, 6f, 10.4f, 18f); c.drawRoundRect(rect, 0.8f, 0.8f, fill)
                rect.set(13.6f, 6f, 16f, 18f); c.drawRoundRect(rect, 0.8f, 0.8f, fill)
            }
            ToonIc.PLAY -> poly(c, true, 8f, 5.5f, 19f, 12f, 8f, 18.5f)
            ToonIc.SAVE -> {
                c.drawLine(12f, 4f, 12f, 15f, stroke)
                poly(c, false, 8f, 11f, 12f, 15f, 16f, 11f)
                poly(c, false, 5f, 16f, 5f, 20f, 19f, 20f, 19f, 16f)
            }
            ToonIc.SHARE -> {
                rect.set(6f, 11.5f, 18f, 19.5f); c.drawRoundRect(rect, 2f, 2f, stroke)
                c.drawLine(12f, 15f, 12f, 3.5f, stroke)
                poly(c, false, 8.5f, 7f, 12f, 3.5f, 15.5f, 7f)
            }
            ToonIc.SLIDERS -> {
                c.drawLine(4f, 7f, 20f, 7f, stroke); c.drawLine(4f, 12f, 20f, 12f, stroke); c.drawLine(4f, 17f, 20f, 17f, stroke)
                for (p in arrayOf(floatArrayOf(9f, 7f), floatArrayOf(15f, 12f), floatArrayOf(8f, 17f))) {
                    c.drawCircle(p[0], p[1], 2.3f, fill); c.drawCircle(p[0], p[1], 2.3f, stroke)
                }
            }
            ToonIc.MOON -> {
                val outer = Path().apply { addCircle(12f, 12f, 8.2f, Path.Direction.CW) }
                val cut = Path().apply { addCircle(16.2f, 8.6f, 7f, Path.Direction.CW) }
                outer.op(cut, Path.Op.DIFFERENCE)
                c.drawPath(outer, fill)
            }
            ToonIc.SUN -> {
                c.drawCircle(12f, 12f, 4.4f, stroke)
                for (i in 0 until 8) {
                    val a = Math.toRadians(i * 45.0)
                    val cx = Math.cos(a).toFloat(); val sy = Math.sin(a).toFloat()
                    c.drawLine(12f + cx * 7.2f, 12f + sy * 7.2f, 12f + cx * 9.6f, 12f + sy * 9.6f, stroke)
                }
            }
            ToonIc.SHIELD -> {
                path.reset()
                path.moveTo(12f, 3f); path.lineTo(19f, 6f); path.lineTo(19f, 12f)
                path.cubicTo(19f, 16.5f, 16f, 19.5f, 12f, 21f)
                path.cubicTo(8f, 19.5f, 5f, 16.5f, 5f, 12f); path.lineTo(5f, 6f); path.close()
                c.drawPath(path, stroke)
            }
            ToonIc.TEXT -> {
                poly(c, false, 5f, 6f, 19f, 6f)
                c.drawLine(12f, 6f, 12f, 19f, stroke)
                c.drawLine(9f, 19f, 15f, 19f, stroke)
            }
            ToonIc.LOCK -> {
                rect.set(6f, 11f, 18f, 20f); c.drawRoundRect(rect, 2.5f, 2.5f, stroke)
                rect.set(8.5f, 4f, 15.5f, 14f); c.drawArc(rect, 180f, 180f, false, stroke)
            }
            ToonIc.DOWNLOAD -> {
                c.drawLine(12f, 4f, 12f, 15.5f, stroke)
                poly(c, false, 7.5f, 11f, 12f, 15.5f, 16.5f, 11f)
                c.drawLine(5f, 20f, 19f, 20f, stroke)
            }
            ToonIc.EXTERNAL -> {
                poly(c, false, 14f, 4f, 20f, 4f, 20f, 10f)
                c.drawLine(20f, 4f, 11.5f, 12.5f, stroke)
                poly(c, false, 18f, 14f, 18f, 20f, 4f, 20f, 4f, 6f, 10f, 6f)
            }
            ToonIc.IMAGE -> {
                rect.set(4f, 5f, 20f, 19f); c.drawRoundRect(rect, 2.5f, 2.5f, stroke)
                c.drawCircle(9f, 10f, 1.8f, stroke)
                poly(c, false, 4.8f, 18.2f, 10f, 13f, 13.5f, 16.5f, 16f, 14f, 19.2f, 17.4f)
            }
            ToonIc.CROP -> {
                poly(c, false, 7f, 2.5f, 7f, 17f, 21.5f, 17f)
                poly(c, false, 2.5f, 7f, 17f, 7f, 17f, 21.5f)
            }
            ToonIc.TRANSLATE -> {
                poly(c, false, 3.5f, 5.5f, 12.5f, 5.5f)
                c.drawLine(8f, 5.5f, 8f, 4f, stroke)
                poly(c, false, 12f, 5.5f, 6.2f, 16f)
                poly(c, false, 4f, 5.5f, 9.8f, 16f)
                poly(c, false, 14.5f, 9f, 20.5f, 9f)
                c.drawLine(17.5f, 9f, 17.5f, 20f, stroke)
                c.drawLine(14.5f, 20f, 20.5f, 20f, stroke)
            }
            ToonIc.WARNING -> {
                path.reset()
                path.moveTo(12f, 3.5f); path.lineTo(21f, 19.5f); path.lineTo(3f, 19.5f); path.close()
                c.drawPath(path, stroke)
                c.drawLine(12f, 9.5f, 12f, 14f, stroke); c.drawCircle(12f, 16.8f, 0.9f, fill)
            }
            ToonIc.INFO -> {
                c.drawCircle(12f, 12f, 8.5f, stroke)
                c.drawLine(12f, 11f, 12f, 16.5f, stroke); c.drawCircle(12f, 7.9f, 0.95f, fill)
            }
            ToonIc.CIRCLE_CHECK -> {
                c.drawCircle(12f, 12f, 8.5f, stroke)
                poly(c, false, 8f, 12.2f, 11f, 15.2f, 16.2f, 9f)
            }
            ToonIc.CIRCLE_CROSS -> {
                c.drawCircle(12f, 12f, 8.5f, stroke)
                c.drawLine(9f, 9f, 15f, 15f, stroke); c.drawLine(15f, 9f, 9f, 15f, stroke)
            }
            ToonIc.CALENDAR -> {
                rect.set(4f, 5.5f, 20f, 20f); c.drawRoundRect(rect, 2.5f, 2.5f, stroke)
                c.drawLine(4f, 10f, 20f, 10f, stroke)
                c.drawLine(8.5f, 3f, 8.5f, 7f, stroke); c.drawLine(15.5f, 3f, 15.5f, 7f, stroke)
            }
            ToonIc.TRASH -> {
                c.drawLine(4.5f, 7f, 19.5f, 7f, stroke)
                poly(c, false, 9f, 7f, 9.5f, 4f, 14.5f, 4f, 15f, 7f)
                poly(c, false, 6.5f, 7f, 7.5f, 20f, 16.5f, 20f, 17.5f, 7f)
                c.drawLine(10.2f, 10.5f, 10.7f, 16.5f, stroke); c.drawLine(13.8f, 10.5f, 13.3f, 16.5f, stroke)
            }
            ToonIc.EDIT -> {
                poly(c, false, 14.5f, 5f, 19f, 9.5f)
                poly(c, false, 4f, 20f, 4.6f, 15.6f, 14.7f, 5.5f, 18.5f, 9.3f, 8.4f, 19.4f, 4f, 20f)
            }
        }
        c.restore()
    }

    override fun setAlpha(alpha: Int) { stroke.alpha = alpha; fill.alpha = alpha }
    override fun setColorFilter(cf: android.graphics.ColorFilter?) { stroke.colorFilter = cf; fill.colorFilter = cf }
    @Suppress("OVERRIDE_DEPRECATION")
    override fun getOpacity(): Int = android.graphics.PixelFormat.TRANSLUCENT
}

/** Icon view sized by dp; tint is fixed at construction (recreate on theme change). */
fun Context.toonIcon(ic: ToonIc, color: Int, sizeDp: Int = 22): android.widget.ImageView =
    android.widget.ImageView(this).apply {
        setImageDrawable(ToonIconDrawable(ic, color))
        scaleType = android.widget.ImageView.ScaleType.FIT_CENTER
        layoutParams = android.widget.LinearLayout.LayoutParams(ToonDp.px(this@toonIcon, sizeDp), ToonDp.px(this@toonIcon, sizeDp))
    }
