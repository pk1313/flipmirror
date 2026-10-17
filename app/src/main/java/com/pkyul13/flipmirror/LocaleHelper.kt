package com.pkyul13.flipmirror

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import java.util.Locale

object LocaleHelper {

    fun getLocale(languageCode: String): Locale {
        return when (languageCode.lowercase()) {
            LanguagePreferences.LANG_KO -> Locale.KOREAN
            else -> Locale.ENGLISH
        }
    }

    fun applyLocale(context: Context, languageCode: String): Context {
        val locale = getLocale(languageCode)
        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocales(LocaleList(locale))
        } else {
            @Suppress("DEPRECATION")
            config.locale = locale
        }
        config.setLayoutDirection(locale)

        return context.createConfigurationContext(config)
    }
}
