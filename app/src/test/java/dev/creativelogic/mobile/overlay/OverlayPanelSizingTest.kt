package dev.creativelogic.mobile.overlay

import org.junit.Assert.*
import org.junit.Test

class OverlayPanelSizingTest {
    @Test fun landscapeDefaultsUseApproximatelyOneQuarterOfScreen() {
        val width = 1536
        val height = 709
        val dimensions = OverlayPanelSizing.calculate(width, height, 2.5f,
            OverlayPanelSizing.defaultWidthPercent(width, height),
            OverlayPanelSizing.defaultHeightPercent(width, height))
        val fraction = dimensions.width.toDouble() * dimensions.height / (width * height)
        assertTrue("Expected a compact landscape overlay, got $fraction", fraction in 0.24..0.31)
    }

    @Test fun resizingClampsToDisplayAndAllowsACompactAndLargePanel() {
        val small = OverlayPanelSizing.calculate(1536, 709, 2.5f, 40, 32)
        val large = OverlayPanelSizing.calculate(1536, 709, 2.5f, 95, 92)
        assertTrue(small.width < large.width)
        assertTrue(small.height < large.height)
        assertTrue(large.width < 1536 && large.height < 709)
        assertEquals(1536 - large.width, OverlayPanelSizing.clampPosition(100000, 0,
            large.width, large.height, 1536, 709).first)
    }

    @Test fun portraitSizeAndTinyScreenAlwaysRemainValid() {
        val portrait = OverlayPanelSizing.calculate(720, 1560, 2.5f,
            OverlayPanelSizing.defaultWidthPercent(720, 1560),
            OverlayPanelSizing.defaultHeightPercent(720, 1560))
        assertTrue(portrait.width < 720 && portrait.height < 1560)
        val tiny = OverlayPanelSizing.calculate(180, 200, 3f, 50, 50)
        assertTrue(tiny.width in 1..180 && tiny.height in 1..200)
        assertEquals(0 to 0, OverlayPanelSizing.clampPosition(-100, -100, tiny.width, tiny.height, 180, 200))
    }
}
