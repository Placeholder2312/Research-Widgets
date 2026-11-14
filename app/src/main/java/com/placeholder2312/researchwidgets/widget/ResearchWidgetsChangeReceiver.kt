package com.placeholder2312.researchwidgets.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.placeholder2312.researchwidgets.data.AftergleamContract

/** Receives Aftergleam's protected data-changed broadcast and re-queries the read-only provider. */
class ResearchWidgetsChangeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == AftergleamContract.ACTION_DATA_CHANGED) {
            PaperStripWidgetProvider.refreshAll(context)
        }
    }
}
