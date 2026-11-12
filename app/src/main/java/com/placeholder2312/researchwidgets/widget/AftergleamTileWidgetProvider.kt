package com.placeholder2312.researchwidgets.widget
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.placeholder2312.researchwidgets.R
import com.placeholder2312.researchwidgets.data.AftergleamContract
/** Static offline 2×2 tile using the original Aftergleam launcher artwork. */
class AftergleamTileWidgetProvider:AppWidgetProvider(){
 override fun onUpdate(c:Context,m:AppWidgetManager,ids:IntArray){ids.forEach{id->val v=RemoteViews(c.packageName,R.layout.widget_aftergleam_tile);val launch=Intent().setComponent(ComponentName(AftergleamContract.PACKAGE,AftergleamContract.PACKAGE+".MainActivity"));val pi=PendingIntent.getActivity(c,20000+id,launch,PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE);v.setOnClickPendingIntent(R.id.aftergleam_tile,pi);v.setContentDescription(R.id.aftergleam_tile,"Open Aftergleam");m.updateAppWidget(id,v)}}
}
