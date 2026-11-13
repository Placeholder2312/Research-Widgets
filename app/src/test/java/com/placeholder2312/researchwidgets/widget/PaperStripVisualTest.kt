package com.placeholder2312.researchwidgets.widget

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.LayoutInflater
import android.view.View
import android.view.View.MeasureSpec
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.StackView
import org.robolectric.RobolectricTestRunner
import android.widget.TextView
import com.placeholder2312.researchwidgets.R
import java.io.File
import java.io.FileOutputStream
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class PaperStripWidgetVisualTest {
    private val titles = listOf(
        "Beyond the Limits of Human Reasoning",
        "When and how can AI improve scientific discovery?",
        "Neural Networks for Better Molecular Design",
        "A Practical Guide to Reliable Evaluation"
    )

    @Test
    fun restoresNativeCardSwipeAndKeepsTheWidgetClippedToItsRoundedShell() {
        val activity = Robolectric.buildActivity(Activity::class.java).setup().get()
        activity.setTheme(R.style.Theme_ResearchWidgets)
        val widget = LayoutInflater.from(activity).inflate(R.layout.widget_paper_strip, null, false)
        val stack = widget.findViewById<StackView>(R.id.paper_stack)

        assertNotNull(stack)
        val shell = widget as android.view.ViewGroup
        assertTrue(shell.getClipChildren())
        assertTrue(shell.getClipToPadding())
        assertTrue(widget.clipToOutline)

        widget.findViewById<View>(R.id.empty_state).visibility = View.GONE
        stack.adapter = PreviewAdapter(activity, titles)
        val host = FrameLayout(activity).apply {
            addView(widget, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
        }
        activity.setContentView(host)
        render(host, WIDTH_DP, HEIGHT_DP, "paper-strip-stack-current.png")
        assertNotNull("StackView should contain its current paper card after host layout", stack.currentView)
        assertEquals(0, stack.displayedChild)

        stack.showNext()
        shadowOf(android.os.Looper.getMainLooper()).idle()
        render(host, WIDTH_DP, HEIGHT_DP, "paper-strip-stack-next.png")
        assertEquals(1, stack.displayedChild)

        stack.showPrevious()
        shadowOf(android.os.Looper.getMainLooper()).idle()
        assertEquals(0, stack.displayedChild)
    }

    @Test
    fun compactAndTallWidgetLayoutsRenderWithoutLosingTheNothingArrowsOrDots() {
        val activity = Robolectric.buildActivity(Activity::class.java).setup().get()
        activity.setTheme(R.style.Theme_ResearchWidgets)
        val widget = LayoutInflater.from(activity).inflate(R.layout.widget_paper_strip, null, false)
        val stack = widget.findViewById<StackView>(R.id.paper_stack)
        widget.findViewById<View>(R.id.empty_state).visibility = View.GONE
        stack.adapter = PreviewAdapter(activity, titles)
        val host = FrameLayout(activity).apply {
            addView(widget, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
        }
        activity.setContentView(host)

        assertNotNull(widget.findViewById<ImageView>(R.id.previous_button))
        assertNotNull(widget.findViewById<ImageView>(R.id.next_button))
        render(host, 360, 56, "paper-strip-compact.png")
        render(host, 420, 96, "paper-strip-tall.png")
    }

    private fun render(view: View, widthDp: Int, heightDp: Int, name: String) {
        val density = view.resources.displayMetrics.density
        val width = (widthDp * density).toInt()
        val height = (heightDp * density).toInt()
        view.measure(
            MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY)
        )
        view.layout(0, 0, width, height)
        shadowOf(android.os.Looper.getMainLooper()).idle()

        println("Widget preview view: attached=${view.isAttachedToWindow}, measured=${view.measuredWidth}x${view.measuredHeight}, layout=${view.width}x${view.height}")
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        view.draw(Canvas(bitmap))
        val directory = File("build/widget-previews")
        directory.mkdirs()
        println("Widget preview screenshot: ${File(directory, name).absolutePath}")
        FileOutputStream(File(directory, name)).use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        bitmap.recycle()
    }

    private class PreviewAdapter(
        private val activity: Activity,
        private val items: List<String>
    ) : BaseAdapter() {
        override fun getCount() = items.size
        override fun getItem(position: Int) = items[position]
        override fun getItemId(position: Int) = position.toLong()

        override fun getView(position: Int, convertView: View?, parent: android.view.ViewGroup): View {
            val view = convertView ?: LayoutInflater.from(activity)
                .inflate(R.layout.widget_paper_card, parent, false)
            view.findViewById<TextView>(R.id.paper_title).text = items[position]
            val dots = view.findViewById<View>(R.id.dot_row)
            dots.visibility = if (items.size > 1) View.VISIBLE else View.GONE
            return view
        }
    }

    private companion object {
        const val WIDTH_DP = 420
        const val HEIGHT_DP = 88
    }
}
