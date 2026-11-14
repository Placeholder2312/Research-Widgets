package com.placeholder2312.researchwidgets.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/** One-shot refresh on boot and after app replacement; no periodic service or polling. */
class WidgetLifecycleReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            PaperStripWidgetProvider.refreshAll(context)
            TapPaperWidgetProvider.refreshAll(context)
            DigestTileWidgetProvider.refreshAll(context)
        }
    }
}
