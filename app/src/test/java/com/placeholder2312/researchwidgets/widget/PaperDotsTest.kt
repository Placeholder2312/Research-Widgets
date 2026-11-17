package com.placeholder2312.researchwidgets.widget

import org.junit.Assert.assertEquals
import org.junit.Test

/** The widgets only ever show as many dots as there are papers, up to [PaperDots.MAX_DOTS]: visible <= count. */
class PaperDotsTest {
    private fun cases(block: (visible: Int, count: Int) -> Unit) {
        for (count in 2..40) for (visible in 2..minOf(count, PaperDots.MAX_DOTS)) block(visible, count)
    }

    @Test
    fun firstPaperIsTheLastDotAndLastPaperIsTheFirstDot() = cases { visible, count ->
        assertEquals("first paper, visible=$visible count=$count", visible - 1, PaperDots.litSlot(0, count, visible))
        assertEquals("last paper, visible=$visible count=$count", 0, PaperDots.litSlot(count - 1, count, visible))
    }

    @Test
    fun oneDotPerPaperWhenEveryPaperHasADot() {
        for (count in 2..PaperDots.MAX_DOTS) for (position in 0 until count) {
            assertEquals(count - 1 - position, PaperDots.litSlot(position, count, count))
        }
    }

    @Test
    fun redDotStaysInRangeAndNeverMovesBackWhenAdvancing() = cases { visible, count ->
        var previous = Int.MAX_VALUE
        for (position in 0 until count) {
            val slot = PaperDots.litSlot(position, count, visible)
            assertEquals("in range, visible=$visible count=$count position=$position", true, slot in 0 until visible)
            assertEquals("monotonic, visible=$visible count=$count position=$position", true, slot <= previous)
            previous = slot
        }
    }
}
