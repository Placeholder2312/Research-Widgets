package com.placeholder2312.researchwidgets.ui

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import com.placeholder2312.researchwidgets.R
import com.placeholder2312.researchwidgets.data.FeedMode
import com.placeholder2312.researchwidgets.data.PaperProviderClient
import com.placeholder2312.researchwidgets.data.WidgetPreferences
import com.placeholder2312.researchwidgets.data.WidgetStyle
import com.placeholder2312.researchwidgets.widget.PaperStripWidgetProvider

/** Launcher setup screen for one Paper Strip instance (feed + look), styled like the widget itself. */
class WidgetConfigActivity : Activity() {
    private var widgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    private val ink by lazy { getColor(R.color.nothing_ink) }
    private val muted by lazy { getColor(R.color.nothing_muted) }
    private val page by lazy { getColor(R.color.nothing_page) }
    private val light: Typeface = Typeface.create("sans-serif-light", Typeface.NORMAL)
    private val mono: Typeface = Typeface.MONOSPACE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setResult(RESULT_CANCELED)
        widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
        if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(page)
            setPadding(dp(24), dp(40), dp(24), dp(24))
        }
        root.addView(caption("WIDGET SETUP", top = 0))
        root.addView(text("Research Paper Strip", 28f, ink, top = 8))
        root.addView(text("Choose which Aftergleam feed this widget displays.", 14f, muted, top = 8, bottom = 20))

        val feedGroup = optionGroup()
        val recommended = option(ID_RECOMMENDED, "Recommended — today's ranked digest")
        val saved = option(ID_SAVED, "Saved — most recently saved first")
        feedGroup.addView(recommended)
        feedGroup.addView(saved)
        feedGroup.check(if (WidgetPreferences.mode(this, widgetId) == FeedMode.SAVED) ID_SAVED else ID_RECOMMENDED)
        root.addView(feedGroup)

        root.addView(caption("LOOK", top = 20, bottom = 8))
        val styleGroup = optionGroup()
        styleGroup.addView(option(ID_ROW, "Row — dots under the title"))
        styleGroup.addView(option(ID_RAIL, "Rail — dots down the right edge, with 01/12 counter"))
        styleGroup.check(if (WidgetPreferences.style(this, widgetId) == WidgetStyle.RAIL) ID_RAIL else ID_ROW)
        root.addView(styleGroup)

        val status = TextView(this).apply {
            text = "Checking Aftergleam…"
            typeface = mono
            textSize = 12f
            setTextColor(muted)
            setPadding(0, dp(18), 0, dp(18))
        }
        root.addView(status)
        Thread {
            val message = PaperProviderClient.read(this, FeedMode.RECOMMENDED).message
            runOnUiThread { if (!isDestroyed) status.text = message }
        }.start()

        val actions = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        actions.addView(
            pill("Cancel", filled = false).apply { setOnClickListener { finish() } },
            LinearLayout.LayoutParams(0, dp(52), 1f).apply { marginEnd = dp(8) }
        )
        actions.addView(
            pill("Add widget", filled = true).apply {
                setOnClickListener {
                    val mode = if (feedGroup.checkedRadioButtonId == ID_SAVED) FeedMode.SAVED else FeedMode.RECOMMENDED
                    val style = if (styleGroup.checkedRadioButtonId == ID_RAIL) WidgetStyle.RAIL else WidgetStyle.ROW
                    confirm(mode, style)
                }
            },
            LinearLayout.LayoutParams(0, dp(52), 1f).apply { marginStart = dp(8) }
        )
        root.addView(actions)
        setContentView(root)
    }

    private fun confirm(mode: FeedMode, style: WidgetStyle) {
        WidgetPreferences.setMode(this, widgetId, mode)
        WidgetPreferences.setStyle(this, widgetId, style)
        PaperStripWidgetProvider.updateWidget(this, widgetId)
        setResult(RESULT_OK, Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId))
        finish()
    }

    private fun caption(label: String, top: Int, bottom: Int = 0) =
        text(label, 11f, muted, top, bottom, mono).apply { letterSpacing = 0.12f }

    private fun text(
        label: String, size: Float, color: Int, top: Int, bottom: Int = 0, face: Typeface = light
    ) = TextView(this).apply {
        text = label
        typeface = face
        textSize = size
        setTextColor(color)
        setPadding(0, dp(top), 0, dp(bottom))
    }

    private fun optionGroup() = RadioGroup(this).apply {
        orientation = RadioGroup.VERTICAL
        setBackgroundResource(R.drawable.nothing_card_bg)
        setPadding(dp(16), dp(8), dp(16), dp(8))
    }

    private fun option(optionId: Int, label: String) = RadioButton(this).apply {
        id = optionId
        text = label
        typeface = light
        textSize = 15f
        setTextColor(ink)
        setButtonDrawable(R.drawable.nothing_radio)
        setPadding(dp(14), dp(14), 0, dp(14))
    }

    private fun pill(label: String, filled: Boolean) = Button(this).apply {
        text = label
        typeface = mono
        textSize = 13f
        letterSpacing = 0.1f
        stateListAnimator = null
        setBackgroundResource(if (filled) R.drawable.nothing_button_filled else R.drawable.nothing_button_outline)
        setTextColor(if (filled) page else ink)
    }

    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()

    private companion object {
        const val ID_RECOMMENDED = 101
        const val ID_SAVED = 102
        const val ID_ROW = 201
        const val ID_RAIL = 202
    }
}
