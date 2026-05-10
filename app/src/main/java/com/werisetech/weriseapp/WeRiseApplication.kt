package com.werisetech.weriseapp

import android.app.Application
import org.osmdroid.config.Configuration

/**
 * Application entry point.
 *
 * The only setup performed here is configuring OSMDroid's user-agent (which
 * tile servers require) and pointing its caches at app-private storage. No
 * analytics SDKs, no crash reporters, and no location reads happen at startup.
 */
class WeRiseApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Configuration.getInstance().apply {
            userAgentValue = USER_AGENT
            osmdroidBasePath = filesDir
            osmdroidTileCache = cacheDir
        }
    }

    private companion object {
        /** Identifies the app politely to tile servers without identifying the user. */
        const val USER_AGENT = "WeRiseDFW/1.0 (We Rise DFW)"
    }
}
