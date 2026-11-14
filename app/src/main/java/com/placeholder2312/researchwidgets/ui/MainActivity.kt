package com.placeholder2312.researchwidgets.ui

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.placeholder2312.researchwidgets.R
import com.placeholder2312.researchwidgets.data.AftergleamContract
import com.placeholder2312.researchwidgets.data.AftergleamLinks
import com.placeholder2312.researchwidgets.data.FeedMode
import com.placeholder2312.researchwidgets.data.PaperProviderClient
import com.placeholder2312.researchwidgets.widget.AftergleamTileWidgetProvider
import com.placeholder2312.researchwidgets.widget.DigestTileWidgetProvider
import com.placeholder2312.researchwidgets.widget.PaperStripWidgetProvider
import com.placeholder2312.researchwidgets.widget.TapPaperWidgetProvider

/** Home screen of the app: connection status plus add/refresh controls for both widgets. */
class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.check_connection).setOnClickListener { refreshStatus() }
        findViewById<Button>(R.id.open_paper).setOnClickListener { openNewestPaper() }
        findViewById<Button>(R.id.refresh_widgets).setOnClickListener {
            PaperStripWidgetProvider.refreshAll(this)
            TapPaperWidgetProvider.refreshAll(this)
            DigestTileWidgetProvider.refreshAll(this)
            refreshStatus()
        }
        findViewById<Button>(R.id.add_strip).setOnClickListener { pin(PaperStripWidgetProvider::class.java) }
        findViewById<Button>(R.id.add_plain_strip).setOnClickListener { pin(TapPaperWidgetProvider::class.java) }
        findViewById<Button>(R.id.add_tile).setOnClickListener { pin(AftergleamTileWidgetProvider::class.java) }
        findViewById<Button>(R.id.add_digest).setOnClickListener { pin(DigestTileWidgetProvider::class.java) }
        findViewById<TextView>(R.id.app_version).text = versionLabel()
    }

    /** Runs after onCreate too, so this is also the first status load. */
    override fun onResume() {
        super.onResume()
        refreshStatus()
    }

    /** "V1.5.1 · BUILD 10" for this app, plus the connected Aftergleam version when it can be read. */
    private fun versionLabel(): String {
        val own = packageInfo(packageName)
        val label = "V${own?.versionName ?: "?"} · BUILD ${own?.let(::buildNumber) ?: "?"}"
        val aftergleam = packageInfo(AftergleamContract.PACKAGE)?.versionName
        return if (aftergleam == null) label else "$label\nAFTERGLEAM V$aftergleam"
    }

    private fun packageInfo(pkg: String): PackageInfo? = try {
        if (Build.VERSION.SDK_INT >= 33) {
            packageManager.getPackageInfo(pkg, PackageManager.PackageInfoFlags.of(0L))
        } else {
            @Suppress("DEPRECATION") packageManager.getPackageInfo(pkg, 0)
        }
    } catch (e: PackageManager.NameNotFoundException) {
        null
    }

    private fun buildNumber(info: PackageInfo): Long =
        if (Build.VERSION.SDK_INT >= 28) info.longVersionCode else @Suppress("DEPRECATION") info.versionCode.toLong()

    /** The provider query runs off the main thread so a slow Aftergleam can't freeze the screen. */
    private fun refreshStatus() {
        Thread {
            val result = PaperProviderClient.read(this, FeedMode.RECOMMENDED)
            runOnUiThread {
                if (isDestroyed) return@runOnUiThread
                findViewById<TextView>(R.id.connection_status).text = result.message
                findViewById<ImageView>(R.id.connection_dot).setImageResource(
                    if (result.connected) R.drawable.nothing_dot_red else R.drawable.nothing_dot_off
                )
            }
        }.start()
        bindWidgetRow(PaperStripWidgetProvider::class.java, R.id.strip_count, R.id.strip_dot)
        bindWidgetRow(TapPaperWidgetProvider::class.java, R.id.plain_strip_count, R.id.plain_strip_dot)
        bindWidgetRow(AftergleamTileWidgetProvider::class.java, R.id.tile_count, R.id.tile_dot)
        bindWidgetRow(DigestTileWidgetProvider::class.java, R.id.digest_tile_count, R.id.digest_dot)
    }

    private fun bindWidgetRow(provider: Class<*>, countId: Int, dotId: Int) {
        val placed = AppWidgetManager.getInstance(this)
            .getAppWidgetIds(ComponentName(this, provider)).size
        findViewById<TextView>(countId).text =
            if (placed == 0) "NOT ON HOME SCREEN" else "$placed ON HOME SCREEN"
        findViewById<ImageView>(dotId).setImageResource(
            if (placed == 0) R.drawable.nothing_dot_off else R.drawable.nothing_dot_red
        )
    }

    /**
     * Opens the first paper of the feed through the same intent the widgets use. If this works but a widget
     * tap doesn't, the problem is in the launcher hand-off, not in Aftergleam or the signing key.
     */
    private fun openNewestPaper() {
        Thread {
            val read = PaperProviderClient.read(this, FeedMode.RECOMMENDED)
            val paper = read.papers.firstOrNull()
                ?: PaperProviderClient.read(this, FeedMode.SAVED).papers.firstOrNull()
            runOnUiThread {
                if (isDestroyed) return@runOnUiThread
                if (paper == null) {
                    toast(if (read.connected) "No papers to open yet" else read.message)
                    return@runOnUiThread
                }
                try {
                    startActivity(AftergleamLinks.openPaper(paper.id))
                } catch (e: SecurityException) {
                    toast("Aftergleam refused the request (signing key mismatch?)")
                } catch (e: ActivityNotFoundException) {
                    toast("Aftergleam's paper-open screen was not found")
                } catch (e: Exception) {
                    toast("Couldn't open the paper in Aftergleam")
                }
            }
        }.start()
    }

    private fun toast(message: String) = Toast.makeText(this, message, Toast.LENGTH_LONG).show()

    /** Asks the launcher to add the widget; not every launcher supports this. */
    private fun pin(provider: Class<*>) {
        val manager = AppWidgetManager.getInstance(this)
        val supported = manager.isRequestPinAppWidgetSupported &&
            manager.requestPinAppWidget(ComponentName(this, provider), null, null)
        if (!supported) {
            toast("Add it from your launcher's widget picker")
        }
    }
}
