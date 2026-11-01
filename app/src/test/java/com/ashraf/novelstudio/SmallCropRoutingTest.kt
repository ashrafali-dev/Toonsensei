package com.ashraf.novelstudio

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Audit crops measured in the field are 96x48 .. 117x72 and always came back empty from Lens, then fell back to
 * ML Kit anyway. Full panels are ~690x1600 and must stay on the remote read.
 */
class SmallCropRoutingTest {

    @Test fun measuredAuditCrops_readLocally() {
        assertTrue(RoutingOcrEngine.preferLocalForSmallCrop(96, 48))
        assertTrue(RoutingOcrEngine.preferLocalForSmallCrop(102, 51))
        assertTrue(RoutingOcrEngine.preferLocalForSmallCrop(114, 54))
        assertTrue(RoutingOcrEngine.preferLocalForSmallCrop(117, 72))
    }

    @Test fun fullPanels_stayOnLens() {
        assertFalse(RoutingOcrEngine.preferLocalForSmallCrop(690, 1600))
        assertFalse(RoutingOcrEngine.preferLocalForSmallCrop(1080, 1920))
    }

    @Test fun boundary_isInclusive() {
        assertTrue(RoutingOcrEngine.preferLocalForSmallCrop(200, 200))
        assertFalse(RoutingOcrEngine.preferLocalForSmallCrop(201, 200))
    }

    @Test fun undecodableSize_neverRoutesLocally() {
        // A bitmap that failed to decode reports 0x0; keep it on the normal route instead of "small".
        assertFalse(RoutingOcrEngine.preferLocalForSmallCrop(0, 0))
        assertFalse(RoutingOcrEngine.preferLocalForSmallCrop(0, 50))
    }
}
