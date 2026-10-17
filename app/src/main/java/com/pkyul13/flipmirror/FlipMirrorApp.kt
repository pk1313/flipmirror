package com.pkyul13.flipmirror

import android.app.Application
import android.content.Context

class FlipMirrorApp : Application() {
    override fun onCreate() {
        super.onCreate()
        LanguagePreferences.init(this)
        ShizukuManager.init()
        DisplayResolutionManager.init(this)
    }

    override fun attachBaseContext(base: Context) {
        val lang = LanguagePreferences.getLanguage(base)
        super.attachBaseContext(LocaleHelper.applyLocale(base, lang))
    }

    override fun onTerminate() {
        super.onTerminate()
        ShizukuManager.destroy()
    }
}
