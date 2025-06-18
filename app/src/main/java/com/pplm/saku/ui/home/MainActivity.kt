package com.pplm.saku.ui.home

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.pplm.saku.databinding.ActivityMainBinding
import com.pplm.saku.ui.add.AddHistoryActivity
import com.pplm.saku.ui.home.history.HistoryFragment
import com.pplm.saku.ui.home.home.HomeFragment
import com.pplm.saku.ui.home.setting.SettingsFragment
import com.pplm.saku.utils.AppPreferences
import com.pplm.saku.utils.LocaleHelper

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupFab()
        setupBottomNavigation()

        if (savedInstanceState == null) {
            replaceFragment(HomeFragment())
        }
    }

    private fun setupFab() {
        binding.fab.setOnClickListener {
            val currentFragment = supportFragmentManager.findFragmentById(binding.fragmentContainer.id)
            when (currentFragment) {
                is HomeFragment, is HistoryFragment -> {
                    val intent = Intent(this, AddHistoryActivity::class.java)
                    startActivity(intent)
                }
                is SettingsFragment -> {
                    // FAB disabled in SettingsFragment
                }
            }
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigationView.setOnItemSelectedListener {
            when (it.itemId) {
                com.pplm.saku.R.id.nav_home -> {
                    replaceFragment(HomeFragment())
                    binding.fab.show()
                    true
                }
                com.pplm.saku.R.id.nav_history -> {
                    replaceFragment(HistoryFragment())
                    binding.fab.show()
                    true
                }
                com.pplm.saku.R.id.nav_settings -> {
                    replaceFragment(SettingsFragment())
                    binding.fab.hide()
                    true
                }
                else -> false
            }
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(binding.fragmentContainer.id, fragment)
            .commit()
    }

    override fun attachBaseContext(newBase: Context) {
        val pref = AppPreferences(newBase)
        val newContext = LocaleHelper.setLocale(newBase, pref.getLanguage())
        super.attachBaseContext(newContext)
    }
}
