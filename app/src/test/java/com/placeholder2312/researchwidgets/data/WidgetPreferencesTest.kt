package com.placeholder2312.researchwidgets.data
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
@RunWith(RobolectricTestRunner::class)
@Config(sdk=[34])
class WidgetPreferencesTest{
 private val context=ApplicationProvider.getApplicationContext<Context>()
 @Test fun feedModeIsStoredPerWidgetInstance(){context.getSharedPreferences("widget_instances",Context.MODE_PRIVATE).edit().clear().commit();WidgetPreferences.setMode(context,41,FeedMode.SAVED);WidgetPreferences.setMode(context,42,FeedMode.RECOMMENDED);assertEquals(FeedMode.SAVED,WidgetPreferences.mode(context,41));assertEquals(FeedMode.RECOMMENDED,WidgetPreferences.mode(context,42))}
 @Test fun newWidgetDefaultsToRecommended(){assertEquals(FeedMode.RECOMMENDED,WidgetPreferences.mode(context,9876))}
}
