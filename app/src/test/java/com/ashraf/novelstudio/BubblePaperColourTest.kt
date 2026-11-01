package com.ashraf.novelstudio

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * A white speech balloon sitting on black artwork. The ring just outside the lettering is the balloon (light); the
 * ring further out is the page (flat BLACK). The cleaner used to adopt that black ring as the bubble background and
 * fill every erased glyph pixel with it, painting a black bar across the balloon.
 *
 * [WmBubbleStyle] and its helpers are Android-typed, so this pins the colour predicate the fix depends on rather
 * than the bitmap walk itself.
 */
class BubblePaperColourTest {

    private fun rgb(r: Int, g: Int, b: Int): Int = (0xFF shl 24) or (r shl 16) or (g shl 8) or b

    private val white = rgb(255, 255, 255)
    private val nearWhite = rgb(246, 244, 240)
    private val black = rgb(0, 0, 0)

    @Test fun blackArtworkAroundAWhiteBalloon_isNotTheSamePaper() {
        // The exact failure: flat black ring outside a white balloon must never become the balloon colour.
        assertFalse(WmBubbleStyle.samePaper(black, white))
        assertFalse(WmBubbleStyle.samePaper(white, black))
    }

    @Test fun slightlyTintedBalloonPaper_isStillTheSamePaper() {
        // A gradient/scan artefact on the balloon itself must keep working as before.
        assertTrue(WmBubbleStyle.samePaper(nearWhite, white))
    }

    @Test fun midGreyArtwork_isNotTheSamePaper() {
        assertFalse(WmBubbleStyle.samePaper(rgb(90, 90, 90), white))
    }

    @Test fun darkBalloonOnWhitePage_isConsistent() {
        // The mirrored case (dark bubble on white page) must behave the same way.
        val darkBubble = rgb(40, 42, 48)
        assertFalse(WmBubbleStyle.samePaper(white, darkBubble))
        assertTrue(WmBubbleStyle.samePaper(rgb(44, 46, 52), darkBubble))
    }
}
