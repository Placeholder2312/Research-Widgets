package com.placeholder2312.researchwidgets.data

import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Build
import android.os.SystemClock
import android.util.Log

/** Which Aftergleam feed a widget shows. */
enum class FeedMode(val key: String, val label: String) {
    RECOMMENDED("recommended", "Recommended"),
    SAVED("saved", "Saved");

    companion object {
        fun from(value: String?) = if (value == SAVED.key) SAVED else RECOMMENDED
    }
}

/** Look of one Paper Strip widget: dots under the title (ROW) or a vertical dot rail on the swipe axis (RAIL). */
enum class WidgetStyle(val key: String) {
    ROW("row"),
    RAIL("rail");

    companion object {
        fun from(value: String?) = if (value == RAIL.key) RAIL else ROW
    }
}

data class WidgetPaper(
    val id: String,
    val title: String,
    val authors: String,
    val source: String,
    val published: String,
    val position: Int,
    val feedType: String
)

enum class ProviderState { CONNECTED, NOT_INSTALLED, PERMISSION_DENIED, UNAVAILABLE }

/** Result of one provider query: a state, a human-readable message and (when connected) the papers. */
data class FeedRead(
    val state: ProviderState,
    val message: String,
    val papers: List<WidgetPaper> = emptyList()
) {
    val connected get() = state == ProviderState.CONNECTED
}

/** Read-only client for the Aftergleam provider. Never throws: failures come back as a [ProviderState]. */
object PaperProviderClient {
    private const val TAG = "ResearchWidgets"
    private const val DEFAULT_MAX_AGE_MS = 5_000L

    private class Cached(val at: Long, val read: FeedRead)

    private val cache = HashMap<FeedMode, Cached>()

    /** Fresh query; also remembered so the list adapter can reuse it instead of querying again. */
    fun read(context: Context, mode: FeedMode): FeedRead =
        query(context, mode).also {
            synchronized(cache) { cache[mode] = Cached(SystemClock.elapsedRealtime(), it) }
        }

    /** Reuses a read from the last few seconds (the widget provider and its list adapter refresh together). */
    fun recent(context: Context, mode: FeedMode, maxAgeMs: Long = DEFAULT_MAX_AGE_MS): FeedRead {
        synchronized(cache) {
            val hit = cache[mode]
            if (hit != null && SystemClock.elapsedRealtime() - hit.at <= maxAgeMs) return hit.read
        }
        return read(context, mode)
    }

    private fun query(context: Context, mode: FeedMode): FeedRead {
        val provider = try {
            resolveProvider(context)
        } catch (e: Exception) {
            Log.w(TAG, "Unable to check whether the Aftergleam provider is installed", e)
            return FeedRead(ProviderState.UNAVAILABLE, "Could not check Aftergleam availability. Open both apps and try again.")
        }
        if (provider == null) {
            return FeedRead(ProviderState.NOT_INSTALLED, "Install the customized Aftergleam fork to connect.")
        }

        val uri = if (mode == FeedMode.RECOMMENDED) AftergleamContract.RECOMMENDED_URI else AftergleamContract.SAVED_URI
        return try {
            val cursor = context.contentResolver.query(uri, AftergleamContract.COLUMNS, null, null, null)
                ?: return FeedRead(ProviderState.UNAVAILABLE, "Aftergleam did not return a feed.")
            val papers = cursor.use(::parseRows)
            FeedRead(ProviderState.CONNECTED, connectedMessage(mode, papers.isEmpty()), papers)
        } catch (e: SecurityException) {
            FeedRead(ProviderState.PERMISSION_DENIED, "Access denied. Check that both apps use the same signing certificate.")
        } catch (e: Exception) {
            Log.w(TAG, "Unable to read Aftergleam ${mode.key} feed", e)
            FeedRead(ProviderState.UNAVAILABLE, "Could not read Aftergleam. Open both apps and try again.")
        }
    }

    private fun connectedMessage(mode: FeedMode, empty: Boolean) = when {
        !empty -> "Connected to Aftergleam."
        mode == FeedMode.SAVED -> "No saved papers yet."
        else -> "Today's digest is empty or has not been built yet."
    }

    private fun resolveProvider(context: Context) =
        if (Build.VERSION.SDK_INT >= 33) {
            context.packageManager.resolveContentProvider(
                AftergleamContract.AUTHORITY, PackageManager.ComponentInfoFlags.of(0L)
            )
        } else {
            @Suppress("DEPRECATION")
            context.packageManager.resolveContentProvider(AftergleamContract.AUTHORITY, 0)
        }

    /** Maps provider rows to papers, skipping rows without an id or title. */
    internal fun parseRows(cursor: Cursor): List<WidgetPaper> {
        val id = cursor.getColumnIndexOrThrow(AftergleamContract.COLUMN_PAPER_ID)
        val title = cursor.getColumnIndexOrThrow(AftergleamContract.COLUMN_TITLE)
        val authors = cursor.getColumnIndexOrThrow(AftergleamContract.COLUMN_AUTHORS)
        val source = cursor.getColumnIndexOrThrow(AftergleamContract.COLUMN_SOURCE)
        val published = cursor.getColumnIndexOrThrow(AftergleamContract.COLUMN_PUBLISHED)
        val position = cursor.getColumnIndexOrThrow(AftergleamContract.COLUMN_FEED_POSITION)
        val type = cursor.getColumnIndexOrThrow(AftergleamContract.COLUMN_FEED_TYPE)
        return buildList {
            while (cursor.moveToNext()) {
                val paperId = cursor.getString(id) ?: continue
                val paperTitle = cursor.getString(title) ?: continue
                add(
                    WidgetPaper(
                        id = paperId,
                        title = paperTitle,
                        authors = cursor.getString(authors).orEmpty(),
                        source = cursor.getString(source).orEmpty(),
                        published = cursor.getString(published).orEmpty(),
                        position = cursor.getInt(position),
                        feedType = cursor.getString(type).orEmpty()
                    )
                )
            }
        }
    }
}
