package com.pplm.saku.ui.home.history

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import com.google.android.material.textfield.TextInputEditText
import com.pplm.saku.R
import com.pplm.saku.data.entity.Transaction
import com.pplm.saku.ui.adapter.TransactionAdapter
import com.pplm.saku.viewmodel.TransactionViewModel
import com.pplm.saku.viewmodel.TransactionViewModelFactory
import java.text.SimpleDateFormat
import java.util.*

class HistoryFragment : Fragment() {

    private lateinit var adapter: TransactionAdapter
    private lateinit var viewModel: TransactionViewModel
    private lateinit var tabLayout: TabLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var btnFilterCustom: ImageButton
    private var transactionList: List<Transaction> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_history, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tabLayout = view.findViewById(R.id.tabLayoutFilter)
        recyclerView = view.findViewById(R.id.recyclerViewTransaction)
        btnFilterCustom = view.findViewById(R.id.btnFilterCustom)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = TransactionAdapter(emptyList(), requireContext()) { id ->
            viewModel.deleteTransactionById(id)
            viewModel.fetchAllTransactions()
        }
        recyclerView.adapter = adapter

        val factory = TransactionViewModelFactory(requireActivity().application)
        viewModel = ViewModelProvider(this, factory)[TransactionViewModel::class.java]

        viewModel.transactions.observe(viewLifecycleOwner) {
            transactionList = it
            val sortedList = it.sortedByDescending { trans ->
                SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).parse(trans.tanggal)
            }
            adapter.updateData(sortedList)
        }

        viewModel.fetchAllTransactions()

        setupCustomTabs()

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val filter = when (tab?.position) {
                    0 -> "semua"
                    1 -> "hari_ini"
                    2 -> "bulan_ini"
                    3 -> "tahun_ini"
                    else -> "semua"
                }
                filterTransactions(filter)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        btnFilterCustom.setOnClickListener { showFilterDialog() }
    }

    private fun setupCustomTabs() {
        tabLayout.removeAllTabs()

        val tabTitles = listOf(
            getString(R.string.tab_all),
            getString(R.string.tab_today),
            getString(R.string.tab_month),
            getString(R.string.tab_year)
        )

        tabTitles.forEach { title ->
            val tab = tabLayout.newTab()
            val customView = LayoutInflater.from(requireContext())
                .inflate(R.layout.layout_custom_tab, null) as TextView
            customView.text = title
            tab.customView = customView
            tabLayout.addTab(tab)
        }
        tabLayout.getTabAt(0)?.select()
    }

    private fun showFilterDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_filter, null)
        val dialog = android.app.AlertDialog.Builder(requireContext()).setView(dialogView).create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()

        val etStartDate = dialogView.findViewById<TextInputEditText>(R.id.etStartDate)
        val etEndDate = dialogView.findViewById<TextInputEditText>(R.id.etEndDate)
        val radioGroup = dialogView.findViewById<RadioGroup>(R.id.radioGroupTransactionType)
        val btnApply = dialogView.findViewById<Button>(R.id.btnApplyFilter)
        val btnClose = dialogView.findViewById<ImageButton>(R.id.btnCloseFilter)

        setupDatePickers(etStartDate, etEndDate)

        btnApply.setOnClickListener {
            val start = etStartDate.text.toString()
            val end = etEndDate.text.toString()
            val type = when (radioGroup.checkedRadioButtonId) {
                R.id.radioIncome -> getString(R.string.income)
                R.id.radioExpense -> getString(R.string.expense)
                else -> getString(R.string.tab_all)
            }
            filterTransactionsCustom(start, end, type)
            dialog.dismiss()
        }

        btnClose.setOnClickListener { dialog.dismiss() }
    }

    private fun setupDatePickers(etStartDate: TextInputEditText, etEndDate: TextInputEditText) {
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val currentDate = Calendar.getInstance().time
        etStartDate.setText(dateFormat.format(currentDate))
        etEndDate.setText(dateFormat.format(currentDate))

        val datePickerListener = { target: TextInputEditText ->
            val calendar = Calendar.getInstance()
            DatePickerDialog(requireContext(),
                { _, y, m, d ->
                    calendar.set(y, m, d)
                    target.setText(dateFormat.format(calendar.time))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        etStartDate.setOnClickListener { datePickerListener(etStartDate) }
        etEndDate.setOnClickListener { datePickerListener(etEndDate) }
    }

    private fun filterTransactionsCustom(startStr: String, endStr: String, type: String) {
        val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val sdfAlt = SimpleDateFormat("d-M-yyyy", Locale.getDefault())

        val startDate = parseDate(startStr, sdf, sdfAlt)
        val endDate = parseDate(endStr, sdf, sdfAlt)

        if (startDate == null || endDate == null) {
            Toast.makeText(context, getString(R.string.invalid_date_format), Toast.LENGTH_SHORT).show()
            return
        }

        val startOfDay = Calendar.getInstance().apply {
            time = startDate
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }.time

        val endOfDay = Calendar.getInstance().apply {
            time = endDate
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
        }.time

        val filtered = transactionList.filter { trans ->
            val transDate = parseDate(trans.tanggal, sdf, sdfAlt)
            val inRange = transDate != null && !transDate.before(startOfDay) && !transDate.after(endOfDay)
            val matchType = when (type) {
                "pemasukan" -> trans.kategori.equals("pemasukan", true)
                "pengeluaran" -> trans.kategori.equals("pengeluaran", true)
                else -> true
            }
            inRange && matchType
        }

        adapter.updateData(filtered.sortedByDescending {
            parseDate(it.tanggal, sdf, sdfAlt)
        })

        Toast.makeText(
            context,
            getString(R.string.filter_applied, startStr, endStr, type),
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun parseDate(dateStr: String, sdf: SimpleDateFormat, sdfAlt: SimpleDateFormat): Date? {
        return try {
            sdf.parse(dateStr) ?: sdfAlt.parse(dateStr)
        } catch (e: Exception) {
            null
        }
    }

    private fun filterTransactions(filter: String) {
        if (transactionList.isEmpty()) return

        val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val now = Calendar.getInstance()

        val filtered = transactionList.filter { trans ->
            val transDate = sdf.parse(trans.tanggal)
            val cal = Calendar.getInstance().apply { time = transDate ?: Date() }

            when (filter) {
                "hari_ini" -> now.get(Calendar.YEAR) == cal.get(Calendar.YEAR) &&
                        now.get(Calendar.DAY_OF_YEAR) == cal.get(Calendar.DAY_OF_YEAR)
                "bulan_ini" -> now.get(Calendar.YEAR) == cal.get(Calendar.YEAR) &&
                        now.get(Calendar.MONTH) == cal.get(Calendar.MONTH)
                "tahun_ini" -> now.get(Calendar.YEAR) == cal.get(Calendar.YEAR)
                else -> true
            }
        }

        adapter.updateData(filtered.sortedByDescending {
            sdf.parse(it.tanggal)
        })
    }

    override fun onResume() {
        super.onResume()
        setupCustomTabs()
        viewModel.fetchAllTransactions()
    }
}
