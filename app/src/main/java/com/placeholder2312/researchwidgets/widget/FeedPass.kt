package com.placeholder2312.researchwidgets.widget

import android.content.Context
import com.placeholder2312.researchwidgets.data.FeedMode
import com.placeholder2312.researchwidgets.data.FeedRead
import com.placeholder2312.researchwidgets.data.PaperProviderClient

/**
 * One refresh pass over the widgets. With [fresh] set, each feed is queried from the provider at most once per pass
 * and every other widget in the pass reuses that result; without it every read may come from the short-lived cache.
 * Used so one data-changed broadcast does not query the same feed once per widget, and so resizing a widget
 * (which fires many option-change callbacks) does not hit the provider each time.
 */
internal class FeedPass(
    private val fresh: Boolean,
    private val readFresh: (FeedMode) -> FeedRead,
    private val readRecent: (FeedMode) -> FeedRead
) {
    constructor(context: Context, fresh: Boolean = true) : this(
        fresh,
        { mode -> PaperProviderClient.read(context, mode) },
        { mode -> PaperProviderClient.recent(context, mode) }
    )

    private val queried = HashSet<FeedMode>()

    fun read(mode: FeedMode): FeedRead =
        if (fresh && queried.add(mode)) readFresh(mode) else readRecent(mode)
}
