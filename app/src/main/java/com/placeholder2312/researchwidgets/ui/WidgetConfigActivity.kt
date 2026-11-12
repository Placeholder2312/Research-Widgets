package com.placeholder2312.researchwidgets.ui
import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import com.placeholder2312.researchwidgets.data.FeedMode
import com.placeholder2312.researchwidgets.data.PaperProviderClient
import com.placeholder2312.researchwidgets.data.WidgetPreferences
import com.placeholder2312.researchwidgets.widget.PaperStripWidgetProvider

/** Launcher configuration screen for one widget instance. */
class WidgetConfigActivity:Activity(){
 private var widgetId=AppWidgetManager.INVALID_APPWIDGET_ID
 override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState);setResult(RESULT_CANCELED);widgetId=intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,AppWidgetManager.INVALID_APPWIDGET_ID);if(widgetId==AppWidgetManager.INVALID_APPWIDGET_ID){finish();return}
  val root=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(dp(22),dp(18),dp(22),dp(18))}
  root.addView(TextView(this).apply{text="Research Paper Strip";textSize=22f})
  root.addView(TextView(this).apply{text="Choose which Aftergleam feed this widget displays.";textSize=14f;setPadding(0,dp(8),0,dp(12))})
  val group=RadioGroup(this).apply{orientation=RadioGroup.VERTICAL};val rec=RadioButton(this).apply{id=101;text="Recommended — today's ranked digest"};val saved=RadioButton(this).apply{id=102;text="Saved — most recently saved first"};group.addView(rec);group.addView(saved);group.check(if(WidgetPreferences.mode(this,widgetId)==FeedMode.SAVED)saved.id else rec.id);root.addView(group)
  root.addView(TextView(this).apply{text=PaperProviderClient.read(this@WidgetConfigActivity,FeedMode.RECOMMENDED).message;textSize=13f;setPadding(0,dp(14),0,dp(14))})
  val actions=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL}
  actions.addView(Button(this).apply{text="Cancel";setOnClickListener{finish()}},LinearLayout.LayoutParams(0,ViewGroup.LayoutParams.WRAP_CONTENT,1f))
  actions.addView(Button(this).apply{text="Add widget";setOnClickListener{val mode=if(group.checkedRadioButtonId==saved.id)FeedMode.SAVED else FeedMode.RECOMMENDED;WidgetPreferences.setMode(this@WidgetConfigActivity,widgetId,mode);PaperStripWidgetProvider.updateWidget(this@WidgetConfigActivity,widgetId);setResult(RESULT_OK,Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,widgetId));finish()}},LinearLayout.LayoutParams(0,ViewGroup.LayoutParams.WRAP_CONTENT,1f))
  root.addView(actions);setContentView(root)
 }
 private fun dp(v:Int)=(v*resources.displayMetrics.density).toInt()
}
