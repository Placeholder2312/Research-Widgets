package com.placeholder2312.researchwidgets.widget

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.StackView
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
    fun paperStripUsesNativeStackViewCardNavigation() {
        val parent = FrameLayout(context)
        val widget = LayoutInflater.from(context).inflate(R.layout.widget_paper_strip, parent, false)
        val stack = widget.findViewById<StackView>(R.id.paper_stack)

        assertNotNull(stack)
        assertEquals(ViewGroup.LayoutParams.MATCH_PARENT, stack.layoutParams.width)
        assertEquals(ViewGroup.LayoutParams.MATCH_PARENT, stack.layoutParams.height)
    }

    @Test
    fun shellClipsStackCardsToTheNothingRoundedOutline() {
        val parent = FrameLayout(context)
        val widget = LayoutInflater.from(context).inflate(R.layout.widget_paper_strip, parent, false)
        val card = LayoutInflater.from(context).inflate(R.layout.widget_paper_card, parent, false)

        val shell = widget as ViewGroup
        assertTrue(shell.getClipChildren())
        assertTrue(shell.getClipToPadding())
        assertTrue(widget.clipToOutline)
        assertEquals(ViewGroup.LayoutParams.MATCH_PARENT, card.layoutParams.width)
        assertEquals(ViewGroup.LayoutParams.MATCH_PARENT, card.layoutParams.height)
        assertEquals(View.VISIBLE, card.findViewById<View>(R.id.dot_row).visibility)
        assertNotNull(card.findViewById(R.id.paper_title))
    }
}
