package com.placeholder2312.researchwidgets.data

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.placeholder2312.researchwidgets.widget.PaperStripWidgetProvider
import com.placeholder2312.researchwidgets.widget.TapPaperWidgetProvider
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class WidgetPreferencesTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Before
    fun clearPrefs() {
        context.getSharedPreferences("widget_instances", Context.MODE_PRIVATE).edit().clear().commit()
    }

    @Test
    fun feedModeIsStoredPerWidgetInstance() {
        WidgetPreferences.setMode(context, 41, FeedMode.SAVED)
        WidgetPreferences.setMode(context, 42, FeedMode.RECOMMENDED)
        assertEquals(FeedMode.SAVED, WidgetPreferences.mode(context, 41))
        assertEquals(FeedMode.RECOMMENDED, WidgetPreferences.mode(context, 42))
    }

    @Test
    fun newWidgetDefaultsToRecommendedRow() {
        assertEquals(FeedMode.RECOMMENDED, WidgetPreferences.mode(context, 9876))
        assertEquals(WidgetStyle.ROW, WidgetPreferences.style(context, 9876))
    }

    @Test
    fun styleIsStoredPerWidgetInstanceAndClearedWithIt() {
        WidgetPreferences.setStyle(context, 61, WidgetStyle.RAIL)
        assertEquals(WidgetStyle.RAIL, WidgetPreferences.style(context, 61))
        assertEquals(WidgetStyle.ROW, WidgetPreferences.style(context, 62))
        WidgetPreferences.clear(context, 61)
        assertEquals(WidgetStyle.ROW, WidgetPreferences.style(context, 61))
    }

    @Test
    fun plainWidgetIndexIsStoredPerInstanceAndClearedWithIt() {
        assertEquals(0, WidgetPreferences.index(context, 81))
        WidgetPreferences.setIndex(context, 81, 4)
        WidgetPreferences.setIndex(context, 82, 2)
        assertEquals(4, WidgetPreferences.index(context, 81))
        assertEquals(2, WidgetPreferences.index(context, 82))
        WidgetPreferences.clear(context, 81)
        assertEquals(0, WidgetPreferences.index(context, 81))
        assertEquals(2, WidgetPreferences.index(context, 82))
    }

    @Test
    fun plainWidgetRestoreKeepsItsFeedStyleAndPosition() {
        WidgetPreferences.setMode(context, 91, FeedMode.SAVED)
        WidgetPreferences.setStyle(context, 91, WidgetStyle.RAIL)
        WidgetPreferences.setIndex(context, 91, 3)
        TapPaperWidgetProvider().onRestored(context, intArrayOf(91), intArrayOf(191))
        assertEquals(FeedMode.SAVED, WidgetPreferences.mode(context, 191))
        assertEquals(WidgetStyle.RAIL, WidgetPreferences.style(context, 191))
        assertEquals(3, WidgetPreferences.index(context, 191))
    }

    @Test
    fun digestModeIsStoredPerWidgetInstance() {
        assertEquals(DigestMode.LATEST, WidgetPreferences.digestMode(context, 71))
        WidgetPreferences.setDigestMode(context, 71, DigestMode.DOWNLOADED)
        assertEquals(DigestMode.DOWNLOADED, WidgetPreferences.digestMode(context, 71))
        assertEquals(DigestMode.LATEST, WidgetPreferences.digestMode(context, 72))
        WidgetPreferences.clear(context, 71)
        assertEquals(DigestMode.LATEST, WidgetPreferences.digestMode(context, 71))
    }

    @Test
    fun restoredWidgetIdsKeepTheirOwnModeAndStyle() {
        WidgetPreferences.setMode(context, 51, FeedMode.SAVED)
        WidgetPreferences.setStyle(context, 51, WidgetStyle.RAIL)
        PaperStripWidgetProvider().onRestored(context, intArrayOf(51), intArrayOf(151))
        assertEquals(FeedMode.SAVED, WidgetPreferences.mode(context, 151))
        assertEquals(WidgetStyle.RAIL, WidgetPreferences.style(context, 151))
    }
}
