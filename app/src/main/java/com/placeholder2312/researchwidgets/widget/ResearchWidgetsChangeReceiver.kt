package com.placeholder2312.researchwidgets.widget
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
/** Protected invalidation receiver; the companion re-queries the read-only provider. */
class ResearchWidgetsChangeReceiver:BroadcastReceiver(){
 override fun onReceive(c:Context,i:Intent){if(i.action=="si.jakobkreft.aftergleam.action.RESEARCH_WIDGETS_DATA_CHANGED")PaperStripWidgetProvider.refreshAll(c)}
}
