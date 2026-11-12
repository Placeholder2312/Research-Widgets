package com.placeholder2312.researchwidgets.ui
import android.app.Activity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import com.placeholder2312.researchwidgets.R
import com.placeholder2312.researchwidgets.data.FeedMode
import com.placeholder2312.researchwidgets.data.PaperProviderClient
import com.placeholder2312.researchwidgets.widget.PaperStripWidgetProvider
class MainActivity:Activity(){
 override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState);setContentView(R.layout.activity_main);val status=findViewById<TextView>(R.id.connection_status);fun refresh(){status.text=PaperProviderClient.read(this,FeedMode.RECOMMENDED).message};findViewById<Button>(R.id.check_connection).setOnClickListener{refresh()};findViewById<Button>(R.id.refresh_widgets).setOnClickListener{PaperStripWidgetProvider.refreshAll(this);refresh()};refresh()}
 override fun onResume(){super.onResume();findViewById<TextView?>(R.id.connection_status)?.text=PaperProviderClient.read(this,FeedMode.RECOMMENDED).message}
}
