package com.placeholder2312.researchwidgets.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.widget.RemoteViews
import com.placeholder2312.researchwidgets.R
import com.placeholder2312.researchwidgets.data.AftergleamContract
import com.placeholder2312.researchwidgets.data.PaperProviderClient
import com.placeholder2312.researchwidgets.data.WidgetPaper
import com.placeholder2312.researchwidgets.data.WidgetPreferences
import com.placeholder2312.researchwidgets.data.WidgetStyle

/**
 * One paper title fills the widget.
 *
 * Tapping the title opens that paper in Aftergleam. Tapping the right edge, the dots or the counter shows
 * the next paper (wrapping around).
 *
 * This is deliberately a plain widget, not a collection (StackView/ListView). Collections can only handle
 * taps through a template PendingIntent plus per-item fill-in intents, and that path never delivered taps
 * on the target device, while plain click PendingIntents (the tiles) do.
 */
class TapPaperWidgetProvider : AppWidgetProvider() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_NEXT) {
            val id = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
            advance(context, id)
            return
        }
        super.onReceive(context, intent)
    }

    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        ids.forEach { updateWidget(context, manager, it) }
    }

    override fun onAppWidgetOptionsChanged(context: Context, manager: AppWidgetManager, id: Int, options: Bundle) {
        updateWidget(context, manager, id, fresh = false)
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
            WidgetPreferences.setIndex(context, newId, WidgetPreferences.index(context, oldId))
            WidgetPreferences.clear(context, oldId)
        }
        refreshAll(context)
    }

    companion object {
        const val ACTION_NEXT = "com.placeholder2312.researchwidgets.action.NEXT_PLAIN_PAPER"

        private const val OPEN_REQUEST_BASE = 20000
        private const val NEXT_REQUEST_BASE = 50000

        private const val DOT_BLOCK_DP = 14 // 6dp dot + 8dp margin
        private const val LINE_HEIGHT_FACTOR = 1.3f
        private const val MAX_DOTS = 7
        private const val RAIL_GAP_DP = 4
        private const val RAIL_DOT_STEP_DP = 9 // 5dp dot + 4dp gap
        private const val RAIL_CORNER_CLEARANCE_DP = 12 // keeps the rail off the rounded corners
        private const val COUNTER_BLOCK_DP = 14 // 10sp mono line + 4dp margin
        private const val NEXT_ZONE_MIN_DP = 32f
        private const val NEXT_ZONE_MAX_DP = 56f

        private val DOT_IDS = intArrayOf(
            R.id.dot_0, R.id.dot_1, R.id.dot_2, R.id.dot_3, R.id.dot_4, R.id.dot_5, R.id.dot_6
        )

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
            manager.getAppWidgetIds(ComponentName(context, TapPaperWidgetProvider::class.java))
                .forEach { updateWidget(context, manager, it) }
        }

        fun updateWidget(context: Context, id: Int) {
            updateWidget(context, AppWidgetManager.getInstance(context), id)
        }

        /** Shows the next paper of one widget instance, wrapping to the first after the last. */
        private fun advance(context: Context, id: Int) {
            if (id == AppWidgetManager.INVALID_APPWIDGET_ID) return
            val manager = AppWidgetManager.getInstance(context)
            // Only ever touch widgets that really belong to this provider.
            val ours = manager.getAppWidgetIds(ComponentName(context, TapPaperWidgetProvider::class.java))
            if (id !in ours) return
            val count = PaperProviderClient.recent(context, WidgetPreferences.mode(context, id)).papers.size
            if (count > 1) {
                WidgetPreferences.setIndex(context, id, (WidgetPreferences.index(context, id) + 1) % count)
            }
            updateWidget(context, manager, id, fresh = false)
        }

        private fun updateWidget(context: Context, manager: AppWidgetManager, id: Int, fresh: Boolean = true) {
            if (id == AppWidgetManager.INVALID_APPWIDGET_ID) return

            val mode = WidgetPreferences.mode(context, id)
            val result = if (fresh) PaperProviderClient.read(context, mode) else PaperProviderClient.recent(context, mode)
            val (width, height) = sizeDp(context, manager.getAppWidgetOptions(id))

            val views = if (!result.connected || result.papers.isEmpty()) {
                emptyViews(context, id, result.message, width, height)
            } else {
                paperViews(context, id, result.papers, WidgetPreferences.style(context, id), width, height)
            }
            manager.updateAppWidget(id, views)
        }

        private fun emptyViews(context: Context, id: Int, message: String, width: Int, height: Int): RemoteViews {
            val views = RemoteViews(context.packageName, R.layout.widget_plain_paper_strip)
            views.setTextViewText(R.id.empty_text, message.uppercase())
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
            // Tapping the empty state opens Aftergleam itself.
            val open = PendingIntents.activity(
                context, OPEN_REQUEST_BASE + id, Intent(context, OpenInAftergleamActivity::class.java)
            )
            views.setOnClickPendingIntent(R.id.empty_state, open)
            views.setContentDescription(R.id.empty_state, "${message}. Open Aftergleam.")
            return views
        }

        private fun paperViews(
            context: Context,
            id: Int,
            papers: List<WidgetPaper>,
            style: WidgetStyle,
            width: Int,
            height: Int
        ): RemoteViews {
            val count = papers.size
            val position = WidgetPreferences.index(context, id).takeIf { it in 0 until count } ?: 0
            val paper = papers[position]
            val rail = style == WidgetStyle.RAIL

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
            // ROW: dots (and, past 7 papers, a counter) sit under the title and cost vertical space.
            // RAIL: dots run down the right edge and cost horizontal space; a counter sits above the title.
            val showDots = count > 1 && if (rail) height >= 40 else height >= 56
            val showCounter = count > 1 && if (rail) height >= 72 else (showDots && count > MAX_DOTS)
            val railDots = if (rail) {
                ((height - 2 * verticalPaddingDp - RAIL_CORNER_CLEARANCE_DP + RAIL_GAP_DP) / RAIL_DOT_STEP_DP).coerceIn(0, MAX_DOTS)
            } else {
                MAX_DOTS
            }
            val dotsBlockDp = if (!rail && showDots) DOT_BLOCK_DP else 0
            val counterBlockDp = if (rail && showCounter) COUNTER_BLOCK_DP else 0
            val fittingLines =
                ((height - 2 * verticalPaddingDp - dotsBlockDp - counterBlockDp) / (titleSize * LINE_HEIGHT_FACTOR)).toInt()
            val layoutId = when {
                fittingLines >= 5 -> if (rail) R.layout.widget_plain_paper_rail_5 else R.layout.widget_plain_paper_card_5
                fittingLines >= 3 -> if (rail) R.layout.widget_plain_paper_rail_3 else R.layout.widget_plain_paper_card_3
                else -> if (rail) R.layout.widget_plain_paper_rail else R.layout.widget_plain_paper_card
            }

            val views = RemoteViews(context.packageName, layoutId)
            views.setTextViewText(R.id.paper_title, paper.title)
            views.setTextViewTextSize(R.id.paper_title, TypedValue.COMPLEX_UNIT_SP, titleSize)

            val density = context.resources.displayMetrics.density
            val horizontalPaddingPx = (horizontalPaddingDp * density).toInt()
            val verticalPaddingPx = (verticalPaddingDp * density).toInt()
            views.setViewPadding(
                R.id.paper_card,
                horizontalPaddingPx,
                verticalPaddingPx,
                horizontalPaddingPx,
                verticalPaddingPx
            )

            val dotCount = if (rail) minOf(count, railDots) else minOf(count, MAX_DOTS)
            val dotsVisible = showDots && dotCount >= 2
            views.setViewVisibility(R.id.dot_row, if (dotsVisible) View.VISIBLE else View.GONE)
            if (dotsVisible) bindDots(views, position, count, dotCount)
            views.setViewVisibility(R.id.paper_counter, if (showCounter) View.VISIBLE else View.GONE)
            if (showCounter) views.setTextViewText(R.id.paper_counter, "%02d/%02d".format(position + 1, count))

            // The byline is not shown, but stays in the accessibility description.
            val byline = listOf(paper.source, paper.authors, paper.published)
                .filter { it.isNotBlank() }
                .joinToString(" · ")
            val description = listOf(
                paper.title,
                byline,
                "Paper ${position + 1} of $count",
                "Open in Aftergleam"
            ).filter { it.isNotBlank() }.joinToString(". ") + "."
            views.setContentDescription(R.id.paper_card, description)

            // Tap the card: open this paper. A plain click PendingIntent, the same mechanism the tiles use.
            val open = PendingIntents.activity(
                context,
                OPEN_REQUEST_BASE + id,
                Intent(context, OpenInAftergleamActivity::class.java)
                    .putExtra(AftergleamContract.EXTRA_PAPER_ID, paper.id)
            )
            views.setOnClickPendingIntent(R.id.paper_card, open)

            // Tap the right edge, the dots or the counter: next paper.
            if (count > 1) {
                val next = nextPendingIntent(context, id)
                views.setViewVisibility(R.id.next_zone, View.VISIBLE)
                views.setOnClickPendingIntent(R.id.next_zone, next)
                views.setOnClickPendingIntent(R.id.dot_row, next)
                views.setOnClickPendingIntent(R.id.paper_counter, next)
                views.setContentDescription(R.id.next_zone, "Next paper")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    views.setViewLayoutWidth(
                        R.id.next_zone,
                        (width / 5f).coerceIn(NEXT_ZONE_MIN_DP, NEXT_ZONE_MAX_DP),
                        TypedValue.COMPLEX_UNIT_DIP
                    )
                }
            } else {
                views.setViewVisibility(R.id.next_zone, View.GONE)
            }
            return views
        }

        private fun nextPendingIntent(context: Context, id: Int): PendingIntent {
            val intent = Intent(context, TapPaperWidgetProvider::class.java)
                .setAction(ACTION_NEXT)
                .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id)
            return PendingIntent.getBroadcast(
                context,
                NEXT_REQUEST_BASE + id,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        /**
         * Position meter. Up to [MAX_DOTS] papers get one dot each; with more papers the fixed set of dots
         * (a row, or a vertical rail) is a proportional meter (first dot = first paper, last dot = last paper), so the
         * red dot never claims a position it isn't at. The exact "Paper X of N" is in the accessibility description.
         */
        private fun bindDots(views: RemoteViews, position: Int, count: Int, visible: Int) {
            val active = if (count <= visible) {
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
    }
}
