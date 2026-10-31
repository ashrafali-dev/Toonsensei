package com.ashraf.novelstudio

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.pow

/**
 * Guards the Paper & Seal palette: every token pair used for text must keep WCAG AA (4.5:1) in both
 * themes, and the two palettes must expose the same roles. Pure JVM (no Android classes needed because
 * ToonTheme stores plain ARGB Ints).
 */
class ToonThemeTest {

    private fun channel(v: Double): Double =
        if (v <= 0.03928) v / 12.92 else ((v + 0.055) / 1.055).pow(2.4)

    private fun luminance(argb: Int): Double {
        val r = channel(((argb shr 16) and 0xFF) / 255.0)
        val g = channel(((argb shr 8) and 0xFF) / 255.0)
        val b = channel((argb and 0xFF) / 255.0)
        return 0.2126 * r + 0.7152 * g + 0.0722 * b
    }

    private fun ratio(a: Int, b: Int): Double {
        val la = luminance(a)
        val lb = luminance(b)
        return (maxOf(la, lb) + 0.05) / (minOf(la, lb) + 0.05)
    }

    private fun textPairs(c: ToonColors) = listOf(
        "title on bg" to (c.title to c.bg),
        "title on surface" to (c.title to c.surface),
        "text on surface" to (c.text to c.surface),
        "muted on surface" to (c.muted to c.surface),
        "subtext on bg" to (c.subtext to c.bg),
        "accent on surface" to (c.accent to c.surface),
        "onInk on ink" to (c.onInk to c.ink),
        "onAccent on accent" to (c.onAccent to c.accent),
        "dangerInk on dangerBg" to (c.dangerInk to c.dangerBg),
        "warnInk on warnBg" to (c.warnInk to c.warnBg),
        "infoInk on infoBg" to (c.infoInk to c.infoBg),
        "success on bg" to (c.success to c.bg),
        "novel seal on bg" to (c.novel to c.bg),
        "scan seal on bg" to (c.scan to c.bg),
        "live seal on bg" to (c.live to c.bg),
        "manual seal on bg" to (c.manual to c.bg)
    )

    @Test fun textTokensMeetAaInBothThemes() {
        for ((name, colors) in listOf("day" to ToonTheme.day, "night" to ToonTheme.night)) {
            for ((label, pair) in textPairs(colors)) {
                val r = ratio(pair.first, pair.second)
                assertTrue("$name $label ratio %.2f < 4.5".format(r), r >= 4.5)
            }
        }
    }

    @Test fun paperAndInkStayDistinct() {
        assertTrue(ToonTheme.day.bg != ToonTheme.day.ink)
        assertTrue(ToonTheme.night.bg != ToonTheme.night.ink)
        assertTrue(ToonTheme.day.modeColor(0) != ToonTheme.day.modeColor(1))
    }

    @Test fun modeSealsAreDistinct() {
        val d = ToonTheme.day
        val seals = (0..3).map { ToonTheme.modeColor(d, it) }
        assertEquals(4, seals.toSet().size)
        val n = ToonTheme.night
        assertEquals(4, (0..3).map { ToonTheme.modeColor(n, it) }.toSet().size)
    }
}
