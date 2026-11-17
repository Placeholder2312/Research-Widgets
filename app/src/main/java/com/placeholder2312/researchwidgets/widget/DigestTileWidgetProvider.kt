package com.placeholder2312.researchwidgets.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.RemoteViews
import com.placeholder2312.researchwidgets.R
import com.placeholder2312.researchwidgets.data.AftergleamContract
import com.placeholder2312.researchwidgets.data.DigestMode
import com.placeholder2312.researchwidgets.data.FeedMode
import com.placeholder2312.researchwidgets.data.ProviderState
import com.placeholder2312.researchwidgets.data.WidgetPreferences

/**
 * Inverted 2×2 tile with one big number. LATEST: papers in today's digest (saved count underneath).
 * DOWNLOADED: PDFs stored on the device (today's count underneath). Tap opens Aftergleam.
 */
class DigestTileWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, appWidgetIds: IntArray) {
        val pass = FeedPass(context)
        appWidgetIds.forEach { update(context, manager, it, pass) }
    }

    override fun onAppWidgetOptionsChanged(context: Context, manager: AppWidgetManager, id: Int, options: Bundle) {
        // Resizing fires this repeatedly and the counts have not changed, so reuse a recent read.
        update(context, manager, id, FeedPass(context, fresh = false))
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

        fun refreshAll(context: Context) = refreshAll(context, FeedPass(context))

        internal fun refreshAll(context: Context, pass: FeedPass) {
            val manager = AppWidgetManager.getInstance(context)
            manager.getAppWidgetIds(ComponentName(context, DigestTileWidgetProvider::class.java))
                .forEach { update(context, manager, it, pass) }
        }

        fun updateWidget(context: Context, id: Int) = update(context, AppWidgetManager.getInstance(context), id)

        private fun update(context: Context, manager: AppWidgetManager, id: Int, pass: FeedPass = FeedPass(context)) {
            if (id == AppWidgetManager.INVALID_APPWIDGET_ID) return
            val mode = WidgetPreferences.digestMode(context, id)
            val today = pass.read(FeedMode.RECOMMENDED)
            val views = RemoteViews(context.packageName, R.layout.widget_digest_tile)

            val description: String
            // Only a fork new enough to serve the downloaded feed also understands the Library shortcut.
            var opensOfflineShelf = false
            if (!today.connected) {
                val label = when (today.state) {
                    ProviderState.NOT_INSTALLED -> "INSTALL AFTERGLEAM FORK"
                    ProviderState.PERMISSION_DENIED -> "SIGNING KEY MISMATCH"
                    else -> "AFTERGLEAM UNAVAILABLE"
                }
                show(views, tag = "TODAY", big = "--", label = label, footer = "")
                description = "$label. Open Aftergleam."
            } else if (mode == DigestMode.DOWNLOADED) {
                val downloaded = pass.read(FeedMode.DOWNLOADED)
                if (downloaded.connected) {
                    val count = downloaded.papers.size
                    show(views, "OFFLINE", "%02d".format(count), if (count == 1) "PDF ON DEVICE" else "PDFS ON DEVICE", "TODAY %02d".format(today.papers.size))
                    description = "$count PDFs downloaded on this device. Open the offline library."
                    opensOfflineShelf = true
                } else {
                    // An Aftergleam build from before the downloaded feed existed rejects the request.
                    show(views, "OFFLINE", "--", "UPDATE AFTERGLEAM FORK", "TODAY %02d".format(today.papers.size))
                    description = "Update the Aftergleam fork to count downloads. Open Aftergleam."
                }
            } else {
                val count = today.papers.size
                val saved = pass.read(FeedMode.SAVED)
                val savedCount = if (saved.connected) saved.papers.size else 0
                show(views, "TODAY", "%02d".format(count), if (count == 1) "PAPER IN DIGEST" else "PAPERS IN DIGEST", "SAVED %02d".format(savedCount))
                description = "$count papers in today's digest, $savedCount saved. Open Aftergleam."
            }

            val handOff = Intent(context, OpenInAftergleamActivity::class.java)
            // Downloaded mode opens the Library on its Offline chip; Latest mode just opens Aftergleam.
            if (opensOfflineShelf) {
                handOff.putExtra(AftergleamContract.EXTRA_LIBRARY_SHELF, AftergleamContract.SHELF_DOWNLOADED)
            }
            val tap = PendingIntents.activity(context, REQUEST_BASE + id, handOff)
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
