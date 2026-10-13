package dev.creativelogic.mobile.overlay

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CoverFlowModelTest {
    @Test fun swipesRotateThreeTabsWithoutCreatingExtraDestinations() {
        assertEquals(1, CoverFlowModel.swipe(0, -38f, 28f))
        assertEquals(0, CoverFlowModel.swipe(2, -38f, 28f))
        assertEquals(2, CoverFlowModel.swipe(0, 38f, 28f))
        assertEquals(2, CoverFlowModel.swipe(2, 9f, 28f))
        assertEquals(listOf(1, 0, 2), CoverFlowModel.cardOrder(2))
        assertEquals(-1, CoverFlowModel.cardSide(2, 1))
        assertEquals(0, CoverFlowModel.cardSide(2, 2))
        assertEquals(1, CoverFlowModel.cardSide(2, 0))
    }
    @Test fun navigationNeverLeavesAFullScreenTouchSurface() {
        val panel = OverlayPanelSizing.clampPanelPosition(180, 340, 600, 320, 1536, 720, 120, 8)
        assertTrue(panel.second <= 272)
        val nav = OverlayPanelSizing.navigatorPosition(panel.first, panel.second, 600, 320, 480, 120, 8, 1536, 720)
        assertTrue(nav.first >= 0 && nav.first + 480 <= 1536)
        assertTrue(nav.second >= 0 && nav.second + 120 <= 720)
        assertEquals(panel.second + 320 + 8, nav.second)
    }
}
