package com.placeholder2312.researchwidgets.data

import android.net.Uri

/** Names that make up the signature-protected provider API (v1) exposed by the Aftergleam fork. */
object AftergleamContract {
    const val PACKAGE = "si.jakobkreft.aftergleam"
    const val AUTHORITY = "si.jakobkreft.aftergleam.researchwidgets"
    const val READ_PERMISSION = "si.jakobkreft.aftergleam.permission.READ_RESEARCH_WIDGETS"
    const val PAPER_OPEN_COMPONENT = "si.jakobkreft.aftergleam.integration.PaperOpenActivity"
    const val ACTION_OPEN_PAPER = "si.jakobkreft.aftergleam.action.OPEN_WIDGET_PAPER"
    const val EXTRA_PAPER_ID = "si.jakobkreft.aftergleam.extra.WIDGET_PAPER_ID"
    const val ACTION_DATA_CHANGED = "si.jakobkreft.aftergleam.action.RESEARCH_WIDGETS_DATA_CHANGED"

    const val COLUMN_PAPER_ID = "paper_id"
    const val COLUMN_TITLE = "title"
    const val COLUMN_AUTHORS = "authors"
    const val COLUMN_SOURCE = "source"
    const val COLUMN_PUBLISHED = "published"
    const val COLUMN_FEED_POSITION = "feed_position"
    const val COLUMN_FEED_TYPE = "feed_type"

    val COLUMNS = arrayOf(
        COLUMN_PAPER_ID, COLUMN_TITLE, COLUMN_AUTHORS, COLUMN_SOURCE,
        COLUMN_PUBLISHED, COLUMN_FEED_POSITION, COLUMN_FEED_TYPE
    )

    val RECOMMENDED_URI: Uri = Uri.parse("content://$AUTHORITY/v1/recommended")
    val SAVED_URI: Uri = Uri.parse("content://$AUTHORITY/v1/saved")
}
