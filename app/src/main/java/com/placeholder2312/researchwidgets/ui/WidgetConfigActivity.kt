package com.placeholder2312.researchwidgets.ui
import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import com.placeholder2312.researchwidgets.R
import com.placeholder2312.researchwidgets.data.FeedMode
import com.placeholder2312.researchwidgets.data.PaperProviderClient
import com.placeholder2312.researchwidgets.data.WidgetPreferences
import com.placeholder2312.researchwidgets.data.WidgetStyle
import com.placeholder2312.researchwidgets.widget.PaperStripWidgetProvider

/** Launcher configuration screen for one widget instance, styled to match the widget (Nothing Classic). */
class WidgetConfigActivity:Activity(){
 private var widgetId=AppWidgetManager.INVALID_APPWIDGET_ID
 override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState);setResult(RESULT_CANCELED);widgetId=intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,AppWidgetManager.INVALID_APPWIDGET_ID);if(widgetId==AppWidgetManager.INVALID_APPWIDGET_ID){finish();return}
  val ink=getColor(R.color.nothing_ink);val muted=getColor(R.color.nothing_muted);val page=getColor(R.color.nothing_page)
  val light=Typeface.create("sans-serif-light",Typeface.NORMAL);val mono=Typeface.MONOSPACE
  val root=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setBackgroundColor(page);setPadding(dp(24),dp(40),dp(24),dp(24))}
  root.addView(TextView(this).apply{text="WIDGET SETUP";typeface=mono;textSize=11f;letterSpacing=0.12f;setTextColor(muted)})
  root.addView(TextView(this).apply{text="Research Paper Strip";typeface=light;textSize=28f;setTextColor(ink);setPadding(0,dp(8),0,0)})
  root.addView(TextView(this).apply{text="Choose which Aftergleam feed this widget displays.";typeface=light;textSize=14f;setTextColor(muted);setPadding(0,dp(8),0,dp(20))})
  val group=RadioGroup(this).apply{orientation=RadioGroup.VERTICAL;setBackgroundResource(R.drawable.nothing_card_bg);setPadding(dp(16),dp(8),dp(16),dp(8))}
  fun option(optionId:Int,label:String)=RadioButton(this).apply{id=optionId;text=label;typeface=light;textSize=15f;setTextColor(ink);setButtonDrawable(R.drawable.nothing_radio);setPadding(dp(14),dp(14),0,dp(14))}
  val rec=option(101,"Recommended — today's ranked digest");val saved=option(102,"Saved — most recently saved first")
  group.addView(rec);group.addView(saved);group.check(if(WidgetPreferences.mode(this,widgetId)==FeedMode.SAVED)saved.id else rec.id);root.addView(group)
  root.addView(TextView(this).apply{text="LOOK";typeface=mono;textSize=11f;letterSpacing=0.12f;setTextColor(muted);setPadding(0,dp(20),0,dp(8))})
  val styleGroup=RadioGroup(this).apply{orientation=RadioGroup.VERTICAL;setBackgroundResource(R.drawable.nothing_card_bg);setPadding(dp(16),dp(8),dp(16),dp(8))}
  val row=option(201,"Row — dots under the title");val rail=option(202,"Rail — dots down the right edge, with 01/12 counter")
  styleGroup.addView(row);styleGroup.addView(rail);styleGroup.check(if(WidgetPreferences.style(this,widgetId)==WidgetStyle.RAIL)rail.id else row.id);root.addView(styleGroup)
  root.addView(TextView(this).apply{text=PaperProviderClient.read(this@WidgetConfigActivity,FeedMode.RECOMMENDED).message;typeface=mono;textSize=12f;setTextColor(muted);setPadding(0,dp(18),0,dp(18))})
  fun pill(label:String,filled:Boolean)=Button(this).apply{text=label;typeface=mono;textSize=13f;letterSpacing=0.1f;stateListAnimator=null;setBackgroundResource(if(filled)R.drawable.nothing_button_filled else R.drawable.nothing_button_outline);setTextColor(if(filled)page else ink)}
  val actions=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL}
  actions.addView(pill("Cancel",false).apply{setOnClickListener{finish()}},LinearLayout.LayoutParams(0,dp(52),1f).apply{marginEnd=dp(8)})
  actions.addView(pill("Add widget",true).apply{setOnClickListener{val mode=if(group.checkedRadioButtonId==saved.id)FeedMode.SAVED else FeedMode.RECOMMENDED;WidgetPreferences.setMode(this@WidgetConfigActivity,widgetId,mode);WidgetPreferences.setStyle(this@WidgetConfigActivity,widgetId,if(styleGroup.checkedRadioButtonId==rail.id)WidgetStyle.RAIL else WidgetStyle.ROW);PaperStripWidgetProvider.updateWidget(this@WidgetConfigActivity,widgetId);setResult(RESULT_OK,Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,widgetId));finish()}},LinearLayout.LayoutParams(0,dp(52),1f).apply{marginStart=dp(8)})
  root.addView(actions);setContentView(root)
 }
 private fun dp(v:Int)=(v*resources.displayMetrics.density).toInt()
}
