package com.placeholder2312.researchwidgets.data

import android.content.Context

/** Per-widget settings: two widget instances can use different feeds and styles. */
object WidgetPreferences {
    private const val PREFS = "widget_instances"

    private fun prefs(context: Context) = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    private fun modeKey(id: Int) = "mode_$id"
    private fun styleKey(id: Int) = "style_$id"
    private fun digestKey(id: Int) = "digest_$id"
    private fun indexKey(id: Int) = "index_$id"

    fun mode(context: Context, id: Int) =
        FeedMode.from(prefs(context).getString(modeKey(id), FeedMode.RECOMMENDED.key))

    fun setMode(context: Context, id: Int, mode: FeedMode) {
        // A different feed starts again at its first paper.
        prefs(context).edit().putString(modeKey(id), mode.key).remove(indexKey(id)).apply()
    }

    fun style(context: Context, id: Int) = WidgetStyle.from(prefs(context).getString(styleKey(id), null))

    fun setStyle(context: Context, id: Int, style: WidgetStyle) {
        prefs(context).edit().putString(styleKey(id), style.key).apply()
    }

    fun digestMode(context: Context, id: Int) = DigestMode.from(prefs(context).getString(digestKey(id), null))

    fun setDigestMode(context: Context, id: Int, mode: DigestMode) {
        prefs(context).edit().putString(digestKey(id), mode.key).apply()
    }

    /** Which paper of the feed the strip shows (0 = first). Callers clamp it to the current feed size. */
    fun index(context: Context, id: Int) = prefs(context).getInt(indexKey(id), 0)

    fun setIndex(context: Context, id: Int, index: Int) {
        prefs(context).edit().putInt(indexKey(id), index.coerceAtLeast(0)).apply()
    }

    fun clear(context: Context, id: Int) {
        prefs(context).edit()
            .remove(modeKey(id)).remove(styleKey(id)).remove(digestKey(id)).remove(indexKey(id))
            .apply()
    }
}
