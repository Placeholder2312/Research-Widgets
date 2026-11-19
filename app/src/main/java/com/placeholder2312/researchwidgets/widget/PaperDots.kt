package com.placeholder2312.researchwidgets.widget

import android.view.View
import android.widget.RemoteViews
import com.placeholder2312.researchwidgets.R

/** The position dots shared by both strips (Bottom row and Side rail). */
internal object PaperDots {
    const val MAX_DOTS = 7

    private val DOT_IDS = intArrayOf(
        R.id.dot_0, R.id.dot_1, R.id.dot_2, R.id.dot_3, R.id.dot_4, R.id.dot_5, R.id.dot_6
    )

    /**
     * Which of the [visible] dots is red for the paper at [position] of [count]. The first paper is the first dot
     * (leftmost in a row, top of a rail) and the last paper is the last dot (rightmost, bottom). Up to [visible]
     * papers get one dot each; with more papers the dots are a proportional meter.
     */
    fun litSlot(position: Int, count: Int, visible: Int): Int {
        val active = if (count <= visible) {
            position
        } else {
            Math.round(position * (visible - 1).toFloat() / (count - 1))
        }
        return active
    }

    fun bind(views: RemoteViews, position: Int, count: Int, visible: Int) {
        val lit = litSlot(position, count, visible)
        DOT_IDS.forEachIndexed { slot, viewId ->
            if (slot < visible) {
                views.setViewVisibility(viewId, View.VISIBLE)
                views.setImageViewResource(
                    viewId,
                    if (slot == lit) R.drawable.nothing_dot_red else R.drawable.nothing_dot_off
                )
            } else {
                views.setViewVisibility(viewId, View.GONE)
            }
        }
    }
}
