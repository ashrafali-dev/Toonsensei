package com.ashraf.novelstudio

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The post-scan geometry refine may skip the two ONNX models (4-8 s per panel) only when doing so cannot change
 * what the reader sees: every readable line is already inside a balloon the scan already found. Anything else
 * must keep the full run, so these tests pin the conservative side.
 */
class RefineSkipInsideBubbleTest {

    private fun bubble(text: String, cx: Float, cy: Float, w: Float, h: Float) =
        WmBubble(
            src = "https://example.test/p1.jpg",
            cx = cx, cy = cy, w = w, h = h,
            angle = 0f, font = 0.02f, bg = 0, fg = 0, sourceText = text
        )

    /** A 1000x2000 panel: the balloon below is in pixels. */
    private val balloon = intArrayOf(200, 300, 600, 700)

    private fun inside(bubbles: List<WmBubble>, balloons: List<IntArray> = listOf(balloon)) =
        WebModeLogic.allLayoutInsideBalloons(bubbles, balloons, 1000, 2000)

    @Test fun singleLineInsideBalloon_isSkippable() {
        // centre (0.4, 0.25) -> (400, 500)px, box 200x100px: comfortably inside 200..600 x 300..700
        assertTrue(inside(listOf(bubble("죄송은 사실이 그렇다는 거지.", 0.4f, 0.25f, 0.2f, 0.05f))))
    }

    @Test fun twoLinesInTheSameBalloon_areSkippable() {
        assertTrue(
            inside(
                listOf(
                    bubble("첫째 줄입니다", 0.4f, 0.2f, 0.2f, 0.04f),
                    bubble("둘째 줄입니다", 0.4f, 0.27f, 0.2f, 0.04f)
                )
            )
        )
    }

    @Test fun lineOutsideEveryBalloon_keepsFullDetection() {
        // Sound effect at the bottom of the panel, far from the balloon.
        assertFalse(
            inside(
                listOf(
                    bubble("죄송은 사실이 그렇다는 거지.", 0.4f, 0.25f, 0.2f, 0.05f),
                    bubble("쿠앙", 0.5f, 0.9f, 0.2f, 0.08f)
                )
            )
        )
    }

    @Test fun noBalloonsDetected_keepsFullDetection() {
        // Nothing to compare against: never assume coverage, or a panel without any detection would be skipped.
        assertFalse(inside(listOf(bubble("텍스트", 0.4f, 0.25f, 0.2f, 0.05f)), emptyList()))
    }

    @Test fun noReadableBubbles_keepsFullDetection() {
        // Punctuation-only / digits-only boxes carry no line to protect; do not report "all inside".
        assertFalse(inside(listOf(bubble("— …", 0.4f, 0.25f, 0.2f, 0.05f))))
    }

    @Test fun boxCentreInsideButMostlySpillingOut_keepsFullDetection() {
        // Box 800x400px centred on the balloon centre: centre is inside, but only 200x400 of it is.
        assertFalse(inside(listOf(bubble("넓은 글자", 0.4f, 0.25f, 0.8f, 0.2f))))
    }

    @Test fun degeneratePanelSize_keepsFullDetection() {
        assertFalse(WebModeLogic.allLayoutInsideBalloons(listOf(bubble("글", 0.4f, 0.25f, 0.2f, 0.05f)), listOf(balloon), 0, 0))
    }

    @Test fun pixelCoordinates_pathIsAlsoCovered() {
        val ocr = OcrBlock(text = "그래.", top = 480, left = 380, cx = 400f, cy = 500f, w = 40f, h = 40f)
        assertTrue(WebModeLogic.allTextInsideBalloons(listOf(ocr), listOf(balloon)))
        val far = OcrBlock(text = "쿠앙", top = 1800, left = 400, cx = 450f, cy = 1850f, w = 100f, h = 60f)
        assertFalse(WebModeLogic.allTextInsideBalloons(listOf(ocr, far), listOf(balloon)))
    }
}
