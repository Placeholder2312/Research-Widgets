package com.placeholder2312.researchwidgets.ui

import android.app.Activity
import android.appwidget.AppWidgetManager
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
import com.placeholder2312.researchwidgets.data.FeedMode
import com.placeholder2312.researchwidgets.data.PaperProviderClient
import com.placeholder2312.researchwidgets.widget.AftergleamTileWidgetProvider
import com.placeholder2312.researchwidgets.widget.PaperStripWidgetProvider

/** Home screen of the app: connection status plus add/refresh controls for both widgets. */
class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.check_connection).setOnClickListener { refreshStatus() }
        findViewById<Button>(R.id.refresh_widgets).setOnClickListener {
            PaperStripWidgetProvider.refreshAll(this)
            refreshStatus()
        }
        findViewById<Button>(R.id.add_strip).setOnClickListener { pin(PaperStripWidgetProvider::class.java) }
        findViewById<Button>(R.id.add_tile).setOnClickListener { pin(AftergleamTileWidgetProvider::class.java) }
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
            val message = PaperProviderClient.read(this, FeedMode.RECOMMENDED).message
            runOnUiThread {
                if (!isDestroyed) findViewById<TextView>(R.id.connection_status).text = message
            }
        }.start()
        bindWidgetRow(PaperStripWidgetProvider::class.java, R.id.strip_count, R.id.strip_dot)
        bindWidgetRow(AftergleamTileWidgetProvider::class.java, R.id.tile_count, R.id.tile_dot)
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

    /** Asks the launcher to add the widget; not every launcher supports this. */
    private fun pin(provider: Class<*>) {
        val manager = AppWidgetManager.getInstance(this)
        val supported = manager.isRequestPinAppWidgetSupported &&
            manager.requestPinAppWidget(ComponentName(this, provider), null, null)
        if (!supported) {
            Toast.makeText(this, "Add it from your launcher's widget picker", Toast.LENGTH_LONG).show()
        }
    }
}
