package com.placeholder2312.researchwidgets.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.placeholder2312.researchwidgets.data.AftergleamContract

/** Receives Aftergleam's protected data-changed broadcast and re-queries the read-only provider. */
class ResearchWidgetsChangeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == AftergleamContract.ACTION_DATA_CHANGED) {
            // One pass: each feed is queried once and shared by every widget.
            val pass = FeedPass(context)
            PaperStripWidgetProvider.refreshAll(context, pass)
            TapPaperWidgetProvider.refreshAll(context, pass)
            DigestTileWidgetProvider.refreshAll(context, pass)
        }
    }
}
