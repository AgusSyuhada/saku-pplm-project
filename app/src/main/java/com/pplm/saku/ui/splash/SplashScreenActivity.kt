package com.pplm.saku.ui.splash

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.pplm.saku.R
import com.pplm.saku.ui.home.MainActivity
import com.pplm.saku.utils.AppPreferences
import com.pplm.saku.utils.LocaleHelper

@SuppressLint("CustomSplashScreen")
class SplashScreenActivity : AppCompatActivity() {

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            goToNextScreen()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    goToNextScreen()
                }

                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }

                else -> {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            goToNextScreen()
        }
    }

    private fun goToNextScreen() {
        Handler(Looper.getMainLooper()).postDelayed({
             startActivity(Intent(this, MainActivity::class.java))
             finish()
        }, 2000)
    }

    override fun attachBaseContext(newBase: Context) {
        val pref = AppPreferences(newBase)
        val newContext = LocaleHelper.setLocale(newBase, pref.getLanguage())
        super.attachBaseContext(newContext)
    }
}
