package com.pkyul13.flipmirror

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object LanguagePreferences {
    private const val PREFS_NAME = "flipmirror_prefs"
    private const val KEY_LANGUAGE = "app_language"

    const val LANG_EN = "en"
    const val LANG_KO = "ko"
    const val DEFAULT_LANGUAGE = LANG_EN // Default is English

    private val _languageFlow = MutableStateFlow(DEFAULT_LANGUAGE)
    val languageFlow: StateFlow<String> = _languageFlow

    fun init(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val lang = prefs.getString(KEY_LANGUAGE, DEFAULT_LANGUAGE) ?: DEFAULT_LANGUAGE
        _languageFlow.value = lang
    }

    fun getLanguage(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_LANGUAGE, DEFAULT_LANGUAGE) ?: DEFAULT_LANGUAGE
    }

    fun setLanguage(context: Context, lang: String) {
        val validLang = if (lang == LANG_KO) LANG_KO else LANG_EN
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_LANGUAGE, validLang).apply()
        _languageFlow.value = validLang
    }
}
