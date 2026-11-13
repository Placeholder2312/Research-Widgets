package com.placeholder2312.researchwidgets.widget

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ListView
import androidx.test.core.app.ApplicationProvider
import com.placeholder2312.researchwidgets.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertFalse
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class PaperStripWidgetLayoutTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Test
    fun stripUsesAFullViewportListInsteadOfTheMultiCardStackView() {
        val parent = FrameLayout(context)
        val widget = LayoutInflater.from(context).inflate(R.layout.widget_paper_strip, parent, false)

        val list = widget.findViewById<ListView>(R.id.paper_list)

        assertNotNull(list)
        assertEquals(ViewGroup.LayoutParams.MATCH_PARENT, list.layoutParams.height)
        assertFalse(list.isVerticalScrollBarEnabled)
    }

    @Test
    fun eachPaperRowFillsTheViewportAndKeepsTheNothingTitleAndDots() {
        val parent = FrameLayout(context)
        val card = LayoutInflater.from(context).inflate(R.layout.widget_paper_card, parent, false)

        assertEquals(ViewGroup.LayoutParams.MATCH_PARENT, card.layoutParams.width)
        assertEquals(ViewGroup.LayoutParams.MATCH_PARENT, card.layoutParams.height)
        assertNotNull(card.findViewById(R.id.paper_title))
        assertNotNull(card.findViewById(R.id.dot_row))
    }
}
