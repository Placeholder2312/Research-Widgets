package com.placeholder2312.researchwidgets.data
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Build
import android.util.Log

enum class FeedMode(val key:String,val label:String){RECOMMENDED("recommended","Recommended"),SAVED("saved","Saved");companion object{fun from(v:String?)=if(v==SAVED.key)SAVED else RECOMMENDED}}
data class WidgetPaper(val id:String,val title:String,val authors:String,val source:String,val published:String,val position:Int,val feedType:String)
enum class ProviderState{CONNECTED,NOT_INSTALLED,PERMISSION_DENIED,UNAVAILABLE}
data class FeedRead(val state:ProviderState,val message:String,val papers:List<WidgetPaper> = emptyList()){val connected get()=state==ProviderState.CONNECTED}

object PaperProviderClient {
 private const val TAG="ResearchWidgets"
 @Suppress("DEPRECATION")
 private fun resolveLegacy(context:Context)=context.packageManager.resolveContentProvider(AftergleamContract.AUTHORITY,0)
 fun read(context:Context,mode:FeedMode):FeedRead {
  val provider=if(Build.VERSION.SDK_INT>=33)context.packageManager.resolveContentProvider(AftergleamContract.AUTHORITY,PackageManager.ComponentInfoFlags.of(0L)) else resolveLegacy(context)
  if(provider==null)return FeedRead(ProviderState.NOT_INSTALLED,"Install the customized Aftergleam fork to connect.")
  val uri=if(mode==FeedMode.RECOMMENDED)AftergleamContract.RECOMMENDED_URI else AftergleamContract.SAVED_URI
  return try {
   val cursor=context.contentResolver.query(uri,AftergleamContract.COLUMNS,null,null,null)?:return FeedRead(ProviderState.UNAVAILABLE,"Aftergleam did not return a feed.")
   val items=cursor.use(::rows)
   FeedRead(ProviderState.CONNECTED,if(items.isEmpty())if(mode==FeedMode.SAVED)"No saved papers yet." else "Today's digest is empty or has not been built yet." else "Connected to Aftergleam.",items)
  }catch(e:SecurityException){FeedRead(ProviderState.PERMISSION_DENIED,"Access denied. Check that both apps use the same signing certificate.")}catch(e:Exception){Log.w(TAG,"Unable to read Aftergleam ${mode.key} feed",e);FeedRead(ProviderState.UNAVAILABLE,"Could not read Aftergleam. Open both apps and try again.")}
 }
 private fun rows(c:Cursor):List<WidgetPaper>{val id=c.getColumnIndexOrThrow(AftergleamContract.COLUMN_PAPER_ID);val title=c.getColumnIndexOrThrow(AftergleamContract.COLUMN_TITLE);val authors=c.getColumnIndexOrThrow(AftergleamContract.COLUMN_AUTHORS);val source=c.getColumnIndexOrThrow(AftergleamContract.COLUMN_SOURCE);val published=c.getColumnIndexOrThrow(AftergleamContract.COLUMN_PUBLISHED);val position=c.getColumnIndexOrThrow(AftergleamContract.COLUMN_FEED_POSITION);val type=c.getColumnIndexOrThrow(AftergleamContract.COLUMN_FEED_TYPE);return buildList{while(c.moveToNext()){val paperId=c.getString(id)?:continue;val paperTitle=c.getString(title)?:continue;add(WidgetPaper(paperId,paperTitle,c.getString(authors).orEmpty(),c.getString(source).orEmpty(),c.getString(published).orEmpty(),c.getInt(position),c.getString(type).orEmpty()))}}}
}
