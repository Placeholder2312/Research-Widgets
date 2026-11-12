package com.placeholder2312.researchwidgets.widget
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.RemoteViews
import com.placeholder2312.researchwidgets.R
import com.placeholder2312.researchwidgets.data.AftergleamContract
import com.placeholder2312.researchwidgets.data.PaperProviderClient
import com.placeholder2312.researchwidgets.data.WidgetPreferences
class PaperStripWidgetProvider:AppWidgetProvider(){
 override fun onUpdate(c:Context,m:AppWidgetManager,ids:IntArray){ids.forEach{updateWidget(c,m,it)}}
 override fun onAppWidgetOptionsChanged(c:Context,m:AppWidgetManager,id:Int,options:Bundle){updateWidget(c,m,id)}
 override fun onDeleted(c:Context,ids:IntArray){ids.forEach{WidgetPreferences.clear(c,it)}}
 override fun onRestored(c:Context,oldIds:IntArray,newIds:IntArray){
  for(i in 0 until minOf(oldIds.size,newIds.size)){
   val oldId=oldIds[i];val newId=newIds[i];val mode=WidgetPreferences.mode(c,oldId);val index=WidgetPreferences.index(c,oldId)
   WidgetPreferences.setMode(c,newId,mode);WidgetPreferences.setIndex(c,newId,index);WidgetPreferences.clear(c,oldId)
  }
  refreshAll(c)
 }
 override fun onReceive(c:Context,intent:Intent){when(intent.action){ACTION_NEXT->move(c,intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,AppWidgetManager.INVALID_APPWIDGET_ID),1);ACTION_PREVIOUS->move(c,intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,AppWidgetManager.INVALID_APPWIDGET_ID),-1);else->super.onReceive(c,intent)}}
 private fun move(c:Context,id:Int,delta:Int){if(id==AppWidgetManager.INVALID_APPWIDGET_ID)return;val count=PaperProviderClient.read(c,WidgetPreferences.mode(c,id)).papers.size;if(count==0)return;WidgetPreferences.setIndex(c,id,(WidgetPreferences.index(c,id)+delta+count)%count);updateWidget(c,AppWidgetManager.getInstance(c),id)}
 companion object{
  private const val ACTION_NEXT="com.placeholder2312.researchwidgets.action.PAPER_NEXT";private const val ACTION_PREVIOUS="com.placeholder2312.researchwidgets.action.PAPER_PREVIOUS"
  fun refreshAll(c:Context){val m=AppWidgetManager.getInstance(c);m.getAppWidgetIds(ComponentName(c,PaperStripWidgetProvider::class.java)).forEach{updateWidget(c,m,it)}}
  fun updateWidget(c:Context,id:Int)=updateWidget(c,AppWidgetManager.getInstance(c),id)
  private fun updateWidget(c:Context,m:AppWidgetManager,id:Int){
   if(id==AppWidgetManager.INVALID_APPWIDGET_ID)return
   val mode=WidgetPreferences.mode(c,id);val result=PaperProviderClient.read(c,mode);val v=RemoteViews(c.packageName,R.layout.widget_paper_strip)
   v.setTextViewText(R.id.mode_label,mode.label);v.setTextViewText(R.id.empty_text,if(!result.connected||result.papers.isEmpty())result.message else "")
   val service=Intent(c,PaperStackRemoteViewsService::class.java).apply{putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,id);data=Uri.parse("researchwidgets://paper-strip/$id")}
   v.setRemoteAdapter(R.id.paper_stack,service);v.setEmptyView(R.id.paper_stack,R.id.empty_text)
   val open=Intent(AftergleamContract.ACTION_OPEN_PAPER).setComponent(ComponentName(AftergleamContract.PACKAGE,AftergleamContract.PAPER_OPEN_COMPONENT))
   val mutable=if(Build.VERSION.SDK_INT>=31)PendingIntent.FLAG_MUTABLE else 0
   v.setPendingIntentTemplate(R.id.paper_stack,PendingIntent.getActivity(c,10000+id,open,PendingIntent.FLAG_UPDATE_CURRENT or mutable))
   v.setOnClickPendingIntent(R.id.previous_button,movePending(c,id,ACTION_PREVIOUS));v.setOnClickPendingIntent(R.id.next_button,movePending(c,id,ACTION_NEXT))
   val count=result.papers.size
   val selected=if(count==0)0 else WidgetPreferences.index(c,id)%count
   if(selected!=WidgetPreferences.index(c,id))WidgetPreferences.setIndex(c,id,selected)
   v.setDisplayedChild(R.id.paper_stack,selected)
   val width=m.getAppWidgetOptions(id).getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH,280);v.setTextViewTextSize(R.id.mode_label,android.util.TypedValue.COMPLEX_UNIT_SP,if(width<270)9f else 10f)
   m.updateAppWidget(id,v);m.notifyAppWidgetViewDataChanged(id,R.id.paper_stack)
  }
  private fun movePending(c:Context,id:Int,action:String):PendingIntent{val intent=Intent(c,PaperStripWidgetProvider::class.java).setAction(action).putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,id);return PendingIntent.getBroadcast(c,id*2+if(action==ACTION_NEXT)1 else 0,intent,PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)}
 }
}
