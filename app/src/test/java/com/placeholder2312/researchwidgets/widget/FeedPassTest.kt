package com.placeholder2312.researchwidgets.widget

import com.placeholder2312.researchwidgets.data.FeedMode
import com.placeholder2312.researchwidgets.data.FeedRead
import com.placeholder2312.researchwidgets.data.ProviderState
import org.junit.Assert.assertEquals
import org.junit.Test

class FeedPassTest {
    private val read = FeedRead(ProviderState.CONNECTED, "ok")
    private var fresh = 0
    private var recent = 0

    private fun pass(fresh: Boolean) = FeedPass(
        fresh,
        { this.fresh++; read },
        { this.recent++; read }
    )

    @Test
    fun freshPassQueriesEachFeedOnceAndReusesItAfterwards() {
        val pass = pass(fresh = true)
        repeat(3) { pass.read(FeedMode.RECOMMENDED) }
        repeat(2) { pass.read(FeedMode.SAVED) }
        assertEquals(2, fresh)
        assertEquals(3, recent)
    }

    @Test
    fun nonFreshPassNeverForcesAQuery() {
        val pass = pass(fresh = false)
        repeat(4) { pass.read(FeedMode.RECOMMENDED) }
        assertEquals(0, fresh)
        assertEquals(4, recent)
    }

    @Test
    fun eachPassStartsClean() {
        pass(fresh = true).read(FeedMode.SAVED)
        pass(fresh = true).read(FeedMode.SAVED)
        assertEquals(2, fresh)
    }
}
