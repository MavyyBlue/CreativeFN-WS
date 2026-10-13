package dev.creativelogic.mobile.overlay

/** Three fixed destinations; visual placement never changes document or simulator data. */
internal object CoverFlowModel {
    val titles = listOf("Steps", "Links", "Graph")
    fun selectedTab(value: Int): Int = value.coerceIn(titles.indices)
    fun swipe(current: Int, deltaX: Float, threshold: Float): Int {
        require(threshold > 0f)
        return when {
            deltaX <= -threshold -> (selectedTab(current) + 1) % titles.size
            deltaX >= threshold -> (selectedTab(current) + titles.size - 1) % titles.size
            else -> selectedTab(current)
        }
    }
    fun cardSide(selected: Int, index: Int): Int {
        require(index in titles.indices)
        val center = selectedTab(selected)
        return when (index) {
            center -> 0
            (center + titles.size - 1) % titles.size -> -1
            else -> 1
        }
    }
    /** Index of each side card in cyclic order; selected card is rendered last (on top). */
    fun cardOrder(selected: Int): List<Int> {
        val center = selectedTab(selected)
        return listOf((center + 2) % titles.size, (center + 1) % titles.size, center)
    }
}
