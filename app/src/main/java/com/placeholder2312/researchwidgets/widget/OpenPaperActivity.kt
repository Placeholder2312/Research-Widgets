package com.placeholder2312.researchwidgets.widget

import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import com.placeholder2312.researchwidgets.data.AftergleamContract

/**
 * Invisible trampoline for widget taps. The widget's pending-intent template targets this activity
 * (in this app), which then opens the paper in Aftergleam from the foreground. Doing the hand-off here
 * avoids silent failures from the launcher sending a mutable PendingIntent straight to another app,
 * and lets us tell the user when the hand-off fails instead of doing nothing.
 */
class OpenPaperActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val paperId = intent.getStringExtra(AftergleamContract.EXTRA_PAPER_ID)
        if (paperId.isNullOrBlank()) {
            Toast.makeText(this, "No paper selected", Toast.LENGTH_SHORT).show()
        } else {
            val open = Intent(AftergleamContract.ACTION_OPEN_PAPER)
                .setComponent(ComponentName(AftergleamContract.PACKAGE, AftergleamContract.PAPER_OPEN_COMPONENT))
                .putExtra(AftergleamContract.EXTRA_PAPER_ID, paperId)
            try {
                startActivity(open)
            } catch (e: SecurityException) {
                Toast.makeText(this, "Aftergleam refused to open the paper (signing key mismatch?)", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Toast.makeText(this, "Couldn't open the paper in Aftergleam", Toast.LENGTH_LONG).show()
            }
        }
        finish()
    }
}
