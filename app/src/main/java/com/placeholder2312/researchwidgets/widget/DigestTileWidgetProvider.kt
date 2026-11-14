package com.placeholder2312.researchwidgets.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.RemoteViews
import com.placeholder2312.researchwidgets.R
import com.placeholder2312.researchwidgets.data.DigestMode
import com.placeholder2312.researchwidgets.data.FeedMode
import com.placeholder2312.researchwidgets.data.PaperProviderClient
import com.placeholder2312.researchwidgets.data.ProviderState
import com.placeholder2312.researchwidgets.data.WidgetPreferences

/**
 * Inverted 2×2 tile with one big number. LATEST: papers in today's digest (saved count underneath).
 * DOWNLOADED: PDFs stored on the device (today's count underneath). Tap opens Aftergleam.
 */
class DigestTileWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, appWidgetIds: IntArray) {
        appWidgetIds.forEach { update(context, manager, it) }
    }

    override fun onAppWidgetOptionsChanged(context: Context, manager: AppWidgetManager, id: Int, options: Bundle) {
        update(context, manager, id)
    }

    override fun onDeleted(context: Context, ids: IntArray) {
        ids.forEach { WidgetPreferences.clear(context, it) }
    }

    override fun onRestored(context: Context, oldIds: IntArray, newIds: IntArray) {
        for (index in 0 until minOf(oldIds.size, newIds.size)) {
            WidgetPreferences.setDigestMode(context, newIds[index], WidgetPreferences.digestMode(context, oldIds[index]))
            WidgetPreferences.clear(context, oldIds[index])
        }
        refreshAll(context)
    }

    companion object {
        private const val REQUEST_BASE = 30000

        fun refreshAll(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            manager.getAppWidgetIds(ComponentName(context, DigestTileWidgetProvider::class.java))
                .forEach { update(context, manager, it) }
        }

        fun updateWidget(context: Context, id: Int) = update(context, AppWidgetManager.getInstance(context), id)

        private fun update(context: Context, manager: AppWidgetManager, id: Int) {
            if (id == AppWidgetManager.INVALID_APPWIDGET_ID) return
            val mode = WidgetPreferences.digestMode(context, id)
            val today = PaperProviderClient.read(context, FeedMode.RECOMMENDED)
            val views = RemoteViews(context.packageName, R.layout.widget_digest_tile)

            val description: String
            if (!today.connected) {
                val label = when (today.state) {
                    ProviderState.NOT_INSTALLED -> "INSTALL AFTERGLEAM FORK"
                    ProviderState.PERMISSION_DENIED -> "SIGNING KEY MISMATCH"
                    else -> "AFTERGLEAM UNAVAILABLE"
                }
                show(views, tag = "TODAY", big = "--", label = label, footer = "")
                description = "$label. Open Aftergleam."
            } else if (mode == DigestMode.DOWNLOADED) {
                val downloaded = PaperProviderClient.read(context, FeedMode.DOWNLOADED)
                if (downloaded.connected) {
                    val count = downloaded.papers.size
                    show(views, "OFFLINE", "%02d".format(count), if (count == 1) "PDF ON DEVICE" else "PDFS ON DEVICE", "TODAY %02d".format(today.papers.size))
                    description = "$count PDFs downloaded on this device. Open Aftergleam."
                } else {
                    // An Aftergleam build from before the downloaded feed existed rejects the request.
                    show(views, "OFFLINE", "--", "UPDATE AFTERGLEAM FORK", "TODAY %02d".format(today.papers.size))
                    description = "Update the Aftergleam fork to count downloads. Open Aftergleam."
                }
            } else {
                val count = today.papers.size
                val saved = PaperProviderClient.read(context, FeedMode.SAVED)
                val savedCount = if (saved.connected) saved.papers.size else 0
                show(views, "TODAY", "%02d".format(count), if (count == 1) "PAPER IN DIGEST" else "PAPERS IN DIGEST", "SAVED %02d".format(savedCount))
                description = "$count papers in today's digest, $savedCount saved. Open Aftergleam."
            }

            val tap = PendingIntents.activity(
                context, REQUEST_BASE + id, Intent(context, OpenInAftergleamActivity::class.java)
            )
            views.setOnClickPendingIntent(R.id.digest_tile, tap)
            views.setContentDescription(R.id.digest_tile, description)
            manager.updateAppWidget(id, views)
        }

        private fun show(views: RemoteViews, tag: String, big: String, label: String, footer: String) {
            views.setTextViewText(R.id.digest_tag, tag)
            views.setTextViewText(R.id.digest_count, big)
            views.setTextViewText(R.id.digest_label, label)
            views.setTextViewText(R.id.digest_saved, footer)
        }
    }
}
