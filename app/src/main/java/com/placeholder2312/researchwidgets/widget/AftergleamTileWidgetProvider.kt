package com.placeholder2312.researchwidgets.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import com.placeholder2312.researchwidgets.R
import com.placeholder2312.researchwidgets.data.AftergleamLinks

/** Static offline 2×2 tile with the Aftergleam artwork; tapping it launches the customized app. */
class AftergleamTileWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, appWidgetIds: IntArray) {
        for (id in appWidgetIds) {
            val tap = PendingIntents.activity(context, REQUEST_BASE + id, AftergleamLinks.launchApp())
            val views = RemoteViews(context.packageName, R.layout.widget_aftergleam_tile).apply {
                setOnClickPendingIntent(R.id.aftergleam_tile, tap)
                setContentDescription(R.id.aftergleam_tile, "Open Aftergleam")
            }
            manager.updateAppWidget(id, views)
        }
    }

    private companion object {
        const val REQUEST_BASE = 20000
    }
}
