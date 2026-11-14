package com.placeholder2312.researchwidgets.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.widget.RemoteViews
import com.placeholder2312.researchwidgets.R
import com.placeholder2312.researchwidgets.data.AftergleamLinks
import com.placeholder2312.researchwidgets.data.PaperProviderClient
import com.placeholder2312.researchwidgets.data.WidgetPreferences

/**
 * One paper title fills the widget; swiping up or down cycles papers (platform StackView).
 * Tapping a title opens that paper in Aftergleam.
 */
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
            WidgetPreferences.setMode(context, newId, WidgetPreferences.mode(context, oldId))
            WidgetPreferences.setStyle(context, newId, WidgetPreferences.style(context, oldId))
            WidgetPreferences.clear(context, oldId)
        }
        refreshAll(context)
    }

    companion object {
        /** StackView gives its cards (1 - 0.1) of its own size; see PERSPECTIVE_SHIFT_FACTOR in AOSP StackView. */
        private const val STACK_CARD_FRACTION = 0.9f

        /** Current widget size in dp: portrait is (min width, max height), landscape is (max width, min height). */
        internal fun sizeDp(context: Context, options: Bundle): Pair<Int, Int> {
            val landscape = context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
            val width = options.getInt(
                if (landscape) AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH else AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH,
                280
            )
            val height = options.getInt(
                if (landscape) AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT else AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT,
                64
            )
            return width.coerceAtLeast(1) to height.coerceAtLeast(1)
        }

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
            views.setTextViewText(
                R.id.empty_text,
                if (!result.connected || result.papers.isEmpty()) result.message.uppercase() else ""
            )

            val (width, height) = sizeDp(context, manager.getAppWidgetOptions(id))

            // Make the front card exactly widget-sized by enlarging the stack (Android 12+). The extra
            // space is above/right of the widget (stack is bottom|start anchored) and is clipped.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                views.setViewLayoutWidth(R.id.paper_stack, width / STACK_CARD_FRACTION, TypedValue.COMPLEX_UNIT_DIP)
                views.setViewLayoutHeight(R.id.paper_stack, height / STACK_CARD_FRACTION, TypedValue.COMPLEX_UNIT_DIP)
            }

            views.setViewVisibility(
                R.id.empty_dots,
                if (width >= 120 && height >= 64) View.VISIBLE else View.GONE
            )
            val density = context.resources.displayMetrics.density
            val emptyPadding = ((if (width < 140) 4 else 10) * density).toInt()
            views.setViewPadding(R.id.empty_text, emptyPadding, emptyPadding, emptyPadding, emptyPadding)
            views.setTextViewTextSize(
                R.id.empty_text,
                TypedValue.COMPLEX_UNIT_SP,
                if (width < 140) 9f else if (width < 190) 10f else 12f
            )

            val service = Intent(context, PaperStripRemoteViewsService::class.java).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id)
                data = Uri.parse("researchwidgets://paper-strip/$id")
            }
            views.setRemoteAdapter(R.id.paper_stack, service)
            views.setEmptyView(R.id.paper_stack, R.id.empty_state)

            // Taps go straight to Aftergleam's paper-open endpoint; each card adds its paper id.
            val template = PendingIntents.activity(context, 10000 + id, AftergleamLinks.openPaperTemplate(), mutable = true)
            views.setPendingIntentTemplate(R.id.paper_stack, template)

            // No setDisplayedChild: the stack keeps whichever paper the user swiped to across refreshes.
            manager.updateAppWidget(id, views)
            manager.notifyAppWidgetViewDataChanged(id, R.id.paper_stack)
        }
    }
}
