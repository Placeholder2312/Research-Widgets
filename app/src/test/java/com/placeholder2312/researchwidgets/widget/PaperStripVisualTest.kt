package com.placeholder2312.researchwidgets.widget

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Path
import android.graphics.RectF
import android.view.LayoutInflater
import android.view.View
import android.view.View.MeasureSpec
import android.view.ViewGroup
import android.widget.AdapterViewFlipper
import android.widget.BaseAdapter
import android.widget.FrameLayout
import android.widget.ImageView
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
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class PaperStripVisualTest {
    private val titles = listOf(
        "Beyond the Limits of Human Reasoning",
        "When and how can AI improve scientific discovery?",
        "Neural Networks for Better Molecular Design",
        "A Practical Guide to Reliable Evaluation"
    )

    @Test
    fun flipperShowsOneFullSizeCardAndAdvancesBetweenPages() {
        val activity = Robolectric.buildActivity(Activity::class.java).setup().get()
        activity.setTheme(R.style.Theme_ResearchWidgets)
        val widget = LayoutInflater.from(activity).inflate(R.layout.widget_paper_strip, null, false)
        val flipper = widget.findViewById<AdapterViewFlipper>(R.id.paper_flipper)

        assertNotNull(flipper)
        val shell = widget as ViewGroup
        assertTrue(shell.clipChildren)
        assertTrue(shell.clipToPadding)
        assertTrue(widget.clipToOutline)

        widget.findViewById<View>(R.id.empty_state).visibility = View.GONE
        flipper.adapter = PreviewAdapter(activity, titles, WIDTH_DP, HEIGHT_DP)
        val host = FrameLayout(activity).apply {
            addView(widget, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
        }
        activity.setContentView(host)
        render(host, WIDTH_DP, HEIGHT_DP, "paper-strip-card-current.png")
        shadowOf(android.os.Looper.getMainLooper()).idle()
        assertEquals(0, flipper.displayedChild)
        assertNotNull(flipper.currentView)
        assertTrue(
            "The visible card must fill at least 95% of the flipper width",
            flipper.currentView!!.width >= flipper.width * 0.95f
        )

        flipper.showNext()
        shadowOf(android.os.Looper.getMainLooper()).idle()
        render(host, WIDTH_DP, HEIGHT_DP, "paper-strip-card-next.png")
        assertEquals(1, flipper.displayedChild)

        flipper.showPrevious()
        shadowOf(android.os.Looper.getMainLooper()).idle()
        assertEquals(0, flipper.displayedChild)
    }

    @Test
    fun compactAndTallCardsKeepNothingArrowsAndPositionDots() {
        val activity = Robolectric.buildActivity(Activity::class.java).setup().get()
        activity.setTheme(R.style.Theme_ResearchWidgets)
        val widget = LayoutInflater.from(activity).inflate(R.layout.widget_paper_strip, null, false)
        val flipper = widget.findViewById<AdapterViewFlipper>(R.id.paper_flipper)
        widget.findViewById<View>(R.id.empty_state).visibility = View.GONE
        val adapter = PreviewAdapter(activity, titles, 360, 56)
        flipper.adapter = adapter
        val host = FrameLayout(activity).apply {
            addView(widget, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
        }
        activity.setContentView(host)

        assertNotNull(widget.findViewById<ImageView>(R.id.previous_button))
        assertNotNull(widget.findViewById<ImageView>(R.id.next_button))
        render(host, 360, 56, "paper-strip-card-compact.png")
        adapter.setWidgetSize(420, 96)
        shadowOf(android.os.Looper.getMainLooper()).idle()
        render(host, 420, 96, "paper-strip-card-tall.png")
    }

    private fun render(view: View, widthDp: Int, heightDp: Int, name: String) {
        val density = view.resources.displayMetrics.density
        val width = (widthDp * density).toInt()
        val height = (heightDp * density).toInt()
        shadowOf(android.os.Looper.getMainLooper()).idle()
        view.measure(
            MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY)
        )
        view.layout(0, 0, width, height)

        // Emulate the launcher clipping the widget root to its Nothing rounded background.
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val radius = 28f * density
        val outline = Path().apply {
            addRoundRect(RectF(0f, 0f, width.toFloat(), height.toFloat()), radius, radius, Path.Direction.CW)
        }
        canvas.save()
        canvas.clipPath(outline)
        view.draw(canvas)
        canvas.restore()

        val directory = File("build/widget-previews")
        directory.mkdirs()
        println("Widget preview screenshot: ${File(directory, name).absolutePath} (${width}x${height})")
        FileOutputStream(File(directory, name)).use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        bitmap.recycle()
    }

    private class PreviewAdapter(
        private val activity: Activity,
        private val items: List<String>,
        private var widthDp: Int,
        private var heightDp: Int
    ) : BaseAdapter() {
        fun setWidgetSize(width: Int, height: Int) {
            widthDp = width
            heightDp = height
            notifyDataSetChanged()
        }

        override fun getCount() = items.size
        override fun getItem(position: Int) = items[position]
        override fun getItemId(position: Int) = position.toLong()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = convertView ?: LayoutInflater.from(activity)
                .inflate(R.layout.widget_paper_card, parent, false)
            val compact = widthDp < 210 || heightDp < 52
            val horizontalPaddingDp = if (!compact) 40 else when {
                widthDp < 120 -> 6
                widthDp < 180 -> 10
                else -> 14
            }
            val verticalPaddingDp = when {
                heightDp < 48 -> 2
                heightDp < 72 -> 3
                else -> 6
            }
            val density = activity.resources.displayMetrics.density
            view.setPadding(
                (horizontalPaddingDp * density).toInt(),
                (verticalPaddingDp * density).toInt(),
                (horizontalPaddingDp * density).toInt(),
                (verticalPaddingDp * density).toInt()
            )
            val baseTitleSize = when {
                widthDp < 120 -> 11f
                widthDp < 180 -> 12f
                widthDp < 270 -> 14f
                widthDp >= 360 && heightDp >= 140 -> 22f
                widthDp >= 360 && heightDp >= 90 -> 18f
                else -> 16f
            }
            view.findViewById<TextView>(R.id.paper_title).apply {
                text = items[position]
                setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, if (heightDp < 72) minOf(baseTitleSize, 14f) else baseTitleSize)
            }
            view.findViewById<View>(R.id.dot_row).visibility =
                if (items.size > 1 && heightDp >= 56) View.VISIBLE else View.GONE
            return view
        }
    }

    private companion object {
        const val WIDTH_DP = 420
        const val HEIGHT_DP = 88
    }
}
