package com.placeholder2312.researchwidgets.data

import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Regression tests pin the companion IPC names to Aftergleam-For-Widgets' v1 contract. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AftergleamContractTest {
    @Test
    fun providerContractMatchesAftergleamForkV1() {
        assertEquals("si.jakobkreft.aftergleam", AftergleamContract.PACKAGE)
        assertEquals("si.jakobkreft.aftergleam.researchwidgets", AftergleamContract.AUTHORITY)
        assertEquals("si.jakobkreft.aftergleam.permission.READ_RESEARCH_WIDGETS", AftergleamContract.READ_PERMISSION)
        assertEquals(Uri.parse("content://si.jakobkreft.aftergleam.researchwidgets/v1/recommended"), AftergleamContract.RECOMMENDED_URI)
        assertEquals(Uri.parse("content://si.jakobkreft.aftergleam.researchwidgets/v1/saved"), AftergleamContract.SAVED_URI)
        assertArrayEquals(
            arrayOf("paper_id", "title", "authors", "source", "published", "feed_position", "feed_type"),
            AftergleamContract.COLUMNS
        )
        assertEquals("si.jakobkreft.aftergleam.action.RESEARCH_WIDGETS_DATA_CHANGED", AftergleamContract.ACTION_DATA_CHANGED)
    }

    @Test
    fun optionalDownloadedFeedUsesTheExtensionUri() {
        assertEquals(Uri.parse("content://si.jakobkreft.aftergleam.researchwidgets/v1/downloaded"), AftergleamContract.DOWNLOADED_URI)
    }

    @Test
    fun paperOpenIntentMatchesForkComponentActionAndExtra() {
        val intent = AftergleamLinks.openPaper("fixture-paper-id")
        assertEquals("si.jakobkreft.aftergleam.action.OPEN_WIDGET_PAPER", intent.action)
        assertEquals(
            ComponentName("si.jakobkreft.aftergleam", "si.jakobkreft.aftergleam.integration.PaperOpenActivity"),
            intent.component
        )
        assertEquals("fixture-paper-id", intent.getStringExtra("si.jakobkreft.aftergleam.extra.WIDGET_PAPER_ID"))
        assertTrue((intent.flags and Intent.FLAG_ACTIVITY_NEW_TASK) != 0)
    }

    @Test
    fun launchIntentTargetsForkLauncherActivity() {
        val intent = AftergleamLinks.launchApp()
        assertEquals(Intent.ACTION_MAIN, intent.action)
        assertTrue(intent.hasCategory(Intent.CATEGORY_LAUNCHER))
        assertEquals(
            ComponentName("si.jakobkreft.aftergleam", "si.jakobkreft.aftergleam.MainActivity"),
            intent.component
        )
    }
}
