package com.placeholder2312.researchwidgets.widget

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterViewFlipper
import android.widget.FrameLayout
import androidx.test.core.app.ApplicationProvider
import com.placeholder2312.researchwidgets.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class PaperStripWidgetLayoutTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Test
    fun paperStripUsesOneFullViewportAdapterViewFlipper() {
        val parent = FrameLayout(context)
        val widget = LayoutInflater.from(context).inflate(R.layout.widget_paper_strip, parent, false)
        val flipper = widget.findViewById<AdapterViewFlipper>(R.id.paper_flipper)

        assertNotNull(flipper)
        assertEquals(ViewGroup.LayoutParams.MATCH_PARENT, flipper.layoutParams.width)
        assertEquals(ViewGroup.LayoutParams.MATCH_PARENT, flipper.layoutParams.height)
    }

    @Test
    fun shellClipsSinglePaperCardToTheNothingRoundedOutline() {
        val parent = FrameLayout(context)
        val widget = LayoutInflater.from(context).inflate(R.layout.widget_paper_strip, parent, false)
        val card = LayoutInflater.from(context).inflate(R.layout.widget_paper_card, parent, false)

        val shell = widget as ViewGroup
        assertTrue(shell.clipChildren)
        assertTrue(shell.clipToPadding)
        assertTrue(widget.clipToOutline)
        assertEquals(ViewGroup.LayoutParams.MATCH_PARENT, card.layoutParams.width)
        assertEquals(ViewGroup.LayoutParams.MATCH_PARENT, card.layoutParams.height)
        assertEquals(View.VISIBLE, card.findViewById<View>(R.id.dot_row).visibility)
        assertNotNull(card.findViewById(R.id.paper_title))
    }
}
