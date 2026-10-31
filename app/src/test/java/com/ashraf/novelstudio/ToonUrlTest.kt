package com.ashraf.novelstudio

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Guards the chapter-position helper behind the dock's chatbot ring (B01). The ring draws
 * `chapterNo(url) * 100 / total`, so the number has to be found for the URL shapes reader sites use and
 * has to stay null when a URL carries no chapter number.
 */
class ToonUrlTest {

    private fun chapterNo(url: String): Double? {
        val path = try { java.net.URI(url).path.orEmpty() } catch (_: Exception) { return null }
        val m = Regex("(?i)(?:chapter|chap|episode|ep|ch)[-_/=.]?(\\d+(?:\\.\\d+)?)").findAll(path).lastOrNull() ?: return null
        return m.groupValues[1].toDoubleOrNull()
    }

    @Test fun readsTheUsualShapes() {
        assertEquals(128.0, chapterNo("https://wtr-lab.com/en/serie-1/x/chapter-128")!!, 0.0)
        assertEquals(3.0, chapterNo("https://comic-site.com/series/12/ch-3")!!, 0.0)
        assertEquals(7.0, chapterNo("https://site.com/novel/9/episode/7")!!, 0.0)
        assertEquals(41.0, chapterNo("https://qidian.com/book/1/chapter_41.html")!!, 0.0)
        assertEquals(3.5, chapterNo("https://site.com/chapter-3.5")!!, 0.0)
    }

    @Test fun takesTheLastNumber() {
        assertEquals(54.0, chapterNo("https://site.com/ep-1/chapter-54")!!, 0.0)
    }

    @Test fun nullWhenThereIsNoChapter() {
        assertEquals(null, chapterNo("https://site.com/"))
        assertEquals(null, chapterNo("https://site.com/series/12/"))
        assertEquals(null, chapterNo("not a url"))
    }
}
