package dev.creativelogic.mobile.overlay

import kotlin.math.min
import kotlin.math.roundToInt

/** Pixel geometry only. No Android or Fortnite dependencies; fractions are screen-relative. */
internal object OverlayPanelSizing {
    data class Dimensions(val width: Int, val height: Int)

    fun isLandscape(screenWidth: Int, screenHeight: Int): Boolean = screenWidth > screenHeight

    fun defaultWidthPercent(screenWidth: Int, screenHeight: Int): Int =
        if (isLandscape(screenWidth, screenHeight)) 52 else 88

    fun defaultHeightPercent(screenWidth: Int, screenHeight: Int): Int =
        if (isLandscape(screenWidth, screenHeight)) 52 else 44

    fun calculate(
        screenWidth: Int, screenHeight: Int, density: Float,
        widthPercent: Int, heightPercent: Int,
    ): Dimensions {
        require(screenWidth > 0 && screenHeight > 0 && density > 0 && density.isFinite())
        val maxWidth = (screenWidth - (12 * density).roundToInt()).coerceAtLeast(1)
        val maxHeight = (screenHeight - (16 * density).roundToInt()).coerceAtLeast(1)
        val minWidth = min(maxWidth, (230 * density).roundToInt().coerceAtLeast(1))
        val minHeight = min(maxHeight, (120 * density).roundToInt().coerceAtLeast(1))
        return Dimensions(
            ((screenWidth * widthPercent.coerceIn(40, 95)) / 100f).roundToInt().coerceIn(minWidth, maxWidth),
            ((screenHeight * heightPercent.coerceIn(32, 92)) / 100f).roundToInt().coerceIn(minHeight, maxHeight),
        )
    }

    /** Leaves room beneath the panel for a separate, narrowly bounded floating navigator. */
    fun clampPanelPosition(
        x: Int, y: Int, panelWidth: Int, panelHeight: Int,
        screenWidth: Int, screenHeight: Int, navigatorHeight: Int, gap: Int,
    ): Pair<Int, Int> =
        x.coerceIn(0, (screenWidth - panelWidth).coerceAtLeast(0)) to
            y.coerceIn(0, (screenHeight - panelHeight - navigatorHeight - gap).coerceAtLeast(0))

    fun navigatorPosition(
        panelX: Int, panelY: Int, panelWidth: Int, panelHeight: Int,
        navWidth: Int, navHeight: Int, gap: Int, screenWidth: Int, screenHeight: Int,
    ): Pair<Int, Int> =
        (panelX + (panelWidth - navWidth) / 2).coerceIn(0, (screenWidth - navWidth).coerceAtLeast(0)) to
            (panelY + panelHeight + gap).coerceIn(0, (screenHeight - navHeight).coerceAtLeast(0))

    fun clampPosition(x: Int, y: Int, width: Int, height: Int, screenWidth: Int, screenHeight: Int): Pair<Int, Int> =
        x.coerceIn(0, (screenWidth - width).coerceAtLeast(0)) to
            y.coerceIn(0, (screenHeight - height).coerceAtLeast(0))
}
