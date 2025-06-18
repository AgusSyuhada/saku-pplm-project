package com.pplm.saku.ui.detail

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.pplm.saku.R
import com.pplm.saku.data.entity.Transaction
import com.pplm.saku.utils.AppPreferences
import com.pplm.saku.utils.LocaleHelper
import com.pplm.saku.viewmodel.TransactionViewModel
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

@SuppressLint("LongLogTag")
class DetailTransactionActivity : AppCompatActivity() {

    private val TAG = "DetailTransactionActivity"
    private val transactionViewModel: TransactionViewModel by viewModels()

    private lateinit var etTanggal: EditText
    private lateinit var ivCalendar: ImageView
    private lateinit var etNominal: EditText
    private lateinit var spinnerKategori: Spinner
    private lateinit var tvKategori: TextView
    private lateinit var ivDropdown: ImageView
    private lateinit var spinnerContainer: View
    private lateinit var etCatatan: EditText
    private lateinit var btnEdit: Button
    private lateinit var btnSimpan: Button
    private lateinit var fabDelete: View

    private var currentTransaction: Transaction? = null
    private var isEditMode = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_transaction)

        setupToolbar()
        initViews()
        setupListeners()

        val transactionId = getTransactionIdFromIntent()
        if (transactionId != -1L) {
            observeTransaction(transactionId)
        } else {
            showToast("Error: Invalid transaction ID")
            finish()
        }
    }

    private fun setupToolbar() {
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressed() }
    }

    private fun initViews() {
        etTanggal = findViewById(R.id.etTanggal)
        ivCalendar = findViewById(R.id.ivCalendar)
        etNominal = findViewById(R.id.etNominal)
        spinnerKategori = findViewById(R.id.spinnerKategori)
        tvKategori = findViewById(R.id.tvKategori)
        ivDropdown = findViewById(R.id.ivDropdown)
        spinnerContainer = findViewById(R.id.spinnerContainer)
        etCatatan = findViewById(R.id.etCatatan)
        btnEdit = findViewById(R.id.btnEdit)
        btnSimpan = findViewById(R.id.btnSimpan)
        fabDelete = findViewById(R.id.fabDelete)

        toggleEditMode(false)
        setupSpinner()
        setupNominalFormat()
        setupDatePicker()
    }

    private fun setupListeners() {
        btnEdit.setOnClickListener { toggleEditMode(true) }
        btnSimpan.setOnClickListener {
            saveTransaction()
            toggleEditMode(false)
        }

        fabDelete.setOnClickListener { showDeleteConfirmationDialog() }
    }

    private fun getTransactionIdFromIntent(): Long {
        return try {
            intent.getSerializableExtra("TRANSACTION_ID").toString().toLong()
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing transaction ID: ${e.message}")
            -1L
        }
    }

    private fun observeTransaction(transactionId: Long) {
        transactionViewModel.getTransaction(transactionId).observe(this) { transaction ->
            if (transaction != null) {
                currentTransaction = transaction
                displayTransactionData(transaction)
            } else {
                showToast("Error: Could not load transaction data")
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun displayTransactionData(transaction: Transaction) {
        etTanggal.setText(transaction.tanggal)
        etNominal.setText(DecimalFormat("#,###").format(transaction.nominal))
        tvKategori.text = transaction.kategori
        etCatatan.setText(transaction.catatan)

        val categories = resources.getStringArray(R.array.transaction_categories)
        val index = categories.indexOf(transaction.kategori)
        if (index >= 0) spinnerKategori.setSelection(index)
    }

    private fun saveTransaction() {
        val tanggal = etTanggal.text.toString()
        val nominal = etNominal.text.toString().replace("[^\\d]".toRegex(), "")
        val kategori = spinnerKategori.selectedItem.toString()
        val catatan = etCatatan.text.toString()

        var isValid = true
        if (tanggal.isEmpty()) {
            etTanggal.error = "Tanggal tidak boleh kosong"
            isValid = false
        }

        if (nominal.isEmpty()) {
            etNominal.error = "Nominal tidak boleh kosong"
            isValid = false
        }

        if (kategori == getString(R.string.select_category)) {
            spinnerContainer.setBackgroundResource(R.drawable.edittext_border_error)
            showToast("Kategori harus dipilih")
            isValid = false
        } else {
            spinnerContainer.setBackgroundResource(R.drawable.edittext_border)
        }

        if (!isValid) {
            toggleEditMode(true)
            return
        }

        currentTransaction?.let {
            val updatedTransaction = it.copy(
                tanggal = tanggal,
                nominal = nominal.toLong(),
                kategori = kategori,
                catatan = catatan
            )
            transactionViewModel.update(updatedTransaction)
            showToast("Transaksi berhasil diperbarui")
        }
    }

    private fun showDeleteConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Konfirmasi Hapus")
            .setMessage("Apakah Anda yakin ingin menghapus transaksi ini?")
            .setPositiveButton("Ya") { _, _ ->
                currentTransaction?.let {
                    transactionViewModel.deleteTransaction(it)
                    showToast("Transaksi berhasil dihapus")
                    finish()
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun toggleEditMode(isEdit: Boolean) {
        isEditMode = isEdit

        btnEdit.visibility = if (isEdit) View.GONE else View.VISIBLE
        btnSimpan.visibility = if (isEdit) View.VISIBLE else View.GONE

        etTanggal.isEnabled = isEdit
        etNominal.isEnabled = isEdit
        etCatatan.isEnabled = isEdit

        spinnerKategori.visibility = if (isEdit) View.VISIBLE else View.GONE
        tvKategori.visibility = if (isEdit) View.GONE else View.VISIBLE

        ivCalendar.visibility = if (isEdit) View.VISIBLE else View.GONE
        ivDropdown.visibility = if (isEdit) View.VISIBLE else View.GONE
    }

    private fun setupNominalFormat() {
        etNominal.addTextChangedListener(object : TextWatcher {
            private var current = ""
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (s.toString() != current) {
                    etNominal.removeTextChangedListener(this)
                    val clean = s.toString().replace("[^\\d]".toRegex(), "")
                    if (clean.isNotEmpty()) {
                        val formatted = DecimalFormat("#,###").format(clean.toBigDecimalOrNull())
                        current = formatted
                        etNominal.setText(formatted)
                        etNominal.setSelection(formatted.length)
                    }
                    etNominal.addTextChangedListener(this)
                }
            }
        })
    }

    private fun setupSpinner() {
        val displayCategories = arrayOf(getString(R.string.income), getString(R.string.expense))
        val adapter = ArrayAdapter(this, R.layout.spinner_item, displayCategories)
        spinnerKategori.adapter = adapter

        spinnerContainer.setOnClickListener {
            if (isEditMode) spinnerKategori.performClick()
        }

        spinnerKategori.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (isEditMode) tvKategori.text = displayCategories[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupDatePicker() {
        val listener = View.OnClickListener {
            if (!isEditMode) return@OnClickListener
            val calendar = Calendar.getInstance()
            val format = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            try {
                val date = format.parse(etTanggal.text.toString())
                date?.let { calendar.time = it }
            } catch (e: Exception) {}

            DatePickerDialog(this, { _, year, month, day ->
                calendar.set(year, month, day)
                etTanggal.setText(format.format(calendar.time))
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        etTanggal.setOnClickListener(listener)
        ivCalendar.setOnClickListener(listener)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun attachBaseContext(newBase: Context) {
        val pref = AppPreferences(newBase)
        val newContext = LocaleHelper.setLocale(newBase, pref.getLanguage())
        super.attachBaseContext(newContext)
    }
}
