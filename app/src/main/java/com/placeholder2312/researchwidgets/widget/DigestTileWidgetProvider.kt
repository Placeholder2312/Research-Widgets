package com.placeholder2312.researchwidgets.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.placeholder2312.researchwidgets.R
import com.placeholder2312.researchwidgets.data.AftergleamContract
import com.placeholder2312.researchwidgets.data.FeedMode
import com.placeholder2312.researchwidgets.data.PaperProviderClient
import com.placeholder2312.researchwidgets.data.ProviderState

/** Inverted 2×2 tile: how many papers are in today's digest and how many are saved. Tap opens Aftergleam. */
class DigestTileWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, appWidgetIds: IntArray) {
        appWidgetIds.forEach { update(context, manager, it) }
    }

    companion object {
        private const val REQUEST_BASE = 30000

        fun refreshAll(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            manager.getAppWidgetIds(ComponentName(context, DigestTileWidgetProvider::class.java))
                .forEach { update(context, manager, it) }
        }

        private fun update(context: Context, manager: AppWidgetManager, id: Int) {
            val today = PaperProviderClient.read(context, FeedMode.RECOMMENDED)
            val saved = PaperProviderClient.read(context, FeedMode.SAVED)
            val views = RemoteViews(context.packageName, R.layout.widget_digest_tile)

            val description: String
            if (today.connected) {
                val count = today.papers.size
                val savedCount = if (saved.connected) saved.papers.size else 0
                views.setTextViewText(R.id.digest_count, "%02d".format(count))
                views.setTextViewText(R.id.digest_label, if (count == 1) "PAPER IN DIGEST" else "PAPERS IN DIGEST")
                views.setTextViewText(R.id.digest_saved, "SAVED %02d".format(savedCount))
                description = "$count papers in today's digest, $savedCount saved. Open Aftergleam."
            } else {
                val label = when (today.state) {
                    ProviderState.NOT_INSTALLED -> "INSTALL AFTERGLEAM FORK"
                    ProviderState.PERMISSION_DENIED -> "SIGNING KEY MISMATCH"
                    else -> "AFTERGLEAM UNAVAILABLE"
                }
                views.setTextViewText(R.id.digest_count, "--")
                views.setTextViewText(R.id.digest_label, label)
                views.setTextViewText(R.id.digest_saved, "")
                description = "$label. Open Aftergleam."
            }

            val launch = Intent().setComponent(
                ComponentName(AftergleamContract.PACKAGE, AftergleamContract.PACKAGE + ".MainActivity")
            )
            val tap = PendingIntent.getActivity(
                context, REQUEST_BASE + id, launch,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.digest_tile, tap)
            views.setContentDescription(R.id.digest_tile, description)
            manager.updateAppWidget(id, views)
        }
    }
}
