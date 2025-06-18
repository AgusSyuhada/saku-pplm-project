package com.pplm.saku.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pplm.saku.data.AppDatabase
import com.pplm.saku.data.entity.Transaction
import com.pplm.saku.repository.TransactionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TransactionViewModel(application: Application) : AndroidViewModel(application) {

    private val repo: TransactionRepository

    init {
        val db = AppDatabase.getDatabase(application)
        repo = TransactionRepository(db.transactionDao())
    }

    fun insert(transaction: Transaction) {
        viewModelScope.launch {
            repo.insert(transaction)
        }
    }

    fun update(transaction: Transaction) = viewModelScope.launch(Dispatchers.IO) {
        repo.update(transaction)
    }

    fun getTransaction(id: Long): LiveData<Transaction> {
        return repo.getById(id)
    }

    fun deleteTransactionById(id: Long) = viewModelScope.launch(Dispatchers.IO) {
        repo.deleteById(id)
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repo.deleteTransaction(transaction)
        }
    }

    private val _transactions = MutableLiveData<List<Transaction>>()
    val transactions: LiveData<List<Transaction>> = _transactions

    fun fetchAllTransactions() {
        viewModelScope.launch {
            val allData = repo.getAll()
            _transactions.postValue(allData)
        }
    }
}

