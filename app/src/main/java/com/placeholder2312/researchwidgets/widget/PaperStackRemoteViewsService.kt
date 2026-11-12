package com.placeholder2312.researchwidgets.widget

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.util.TypedValue
import android.view.View
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.placeholder2312.researchwidgets.R
import com.placeholder2312.researchwidgets.data.AftergleamContract
import com.placeholder2312.researchwidgets.data.PaperProviderClient
import com.placeholder2312.researchwidgets.data.WidgetPreferences

class PaperStackRemoteViewsService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        val widgetId = intent.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        )
        return PaperFactory(widgetId)
    }

    private inner class PaperFactory(private val widgetId: Int) : RemoteViewsFactory {
        private var papers = emptyList<com.placeholder2312.researchwidgets.data.WidgetPaper>()

        override fun onCreate() {
            reload()
        }

        override fun onDataSetChanged() {
            reload()
        }

        private fun reload() {
            papers = if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
                emptyList()
            } else {
                PaperProviderClient.read(
                    this@PaperStackRemoteViewsService,
                    WidgetPreferences.mode(this@PaperStackRemoteViewsService, widgetId)
                ).papers
            }
        }

        override fun getCount() = papers.size

        override fun getViewAt(position: Int): RemoteViews? {
            val paper = papers.getOrNull(position) ?: return null
            val views = RemoteViews(packageName, R.layout.widget_paper_card)
            views.setTextViewText(R.id.paper_title, paper.title)

            val byline = listOf(paper.source, paper.authors, paper.published)
                .filter { it.isNotBlank() }
                .joinToString(" · ")
            views.setTextViewText(R.id.paper_byline, byline)

            val options = AppWidgetManager.getInstance(this@PaperStackRemoteViewsService)
                .getAppWidgetOptions(widgetId)
            val width = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH, 280).coerceAtLeast(1)
            val height = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT, 64).coerceAtLeast(1)

            val horizontalPaddingDp = when {
                width < 120 -> 4
                width < 180 -> 8
                width < 270 -> 14
                else -> 22
            }
            val verticalPaddingDp = when {
                height < 48 -> 1
                height < 72 -> 2
                else -> 3
            }
            val density = resources.displayMetrics.density
            val horizontalPaddingPx = (horizontalPaddingDp * density).toInt()
            val verticalPaddingPx = (verticalPaddingDp * density).toInt()
            views.setViewPadding(
                R.id.paper_card,
                horizontalPaddingPx,
                verticalPaddingPx,
                horizontalPaddingPx,
                verticalPaddingPx
            )

            val titleSize = when {
                width < 120 -> 11f
                width < 180 -> 12f
                width < 270 -> 14f
                width >= 360 && height >= 90 -> 18f
                else -> 16f
            }
            views.setTextViewTextSize(R.id.paper_title, TypedValue.COMPLEX_UNIT_SP, titleSize)

            val showByline = width >= 150 && height >= 64 && byline.isNotBlank()
            views.setViewVisibility(R.id.paper_byline, if (showByline) View.VISIBLE else View.GONE)
            views.setTextViewTextSize(
                R.id.paper_byline,
                TypedValue.COMPLEX_UNIT_SP,
                if (width < 220 || height < 72) 8f else 10f
            )

            views.setContentDescription(R.id.paper_card, "${paper.title}. $byline. Open in Aftergleam.")
            views.setOnClickFillInIntent(
                R.id.paper_card,
                Intent().putExtra(AftergleamContract.EXTRA_PAPER_ID, paper.id)
            )
            return views
        }

        override fun getLoadingView(): RemoteViews? = null
        override fun getViewTypeCount() = 1
        override fun getItemId(position: Int) = papers.getOrNull(position)?.id?.hashCode()?.toLong() ?: position.toLong()
        override fun hasStableIds() = true

        override fun onDestroy() {
            papers = emptyList()
        }
    }
}
