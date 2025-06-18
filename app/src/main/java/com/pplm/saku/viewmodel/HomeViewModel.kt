package com.pplm.saku.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pplm.saku.data.entity.Transaction
import com.pplm.saku.repository.TransactionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HomeViewModel(private val repository: TransactionRepository) : ViewModel() {

    private val _totalPemasukan = MutableLiveData<Long>()
    val totalPemasukan: LiveData<Long> = _totalPemasukan

    private val _totalPengeluaran = MutableLiveData<Long>()
    val totalPengeluaran: LiveData<Long> = _totalPengeluaran

    private val _selisih = MutableLiveData<Long>()
    val selisih: LiveData<Long> = _selisih

    private val _lastThreeTransactions = MutableLiveData<List<Transaction>>()
    val lastThreeTransactions: LiveData<List<Transaction>> = _lastThreeTransactions

    fun deleteTransactionById(id: Long) = viewModelScope.launch(Dispatchers.IO) {
        repository.deleteById(id)
    }

    init {
        loadRecentTransactions()
    }

    fun loadData(filter: String = "semua") {
        when (filter) {
            "semua" -> loadAllData()
            "hari_ini" -> loadTodayData()
            "bulan_ini" -> loadMonthData()
            "tahun_ini" -> loadYearData()
        }
    }

    private fun loadRecentTransactions() {
        viewModelScope.launch {
            _lastThreeTransactions.value = repository.getLastTransactions(3)
        }
    }

    private fun loadAllData() {
        viewModelScope.launch {
            val pemasukan = repository.getTotalPemasukan()
            val pengeluaran = repository.getTotalPengeluaran()

            _totalPemasukan.value = pemasukan
            _totalPengeluaran.value = pengeluaran
            _selisih.value = pemasukan - pengeluaran
        }
    }

    private fun loadTodayData() {
        viewModelScope.launch {
            val pemasukan = repository.getTodayPemasukan()
            val pengeluaran = repository.getTodayPengeluaran()

            _totalPemasukan.value = pemasukan
            _totalPengeluaran.value = pengeluaran
            _selisih.value = pemasukan - pengeluaran
        }
    }

    private fun loadMonthData() {
        viewModelScope.launch {
            val pemasukan = repository.getThisMonthPemasukan()
            val pengeluaran = repository.getThisMonthPengeluaran()

            _totalPemasukan.value = pemasukan
            _totalPengeluaran.value = pengeluaran
            _selisih.value = pemasukan - pengeluaran
        }
    }

    private fun loadYearData() {
        viewModelScope.launch {
            val pemasukan = repository.getThisYearPemasukan()
            val pengeluaran = repository.getThisYearPengeluaran()

            _totalPemasukan.value = pemasukan
            _totalPengeluaran.value = pengeluaran
            _selisih.value = pemasukan - pengeluaran
        }
    }
}