package com.placeholder2312.researchwidgets.widget

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Build
import android.util.TypedValue
import android.view.View
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.placeholder2312.researchwidgets.R
import com.placeholder2312.researchwidgets.data.AftergleamContract
import com.placeholder2312.researchwidgets.data.PaperProviderClient
import com.placeholder2312.researchwidgets.data.WidgetPreferences
import kotlin.math.ceil
import kotlin.math.roundToInt

class PaperStripRemoteViewsService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        val widgetId = intent.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        )
        return PaperFactory(widgetId)
    }

    private inner class PaperFactory(private val widgetId: Int) : RemoteViewsFactory {
        private var papers = emptyList<com.placeholder2312.researchwidgets.data.WidgetPaper>()

        override fun onCreate() = reload()

        override fun onDataSetChanged() = reload()

        private fun reload() {
            papers = if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
                emptyList()
            } else {
                PaperProviderClient.read(
                    this@PaperStripRemoteViewsService,
                    WidgetPreferences.mode(this@PaperStripRemoteViewsService, widgetId)
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

            val options = AppWidgetManager.getInstance(this@PaperStripRemoteViewsService)
                .getAppWidgetOptions(widgetId)
            val width = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH, 280).coerceAtLeast(1)
            val height = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT, 64).coerceAtLeast(1)
            val compact = PaperStripWidgetProvider.isCompact(width, height)

            val horizontalPaddingDp = if (!compact) 40 else when {
                width < 120 -> 6
                width < 180 -> 10
                else -> 14
            }
            val verticalPaddingDp = when {
                height < 48 -> 2
                height < 72 -> 3
                else -> 6
            }
            val density = resources.displayMetrics.density
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                // StackView measures each item with AT_MOST. Without an explicit size, the
                // transparent FrameLayout collapses to its text width and StackView fans
                // the papers across the strip. Match StackView's 90% child viewport so the
                // active card occupies the full usable widget area and only a small peek remains.
                val stackFramePaddingPx = ceil(4f * density).toInt()
                val cardWidthDp = ((width * density * 0.9f).roundToInt() - 2 * stackFramePaddingPx) / density
                val cardHeightDp = ((height * density * 0.9f).roundToInt() - 2 * stackFramePaddingPx) / density
                views.setViewLayoutWidth(R.id.paper_card, cardWidthDp.coerceAtLeast(1f), TypedValue.COMPLEX_UNIT_DIP)
                views.setViewLayoutHeight(R.id.paper_card, cardHeightDp.coerceAtLeast(1f), TypedValue.COMPLEX_UNIT_DIP)
            }
            views.setViewPadding(
                R.id.paper_card,
                (horizontalPaddingDp * density).toInt(),
                (verticalPaddingDp * density).toInt(),
                (horizontalPaddingDp * density).toInt(),
                (verticalPaddingDp * density).toInt()
            )

            val baseTitleSize = when {
                width < 120 -> 11f
                width < 180 -> 12f
                width < 270 -> 14f
                width >= 360 && height >= 140 -> 22f
                width >= 360 && height >= 90 -> 18f
                else -> 16f
            }
            views.setTextViewTextSize(
                R.id.paper_title,
                TypedValue.COMPLEX_UNIT_SP,
                if (height < 72) minOf(baseTitleSize, 14f) else baseTitleSize
            )

            val count = papers.size
            val showDots = count > 1 && height >= 56
            views.setViewVisibility(R.id.dot_row, if (showDots) View.VISIBLE else View.GONE)
            if (showDots) bindDots(views, position, count)

            val description = listOf(
                paper.title,
                byline,
                "Paper ${position + 1} of $count",
                "Swipe up or down to browse papers. Open in Aftergleam."
            ).filter { it.isNotBlank() }.joinToString(". ") + "."
            views.setContentDescription(R.id.paper_card, description)
            views.setOnClickFillInIntent(
                R.id.paper_card,
                Intent().putExtra(AftergleamContract.EXTRA_PAPER_ID, paper.id)
            )
            return views
        }

        private fun bindDots(views: RemoteViews, position: Int, count: Int) {
            val visible = minOf(count, MAX_DOTS)
            val start = if (count <= MAX_DOTS) 0 else {
                (position - MAX_DOTS / 2).coerceIn(0, count - MAX_DOTS)
            }
            DOT_IDS.forEachIndexed { slot, viewId ->
                if (slot < visible) {
                    views.setViewVisibility(viewId, View.VISIBLE)
                    views.setImageViewResource(
                        viewId,
                        if (start + slot == position) R.drawable.nothing_dot_red else R.drawable.nothing_dot_off
                    )
                } else {
                    views.setViewVisibility(viewId, View.GONE)
                }
            }
        }

        override fun getLoadingView(): RemoteViews? = null
        override fun getViewTypeCount() = 1
        override fun getItemId(position: Int) = papers.getOrNull(position)?.id?.hashCode()?.toLong() ?: position.toLong()
        override fun hasStableIds() = true

        override fun onDestroy() {
            papers = emptyList()
        }
    }

    private companion object {
        const val MAX_DOTS = 7
        val DOT_IDS = intArrayOf(
            R.id.dot_0,
            R.id.dot_1,
            R.id.dot_2,
            R.id.dot_3,
            R.id.dot_4,
            R.id.dot_5,
            R.id.dot_6
        )
    }
}
