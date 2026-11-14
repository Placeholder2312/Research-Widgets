package com.placeholder2312.researchwidgets.data
import android.content.Context
/** Per-widget settings: two widget instances can select different feeds. */
object WidgetPreferences {
 private const val PREFS="widget_instances"
 private fun p(c:Context)=c.getSharedPreferences(PREFS,Context.MODE_PRIVATE)
 private fun mk(id:Int)="mode_$id";private fun sk(id:Int)="style_$id"
 fun mode(c:Context,id:Int)=FeedMode.from(p(c).getString(mk(id),FeedMode.RECOMMENDED.key))
 fun setMode(c:Context,id:Int,m:FeedMode){p(c).edit().putString(mk(id),m.key).apply()}
 fun style(c:Context,id:Int)=WidgetStyle.from(p(c).getString(sk(id),null))
 fun setStyle(c:Context,id:Int,s:WidgetStyle){p(c).edit().putString(sk(id),s.key).apply()}
 fun clear(c:Context,id:Int){p(c).edit().remove(mk(id)).remove(sk(id)).apply()}
}
