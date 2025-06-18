package com.pplm.saku

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate
import com.pplm.saku.utils.AppPreferences
import com.pplm.saku.utils.LocaleHelper
import java.util.Locale

class SakuApplication : Application() {

    companion object {
        private lateinit var instance: SakuApplication

        fun getInstance(): SakuApplication {
            return instance
        }

        var isDarkModeChange = false
    }

    lateinit var appPreferences: AppPreferences

    override fun onCreate() {
        super.onCreate()
        instance = this

        appPreferences = AppPreferences(this)

        applyDarkMode(appPreferences.isDarkModeEnabled())

        setLocale(this, appPreferences.getLanguage())
    }

    fun applyDarkMode(isDarkMode: Boolean) {
        isDarkModeChange = true
        AppCompatDelegate.setDefaultNightMode(
            if (isDarkMode) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
        isDarkModeChange = false
    }

    fun setLocale(context: Context, languageCode: String) {
        // Skip locale change during dark mode change
        if (isDarkModeChange) return

        val locale = when (languageCode) {
            "Indonesia" -> Locale("id", "ID")
            "English" -> Locale("en", "US")
            else -> Locale("id", "ID")
        }

        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
    }

    override fun attachBaseContext(base: Context) {
        val pref = AppPreferences(base)
        val newContext = LocaleHelper.setLocale(base, pref.getLanguage())
        super.attachBaseContext(newContext)
    }
}