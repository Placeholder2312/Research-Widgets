package com.placeholder2312.researchwidgets.data
import android.content.Context
import android.database.MatrixCursor
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[34])
class PaperProviderClientTest {
 private val context=ApplicationProvider.getApplicationContext<Context>()

 @Test fun mapsRecommendedAndSavedProviderRows() {
  val cursor=MatrixCursor(AftergleamContract.COLUMNS)
  cursor.addRow(arrayOf<Any?>("paper-a","Recommended paper","Ada Lovelace, Grace Hopper","arXiv","2026-11-12",0,"recommended"))
  cursor.addRow(arrayOf<Any?>("paper-b","Saved paper","Katherine Johnson","bioRxiv","2026-11-11",1,"saved"))
  val papers=cursor.use { PaperProviderClient.parseRows(it) }
  assertEquals(2,papers.size)
  assertEquals("paper-a",papers[0].id)
  assertEquals("recommended",papers[0].feedType)
  assertEquals("paper-b",papers[1].id)
  assertEquals("Katherine Johnson",papers[1].authors)
  assertEquals("saved",papers[1].feedType)
 }

 @Test fun emptyProviderCursorMapsToEmptyFeed() {
  val cursor=MatrixCursor(AftergleamContract.COLUMNS)
  assertTrue(cursor.use { PaperProviderClient.parseRows(it) }.isEmpty())
 }

 @Test fun providerResolutionFailuresReturnStateInsteadOfThrowing() {
  val result=PaperProviderClient.read(context,FeedMode.RECOMMENDED)
  assertTrue(result.state==ProviderState.NOT_INSTALLED || result.state==ProviderState.UNAVAILABLE)
  assertTrue(result.message.isNotBlank())
 }

 @Test fun unknownStoredModeDefaultsToRecommended() {
  assertEquals(FeedMode.RECOMMENDED,FeedMode.from("invalid"))
  assertEquals(FeedMode.SAVED,FeedMode.from("saved"))
 }
}
