package com.placeholder2312.researchwidgets.ui

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import com.placeholder2312.researchwidgets.data.DigestMode
import com.placeholder2312.researchwidgets.data.WidgetPreferences
import com.placeholder2312.researchwidgets.widget.DigestTileWidgetProvider

/** Setup screen for one Digest tile: count today's digest, or the PDFs downloaded on the device. */
class DigestConfigActivity : Activity() {
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
        root.addView(form.text("Digest Tile", 28f, top = 8))
        root.addView(form.text("Choose the big number.", 14f, form.muted, top = 8, bottom = 20))

        val group = form.optionGroup()
        group.addView(form.option(ID_LATEST, "Latest — papers in today's digest"))
        group.addView(form.option(ID_DOWNLOADED, "Downloaded — PDFs saved on this device"))
        group.check(if (WidgetPreferences.digestMode(this, widgetId) == DigestMode.DOWNLOADED) ID_DOWNLOADED else ID_LATEST)
        root.addView(group)
        root.addView(form.text("Downloaded needs the newer Aftergleam fork build.", 12f, form.muted, top = 14, bottom = 18))

        root.addView(form.actions("Add widget", onCancel = { finish() }) {
            val mode = if (group.checkedRadioButtonId == ID_DOWNLOADED) DigestMode.DOWNLOADED else DigestMode.LATEST
            WidgetPreferences.setDigestMode(this, widgetId, mode)
            DigestTileWidgetProvider.updateWidget(this, widgetId)
            setResult(RESULT_OK, Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId))
            finish()
        })
        setContentView(root)
    }

    private companion object {
        const val ID_LATEST = 301
        const val ID_DOWNLOADED = 302
    }
}
