package com.pplm.saku.ui.home.setting

import android.app.AlertDialog
import android.app.TimePickerDialog
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.pplm.saku.R
import com.pplm.saku.SakuApplication
import com.pplm.saku.data.AppDatabase
import com.pplm.saku.databinding.FragmentSettingsBinding
import com.pplm.saku.repository.TransactionRepository
import com.pplm.saku.utils.AlarmHelper
import com.pplm.saku.utils.AppPreferences
import com.pplm.saku.utils.LocaleHelper
import kotlinx.coroutines.launch

class SettingsFragment : Fragment(R.layout.fragment_settings) {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private lateinit var appPreferences: AppPreferences
    private lateinit var transactionRepository: TransactionRepository
    private lateinit var alarmHelper: AlarmHelper

    private var isInitialLanguageSetup = true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = FragmentSettingsBinding.bind(view)

        appPreferences = AppPreferences(requireContext())
        alarmHelper = AlarmHelper(requireContext())
        checkNotificationPermission()

        if (appPreferences.isDailyReminderEnabled()) {
            scheduleAlarm()
        }

        val transactionDao = AppDatabase.getDatabase(requireContext()).transactionDao()
        transactionRepository = TransactionRepository(transactionDao)

        setupSwitches()
        setupTimePickerDropdown()
        setupLanguageDropdown()
        setupDeleteDataButton()
    }

    override fun onResume() {
        super.onResume()
        resetSwitchStates()
    }

    private fun resetSwitchStates() {
        binding.switchDailyReminder.setOnCheckedChangeListener(null)
        binding.switchDarkMode.setOnCheckedChangeListener(null)

        binding.switchDailyReminder.isChecked = appPreferences.isDailyReminderEnabled()
        binding.switchDarkMode.isChecked = appPreferences.isDarkModeEnabled()

        setupSwitches()

        updateTimePickerState(appPreferences.isDailyReminderEnabled())
    }

    private fun setupSwitches() {
        binding.switchDailyReminder.setOnCheckedChangeListener { _, isChecked ->
            appPreferences.setDailyReminder(isChecked)
            updateTimePickerState(isChecked)
            if (isChecked) scheduleAlarm() else alarmHelper.cancelDailyReminder()
        }

        binding.switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            appPreferences.setDarkMode(isChecked)
            SakuApplication.getInstance().applyDarkMode(isChecked)
        }
    }

    private fun scheduleAlarm() {
        val (hour, minute) = appPreferences.getReminderTime().split(":").map { it.toInt() }
        alarmHelper.setDailyReminder(hour, minute)
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted && appPreferences.isDailyReminderEnabled()) {
            scheduleAlarm()
        } else {
            appPreferences.setDailyReminder(false)
            binding.switchDailyReminder.isChecked = false
            updateTimePickerState(false)
            Toast.makeText(
                requireContext(),
                getString(R.string.notification_permission_denied),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun setupTimePickerDropdown() {
        binding.txtSelectedTime.text = appPreferences.getReminderTime()
        updateTimePickerState(appPreferences.isDailyReminderEnabled())

        binding.timePickerContainer.setOnClickListener {
            if (appPreferences.isDailyReminderEnabled()) showTimePickerDialog()
        }
    }

    private fun updateTimePickerState(enabled: Boolean) {
        val primary = ContextCompat.getColor(requireContext(), R.color.textPrimary)
        val disabled = ContextCompat.getColor(requireContext(), R.color.textDisabled)

        binding.timePickerContainer.alpha = if (enabled) 1.0f else 0.5f
        binding.timePickerContainer.isEnabled = enabled
        binding.txtSelectTime.setTextColor(if (enabled) primary else disabled)
        binding.txtSelectedTime.setTextColor(if (enabled) primary else disabled)
    }

    private fun showTimePickerDialog() {
        val (hour, minute) = appPreferences.getReminderTime().split(":").map { it.toInt() }
        TimePickerDialog(requireContext(), { _, h, m ->
            val time = String.format("%02d:%02d", h, m)
            binding.txtSelectedTime.text = time
            appPreferences.setReminderTime(time)
            if (appPreferences.isDailyReminderEnabled()) scheduleAlarm()
        }, hour, minute, true).show()
    }

    private fun setupLanguageDropdown() {
        val languages = arrayOf("Indonesia", "English")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, languages)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerLanguage.adapter = adapter

        val pos = languages.indexOf(appPreferences.getLanguage())
        if (pos != -1) binding.spinnerLanguage.setSelection(pos)

        binding.spinnerLanguage.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
                if (isInitialLanguageSetup || SakuApplication.isDarkModeChange) {
                    isInitialLanguageSetup = false
                    return
                }

                val selected = languages[pos]
                if (selected != appPreferences.getLanguage()) {
                    appPreferences.setLanguage(selected)
                    setLocale(selected)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupDeleteDataButton() {
        binding.btnDeleteData.setOnClickListener {
            showDeleteDataConfirmationDialog()
        }
    }

    private fun showDeleteDataConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.delete_data))
            .setMessage(getString(R.string.delete_data_confirmation))
            .setPositiveButton(getString(R.string.yes)) { _, _ -> deleteAllData() }
            .setNegativeButton(getString(R.string.no), null)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show()
    }

    private fun deleteAllData() {
        lifecycleScope.launch {
            try {
                transactionRepository.deleteAll()
                Toast.makeText(requireContext(), getString(R.string.delete_data_success), Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), getString(R.string.delete_data_error), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setLocale(language: String) {
        if (SakuApplication.isDarkModeChange) return
        appPreferences.setLanguage(language)
        LocaleHelper.setLocale(requireContext(), language)
        requireActivity().recreate()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
