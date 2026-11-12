package com.placeholder2312.researchwidgets.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.RemoteViews
import com.placeholder2312.researchwidgets.R
import com.placeholder2312.researchwidgets.data.AftergleamContract
import com.placeholder2312.researchwidgets.data.PaperProviderClient
import com.placeholder2312.researchwidgets.data.WidgetPreferences

class PaperStripWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        ids.forEach { updateWidget(context, manager, it) }
    }

    override fun onAppWidgetOptionsChanged(context: Context, manager: AppWidgetManager, id: Int, options: Bundle) {
        updateWidget(context, manager, id)
    }

    override fun onDeleted(context: Context, ids: IntArray) {
        ids.forEach { WidgetPreferences.clear(context, it) }
    }

    override fun onRestored(context: Context, oldIds: IntArray, newIds: IntArray) {
        for (index in 0 until minOf(oldIds.size, newIds.size)) {
            val oldId = oldIds[index]
            val newId = newIds[index]
            val mode = WidgetPreferences.mode(context, oldId)
            val paperIndex = WidgetPreferences.index(context, oldId)
            WidgetPreferences.setMode(context, newId, mode)
            WidgetPreferences.setIndex(context, newId, paperIndex)
            WidgetPreferences.clear(context, oldId)
        }
        refreshAll(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_NEXT -> move(context, intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID), 1)
            ACTION_PREVIOUS -> move(context, intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID), -1)
            else -> super.onReceive(context, intent)
        }
    }

    private fun move(context: Context, id: Int, delta: Int) {
        if (id == AppWidgetManager.INVALID_APPWIDGET_ID) return
        val count = PaperProviderClient.read(context, WidgetPreferences.mode(context, id)).papers.size
        if (count == 0) return
        WidgetPreferences.setIndex(context, id, (WidgetPreferences.index(context, id) + delta + count) % count)
        updateWidget(context, AppWidgetManager.getInstance(context), id)
    }

    companion object {
        private const val ACTION_NEXT = "com.placeholder2312.researchwidgets.action.PAPER_NEXT"
        private const val ACTION_PREVIOUS = "com.placeholder2312.researchwidgets.action.PAPER_PREVIOUS"

        fun refreshAll(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            manager.getAppWidgetIds(ComponentName(context, PaperStripWidgetProvider::class.java))
                .forEach { updateWidget(context, manager, it) }
        }

        fun updateWidget(context: Context, id: Int) {
            updateWidget(context, AppWidgetManager.getInstance(context), id)
        }

        private fun updateWidget(context: Context, manager: AppWidgetManager, id: Int) {
            if (id == AppWidgetManager.INVALID_APPWIDGET_ID) return

            val mode = WidgetPreferences.mode(context, id)
            val result = PaperProviderClient.read(context, mode)
            val views = RemoteViews(context.packageName, R.layout.widget_paper_strip)
            views.setTextViewText(R.id.mode_label, mode.label)
            views.setTextViewText(R.id.empty_text, if (!result.connected || result.papers.isEmpty()) result.message else "")

            val options = manager.getAppWidgetOptions(id)
            val width = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH, 280).coerceAtLeast(1)
            val height = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT, 64).coerceAtLeast(1)
            val compact = width < 210 || height < 52
            val showModeLabel = width >= 160 && height >= 60

            views.setViewVisibility(R.id.previous_button, if (compact) View.GONE else View.VISIBLE)
            views.setViewVisibility(R.id.next_button, if (compact) View.GONE else View.VISIBLE)
            views.setViewVisibility(R.id.mode_label, if (showModeLabel) View.VISIBLE else View.GONE)
            views.setTextViewTextSize(
                R.id.mode_label,
                android.util.TypedValue.COMPLEX_UNIT_SP,
                if (width < 270) 9f else 10f
            )

            val density = context.resources.displayMetrics.density
            val emptyPadding = ((if (width < 140) 4 else 10) * density).toInt()
            views.setViewPadding(R.id.empty_text, emptyPadding, emptyPadding, emptyPadding, emptyPadding)
            views.setTextViewTextSize(
                R.id.empty_text,
                android.util.TypedValue.COMPLEX_UNIT_SP,
                if (width < 140) 9f else if (width < 190) 10f else 12f
            )

            val service = Intent(context, PaperStackRemoteViewsService::class.java).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id)
                data = Uri.parse("researchwidgets://paper-strip/$id")
            }
            views.setRemoteAdapter(R.id.paper_stack, service)
            views.setEmptyView(R.id.paper_stack, R.id.empty_text)

            val openPaper = Intent(AftergleamContract.ACTION_OPEN_PAPER).setComponent(
                ComponentName(AftergleamContract.PACKAGE, AftergleamContract.PAPER_OPEN_COMPONENT)
            )
            val mutableFlag = if (Build.VERSION.SDK_INT >= 31) PendingIntent.FLAG_MUTABLE else 0
            val template = PendingIntent.getActivity(
                context,
                10000 + id,
                openPaper,
                PendingIntent.FLAG_UPDATE_CURRENT or mutableFlag
            )
            views.setPendingIntentTemplate(R.id.paper_stack, template)

            views.setOnClickPendingIntent(R.id.previous_button, movePending(context, id, ACTION_PREVIOUS))
            views.setOnClickPendingIntent(R.id.next_button, movePending(context, id, ACTION_NEXT))

            val count = result.papers.size
            val storedIndex = WidgetPreferences.index(context, id)
            val selectedIndex = if (count == 0) 0 else storedIndex % count
            if (selectedIndex != storedIndex) WidgetPreferences.setIndex(context, id, selectedIndex)
            views.setDisplayedChild(R.id.paper_stack, selectedIndex)

            manager.updateAppWidget(id, views)
            manager.notifyAppWidgetViewDataChanged(id, R.id.paper_stack)
        }

        private fun movePending(context: Context, id: Int, action: String): PendingIntent {
            val intent = Intent(context, PaperStripWidgetProvider::class.java)
                .setAction(action)
                .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id)
            val requestCode = id * 2 + if (action == ACTION_NEXT) 1 else 0
            return PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }
    }
}
