package com.pplm.saku.ui.add

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.pplm.saku.R
import com.pplm.saku.data.entity.Transaction
import com.pplm.saku.databinding.ActivityAddHistoryBinding
import com.pplm.saku.utils.AppPreferences
import com.pplm.saku.utils.LocaleHelper
import com.pplm.saku.viewmodel.TransactionViewModel
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

class AddHistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddHistoryBinding
    private val transactionViewModel: TransactionViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupDatePicker()
        setupNominalFormat()
        setupCategorySpinner()

        binding.btnSimpan.setOnClickListener {
            saveTransaction()
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }

    private fun setupDatePicker() {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        binding.etTanggal.setText(dateFormat.format(calendar.time))

        val onDateClick = View.OnClickListener {
            DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    calendar.set(year, month, dayOfMonth)
                    binding.etTanggal.setText(dateFormat.format(calendar.time))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        binding.etTanggal.setOnClickListener(onDateClick)
        binding.ivCalendar.setOnClickListener(onDateClick)
    }

    private fun setupNominalFormat() {
        binding.etNominal.addTextChangedListener(object : TextWatcher {
            private var current = ""
            private val decimalFormat = DecimalFormat("#,###")

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (s.toString() != current) {
                    binding.etNominal.removeTextChangedListener(this)

                    val cleanString = s.toString().replace("[^\\d]".toRegex(), "")
                    if (cleanString.isNotEmpty()) {
                        val parsed = cleanString.toBigDecimalOrNull() ?: return
                        val formatted = decimalFormat.format(parsed)
                        current = formatted
                        binding.etNominal.setText(formatted)
                        binding.etNominal.setSelection(formatted.length)
                    } else {
                        current = ""
                    }

                    binding.etNominal.addTextChangedListener(this)
                }
            }
        })
    }

    private fun setupCategorySpinner() {
        val categories = arrayOf(
            getString(R.string.select_category),
            getString(R.string.expense),
            getString(R.string.income)
        )

        val adapter = object : ArrayAdapter<String>(
            this,
            R.layout.spinner_item,
            categories
        ) {
            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getDropDownView(position, convertView, parent)

                view.isEnabled = position != 0
                view.alpha = if (position == 0) 0.5f else 1.0f

                val width = binding.spinnerContainer.width
                if (width > 0) {
                    view.layoutParams = view.layoutParams.apply {
                        this.width = width
                    }
                }

                return view
            }

            override fun isEnabled(position: Int): Boolean = position != 0
        }

        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        binding.spinnerKategori.adapter = adapter
        binding.spinnerKategori.setSelection(0, false)

        binding.spinnerContainer.setOnClickListener {
            binding.spinnerKategori.performClick()
        }
    }

    private fun saveTransaction() {
        val tanggal = binding.etTanggal.text.toString()
        val nominalStr = binding.etNominal.text.toString().replace("[^\\d]".toRegex(), "")
        val kategori = binding.spinnerKategori.selectedItem.toString()
        val catatan = binding.etCatatan.text.toString()

        var isValid = true

        if (tanggal.isEmpty()) {
            binding.etTanggal.error = getString(R.string.error_empty_date)
            isValid = false
        }

        if (nominalStr.isEmpty()) {
            binding.etNominal.error = getString(R.string.error_empty_amount)
            isValid = false
        }

        if (kategori == getString(R.string.select_category)) {
            binding.spinnerContainer.setBackgroundResource(R.drawable.edittext_border_error)
            Toast.makeText(this, getString(R.string.error_category_required), Toast.LENGTH_SHORT).show()
            isValid = false
        } else {
            binding.spinnerContainer.setBackgroundResource(R.drawable.edittext_border)
        }

        if (!isValid) return

        val transaction = Transaction(
            tanggal = tanggal,
            nominal = nominalStr.toLong(),
            kategori = kategori,
            catatan = catatan
        )

        transactionViewModel.insert(transaction)

        Toast.makeText(this, getString(R.string.success_save_transaction), Toast.LENGTH_SHORT).show()
        finish()
    }

    override fun attachBaseContext(newBase: Context) {
        val pref = AppPreferences(newBase)
        val newContext = LocaleHelper.setLocale(newBase, pref.getLanguage())
        super.attachBaseContext(newContext)
    }
}