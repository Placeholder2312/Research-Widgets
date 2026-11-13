package com.placeholder2312.researchwidgets.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
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

    override fun onAppWidgetOptionsChanged(context: Context, manager: AppWidgetManager, id: Int, options: android.os.Bundle) {
        updateWidget(context, manager, id)
    }

    override fun onDeleted(context: Context, ids: IntArray) {
        ids.forEach { WidgetPreferences.clear(context, it) }
    }

    override fun onRestored(context: Context, oldIds: IntArray, newIds: IntArray) {
        for (index in 0 until minOf(oldIds.size, newIds.size)) {
            val oldId = oldIds[index]
            val newId = newIds[index]
            WidgetPreferences.setMode(context, newId, WidgetPreferences.mode(context, oldId))
            WidgetPreferences.clear(context, oldId)
        }
        refreshAll(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_NEXT -> move(
                context,
                intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID),
                1
            )
            ACTION_PREVIOUS -> move(
                context,
                intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID),
                -1
            )
            else -> super.onReceive(context, intent)
        }
    }

    /** Use StackView's own current position so arrows stay in sync with card swipes. */
    private fun move(context: Context, id: Int, delta: Int) {
        if (id == AppWidgetManager.INVALID_APPWIDGET_ID) return

        val count = PaperProviderClient.read(
            context,
            WidgetPreferences.mode(context, id)
        ).papers.size
        if (count <= 1) return

        val navigation = RemoteViews(context.packageName, R.layout.widget_paper_strip)
        if (delta > 0) navigation.showNext(R.id.paper_stack) else navigation.showPrevious(R.id.paper_stack)
        AppWidgetManager.getInstance(context).partiallyUpdateAppWidget(id, navigation)
    }

    companion object {
        private const val ACTION_NEXT = "com.placeholder2312.researchwidgets.action.PAPER_NEXT"
        private const val ACTION_PREVIOUS = "com.placeholder2312.researchwidgets.action.PAPER_PREVIOUS"

        /** Below this size the chevrons are hidden to preserve a clear title area. */
        internal fun isCompact(widthDp: Int, heightDp: Int): Boolean = widthDp < 210 || heightDp < 52

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
            val count = result.papers.size
            val views = RemoteViews(context.packageName, R.layout.widget_paper_strip)

            views.setTextViewText(
                R.id.empty_text,
                if (!result.connected || result.papers.isEmpty()) result.message.uppercase() else ""
            )

            val options = manager.getAppWidgetOptions(id)
            val width = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH, 280).coerceAtLeast(1)
            val height = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT, 64).coerceAtLeast(1)
            val compact = isCompact(width, height)
            val canNavigate = !compact && count > 1

            views.setViewVisibility(R.id.previous_button, if (canNavigate) View.VISIBLE else View.GONE)
            views.setViewVisibility(R.id.next_button, if (canNavigate) View.VISIBLE else View.GONE)
            views.setViewVisibility(
                R.id.empty_dots,
                if (width >= 120 && height >= 64) View.VISIBLE else View.GONE
            )

            val density = context.resources.displayMetrics.density
            val emptyPadding = ((if (width < 140) 4 else 10) * density).toInt()
            views.setViewPadding(R.id.empty_text, emptyPadding, emptyPadding, emptyPadding, emptyPadding)
            views.setTextViewTextSize(
                R.id.empty_text,
                android.util.TypedValue.COMPLEX_UNIT_SP,
                if (width < 140) 9f else if (width < 190) 10f else 12f
            )

            val service = Intent(context, PaperStripRemoteViewsService::class.java).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id)
                data = Uri.parse("researchwidgets://paper-strip/$id")
            }
            views.setRemoteAdapter(R.id.paper_stack, service)
            views.setEmptyView(R.id.paper_stack, R.id.empty_state)

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
