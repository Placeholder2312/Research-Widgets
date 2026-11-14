package com.placeholder2312.researchwidgets.widget

import android.app.ActivityOptions
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build

/**
 * Builds the activity PendingIntents used by the widgets.
 *
 * On Android 15+ a PendingIntent's creator no longer lends its background-activity-start privileges by
 * default, so we opt in explicitly. Widget taps launch from a background app process otherwise, and a
 * blocked launch fails silently.
 */
internal object PendingIntents {
    fun activity(context: Context, requestCode: Int, intent: Intent, mutable: Boolean = false): PendingIntent {
        var flags = PendingIntent.FLAG_UPDATE_CURRENT
        flags = flags or if (mutable && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_MUTABLE
        } else if (!mutable) {
            PendingIntent.FLAG_IMMUTABLE
        } else {
            0
        }
        val options = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            ActivityOptions.makeBasic()
                .setPendingIntentCreatorBackgroundActivityStartMode(ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOWED)
                .toBundle()
        } else {
            null
        }
        return PendingIntent.getActivity(context, requestCode, intent, flags, options)
    }
}
