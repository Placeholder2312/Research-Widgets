package com.placeholder2312.researchwidgets.widget
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.util.TypedValue
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.placeholder2312.researchwidgets.R
import com.placeholder2312.researchwidgets.data.AftergleamContract
import com.placeholder2312.researchwidgets.data.PaperProviderClient
import com.placeholder2312.researchwidgets.data.WidgetPreferences
class PaperStackRemoteViewsService:RemoteViewsService(){
 override fun onGetViewFactory(intent:Intent):RemoteViewsFactory=PaperFactory(intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,AppWidgetManager.INVALID_APPWIDGET_ID))
 private inner class PaperFactory(private val widgetId:Int):RemoteViewsFactory{
  private var papers=emptyList<com.placeholder2312.researchwidgets.data.WidgetPaper>()
  override fun onCreate(){reload()}
  override fun onDataSetChanged(){reload()}
  private fun reload(){papers=if(widgetId==AppWidgetManager.INVALID_APPWIDGET_ID)emptyList() else PaperProviderClient.read(this@PaperStackRemoteViewsService,WidgetPreferences.mode(this@PaperStackRemoteViewsService,widgetId)).papers}
  override fun getCount()=papers.size
  override fun getViewAt(position:Int):RemoteViews?{
   val p=papers.getOrNull(position)?:return null
   val v=RemoteViews(packageName,R.layout.widget_paper_card)
   v.setTextViewText(R.id.paper_title,p.title)
   val byline=listOf(p.source,p.authors,p.published).filter{it.isNotBlank()}.joinToString(" · ")
   v.setTextViewText(R.id.paper_byline,byline)
   val opts=AppWidgetManager.getInstance(this@PaperStackRemoteViewsService).getAppWidgetOptions(widgetId)
   val width=opts.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH,280);val height=opts.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT,64)
   v.setTextViewTextSize(R.id.paper_title,TypedValue.COMPLEX_UNIT_SP,if(width>=360&&height>=90)18f else if(width<270)14f else 16f)
   v.setTextViewTextSize(R.id.paper_byline,TypedValue.COMPLEX_UNIT_SP,if(height<64)8f else 10f)
   v.setContentDescription(R.id.paper_card,"${p.title}. $byline. Open in Aftergleam.")
   v.setOnClickFillInIntent(R.id.paper_card,Intent().putExtra(AftergleamContract.EXTRA_PAPER_ID,p.id))
   return v
  }
  override fun getLoadingView():RemoteViews?=null
  override fun getViewTypeCount()=1
  override fun getItemId(position:Int)=papers.getOrNull(position)?.id?.hashCode()?.toLong()?:position.toLong()
  override fun hasStableIds()=true
  override fun onDestroy(){papers=emptyList()}
 }
}
