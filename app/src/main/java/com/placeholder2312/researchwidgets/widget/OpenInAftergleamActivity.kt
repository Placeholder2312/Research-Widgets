package com.placeholder2312.researchwidgets.widget

import android.app.Activity
import android.content.ActivityNotFoundException
import android.widget.Toast
import com.placeholder2312.researchwidgets.data.AftergleamContract
import com.placeholder2312.researchwidgets.data.AftergleamLinks

/**
 * Invisible hand-off for every widget tap. The launcher starts this activity (an allowed launch), and once its
 * window is actually on screen it opens Aftergleam, exactly like the in-app test button does. Starting the other
 * app from onCreate, before this window is visible, is what Android may block without any error.
 *
 * With a paper id it opens that paper; without one it just opens Aftergleam.
 */
class OpenInAftergleamActivity : Activity() {
    private var handled = false

    override fun onResume() {
        super.onResume()
        if (handled) return
        handled = true

        val paperId = intent.getStringExtra(AftergleamContract.EXTRA_PAPER_ID)
        val target = if (paperId.isNullOrBlank()) AftergleamLinks.launchApp() else AftergleamLinks.openPaper(paperId)
        try {
            startActivity(target)
        } catch (e: SecurityException) {
            toast("Aftergleam refused the request (signing key mismatch?)")
        } catch (e: ActivityNotFoundException) {
            toast("Aftergleam is not installed")
        } catch (e: Exception) {
            toast("Couldn't open Aftergleam")
        }
        finish()
    }

    private fun toast(message: String) = Toast.makeText(this, message, Toast.LENGTH_LONG).show()
}
