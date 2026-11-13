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
            val options = AppWidgetManager.getInstance(this@PaperStackRemoteViewsService)
                .getAppWidgetOptions(widgetId)
            val (width, height) = PaperStripWidgetProvider.sizeDp(this@PaperStackRemoteViewsService, options)
            val count = papers.size
            val showDots = count > 1 && height >= 56

            val horizontalPaddingDp = when {
                width < 120 -> 8
                width < 180 -> 12
                else -> 16
            }
            val verticalPaddingDp = when {
                height < 48 -> 2
                height < 72 -> 4
                height < 110 -> 6
                else -> 10
            }
            val baseTitleSize = when {
                width < 120 -> 11f
                width < 180 -> 13f
                width < 270 -> 15f
                width < 360 -> 17f
                else -> 19f
            }
            // Short strips also hold the dot row; taller widgets get a larger title and more lines.
            val titleSize = when {
                height < 72 -> minOf(baseTitleSize, 14f)
                height >= 150 -> baseTitleSize + 6f
                height >= 110 -> baseTitleSize + 2f
                else -> baseTitleSize
            }
            val dotsBlockDp = if (showDots) DOT_BLOCK_DP else 0
            val fittingLines = ((height - 2 * verticalPaddingDp - dotsBlockDp) / (titleSize * LINE_HEIGHT_FACTOR)).toInt()
            val layoutId = when {
                fittingLines >= 5 -> R.layout.widget_paper_card_5
                fittingLines >= 3 -> R.layout.widget_paper_card_3
                else -> R.layout.widget_paper_card
            }

            val views = RemoteViews(packageName, layoutId)
            views.setTextViewText(R.id.paper_title, paper.title)
            views.setTextViewTextSize(R.id.paper_title, TypedValue.COMPLEX_UNIT_SP, titleSize)

            // The byline is not shown, but stays in the accessibility description.
            val byline = listOf(paper.source, paper.authors, paper.published)
                .filter { it.isNotBlank() }
                .joinToString(" · ")

            val density = resources.displayMetrics.density
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                // StackView measures each card with AT_MOST, so without an explicit size the card
                // collapses to its text width (the "squished to the side" bug). The provider sizes the
                // stack to widget/0.9, so StackView's 90% child frame equals the widget; the card fills
                // that frame minus StackView's built-in 4dp frame padding on each side.
                val framePaddingPx = ceil(4f * density).toInt()
                val cardWidthDp = ((width * density).roundToInt() - 2 * framePaddingPx) / density
                val cardHeightDp = ((height * density).roundToInt() - 2 * framePaddingPx) / density
                views.setViewLayoutWidth(R.id.paper_card, cardWidthDp.coerceAtLeast(1f), TypedValue.COMPLEX_UNIT_DIP)
                views.setViewLayoutHeight(R.id.paper_card, cardHeightDp.coerceAtLeast(1f), TypedValue.COMPLEX_UNIT_DIP)
            }
            val horizontalPaddingPx = (horizontalPaddingDp * density).toInt()
            val verticalPaddingPx = (verticalPaddingDp * density).toInt()
            views.setViewPadding(
                R.id.paper_card,
                horizontalPaddingPx,
                verticalPaddingPx,
                horizontalPaddingPx,
                verticalPaddingPx
            )

            views.setViewVisibility(R.id.dot_row, if (showDots) View.VISIBLE else View.GONE)
            if (showDots) bindDots(views, position, count)

            val description = listOf(
                paper.title,
                byline,
                "Paper ${position + 1} of $count",
                "Open in Aftergleam"
            ).filter { it.isNotBlank() }.joinToString(". ") + "."
            views.setContentDescription(R.id.paper_card, description)
            views.setOnClickFillInIntent(
                R.id.paper_card,
                Intent().putExtra(AftergleamContract.EXTRA_PAPER_ID, paper.id)
            )
            return views
        }

        /**
         * Position meter. Up to [MAX_DOTS] papers get one dot each; with more papers the fixed row of dots
         * is a proportional meter (first dot = first paper, last dot = last paper), so the red dot never
         * claims a position it isn't at. The exact "Paper X of N" is in the accessibility description.
         */
        private fun bindDots(views: RemoteViews, position: Int, count: Int) {
            val visible = minOf(count, MAX_DOTS)
            val active = if (count <= MAX_DOTS) {
                position
            } else {
                Math.round(position * (visible - 1).toFloat() / (count - 1))
            }
            DOT_IDS.forEachIndexed { slot, viewId ->
                if (slot < visible) {
                    views.setViewVisibility(viewId, View.VISIBLE)
                    views.setImageViewResource(
                        viewId,
                        if (slot == active) R.drawable.nothing_dot_red else R.drawable.nothing_dot_off
                    )
                } else {
                    views.setViewVisibility(viewId, View.GONE)
                }
            }
        }

        override fun getLoadingView(): RemoteViews? = null
        override fun getViewTypeCount() = 3
        override fun getItemId(position: Int) = papers.getOrNull(position)?.id?.hashCode()?.toLong() ?: position.toLong()
        override fun hasStableIds() = true

        override fun onDestroy() {
            papers = emptyList()
        }
    }

    private companion object {
        const val DOT_BLOCK_DP = 14 // 6dp dot + 8dp margin
        const val LINE_HEIGHT_FACTOR = 1.3f
        const val MAX_DOTS = 7
        val DOT_IDS = intArrayOf(
            R.id.dot_0, R.id.dot_1, R.id.dot_2, R.id.dot_3, R.id.dot_4, R.id.dot_5, R.id.dot_6
        )
    }
}
