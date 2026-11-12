package com.placeholder2312.researchwidgets.data
import android.content.Context
/** Per-widget settings: two widget instances can select different feeds. */
object WidgetPreferences {
 private const val PREFS="widget_instances"
 private fun p(c:Context)=c.getSharedPreferences(PREFS,Context.MODE_PRIVATE)
 private fun mk(id:Int)="mode_$id";private fun ik(id:Int)="index_$id"
 fun mode(c:Context,id:Int)=FeedMode.from(p(c).getString(mk(id),FeedMode.RECOMMENDED.key))
 fun setMode(c:Context,id:Int,m:FeedMode){p(c).edit().putString(mk(id),m.key).putInt(ik(id),0).apply()}
 fun index(c:Context,id:Int)=p(c).getInt(ik(id),0).coerceAtLeast(0)
 fun setIndex(c:Context,id:Int,v:Int){p(c).edit().putInt(ik(id),v.coerceAtLeast(0)).apply()}
 fun clear(c:Context,id:Int){p(c).edit().remove(mk(id)).remove(ik(id)).apply()}
}
