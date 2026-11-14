package com.placeholder2312.researchwidgets.ui

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import com.placeholder2312.researchwidgets.data.FeedMode
import com.placeholder2312.researchwidgets.data.PaperProviderClient
import com.placeholder2312.researchwidgets.data.WidgetPreferences
import com.placeholder2312.researchwidgets.data.WidgetStyle
import com.placeholder2312.researchwidgets.widget.TapPaperWidgetProvider

/** Setup screen for one Paper Strip instance (feed + look), styled like the widget itself. */
class PlainPaperWidgetConfigActivity : Activity() {
    private var widgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setResult(RESULT_CANCELED)
        widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
        if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        val form = NothingForm(this)
        val root = form.root()
        root.addView(form.caption("WIDGET SETUP"))
        root.addView(form.text("Research Paper Tap Strip", 28f, top = 8))
        root.addView(form.text("Choose which Aftergleam feed this widget displays.", 14f, form.muted, top = 8, bottom = 20))

        val feedGroup = form.optionGroup()
        feedGroup.addView(form.option(ID_RECOMMENDED, "Recommended — today's ranked digest"))
        feedGroup.addView(form.option(ID_SAVED, "Saved — most recently saved first"))
        feedGroup.check(if (WidgetPreferences.mode(this, widgetId) == FeedMode.SAVED) ID_SAVED else ID_RECOMMENDED)
        root.addView(feedGroup)

        root.addView(form.caption("LOOK", top = 20, bottom = 8))
        val styleGroup = form.optionGroup()
        styleGroup.addView(form.option(ID_ROW, "Row — dots under the title"))
        styleGroup.addView(form.option(ID_RAIL, "Rail — dots down the right edge, with 01/12 counter"))
        styleGroup.check(if (WidgetPreferences.style(this, widgetId) == WidgetStyle.RAIL) ID_RAIL else ID_ROW)
        root.addView(styleGroup)

        val status = form.status("Checking Aftergleam…")
        root.addView(status)
        Thread {
            val message = PaperProviderClient.read(this, FeedMode.RECOMMENDED).message
            runOnUiThread { if (!isDestroyed) status.text = message }
        }.start()

        root.addView(form.actions("Add widget", onCancel = { finish() }) {
            val mode = if (feedGroup.checkedRadioButtonId == ID_SAVED) FeedMode.SAVED else FeedMode.RECOMMENDED
            val style = if (styleGroup.checkedRadioButtonId == ID_RAIL) WidgetStyle.RAIL else WidgetStyle.ROW
            WidgetPreferences.setMode(this, widgetId, mode)
            WidgetPreferences.setStyle(this, widgetId, style)
            TapPaperWidgetProvider.updateWidget(this, widgetId)
            setResult(RESULT_OK, Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId))
            finish()
        })
        setContentView(root)
    }

    private companion object {
        const val ID_RECOMMENDED = 101
        const val ID_SAVED = 102
        const val ID_ROW = 201
        const val ID_RAIL = 202
    }
}
