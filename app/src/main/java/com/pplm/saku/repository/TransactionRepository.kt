package com.pplm.saku.repository

import androidx.lifecycle.LiveData
import com.pplm.saku.data.dao.TransactionDao
import com.pplm.saku.data.entity.Transaction
import java.text.SimpleDateFormat
import java.util.*

class TransactionRepository(private val transactionDao: TransactionDao) {

    suspend fun insert(transaction: Transaction) = transactionDao.insert(transaction)

    suspend fun update(transaction: Transaction) = transactionDao.update(transaction)

    fun getById(id: Long): LiveData<Transaction> = transactionDao.getById(id)

    suspend fun getAll() = transactionDao.getAll()

    suspend fun deleteById(id: Long) = transactionDao.deleteById(id)

    suspend fun deleteTransaction(transaction: Transaction) {
        transactionDao.delete(transaction)
    }

    suspend fun deleteAll() = transactionDao.deleteAll()

    suspend fun getTotalPemasukan(): Long {
        return transactionDao.getAll()
            .filter { it.kategori == "Pemasukan" }
            .sumOf { it.nominal }
    }

    suspend fun getTotalPengeluaran(): Long {
        return transactionDao.getAll()
            .filter { it.kategori == "Pengeluaran" }
            .sumOf { it.nominal }
    }

    suspend fun getTotalPemasukanForDate(date: String): Long {
        return transactionDao.getByDate(date)
            .filter { it.kategori == "Pemasukan" }
            .sumOf { it.nominal }
    }

    suspend fun getTotalPengeluaranForDate(date: String): Long {
        return transactionDao.getByDate(date)
            .filter { it.kategori == "Pengeluaran" }
            .sumOf { it.nominal }
    }

    suspend fun getTotalPemasukanForRange(startDate: String, endDate: String): Long {
        return transactionDao.getByDateRange(startDate, endDate)
            .filter { it.kategori == "Pemasukan" }
            .sumOf { it.nominal }
    }

    suspend fun getTotalPengeluaranForRange(startDate: String, endDate: String): Long {
        return transactionDao.getByDateRange(startDate, endDate)
            .filter { it.kategori == "Pengeluaran" }
            .sumOf { it.nominal }
    }

    suspend fun getLastTransactions(limit: Int): List<Transaction> {
        return transactionDao.getLastTransactions(limit)
    }

    suspend fun getTodayPemasukan(): Long {
        val today = getTodayDate()
        return getTotalPemasukanForDate(today)
    }

    suspend fun getTodayPengeluaran(): Long {
        val today = getTodayDate()
        return getTotalPengeluaranForDate(today)
    }

    suspend fun getThisMonth(): List<Transaction> {
        val (start, end) = getStartAndEndOfMonth()
        return transactionDao.getByDateRange(start, end)
    }

    suspend fun getThisMonthPemasukan(): Long {
        val (start, end) = getStartAndEndOfMonth()
        return getTotalPemasukanForRange(start, end)
    }

    suspend fun getThisMonthPengeluaran(): Long {
        val (start, end) = getStartAndEndOfMonth()
        return getTotalPengeluaranForRange(start, end)
    }

    suspend fun getThisYearPemasukan(): Long {
        val (start, end) = getStartAndEndOfYear()
        return getTotalPemasukanForRange(start, end)
    }

    suspend fun getThisYearPengeluaran(): Long {
        val (start, end) = getStartAndEndOfYear()
        return getTotalPengeluaranForRange(start, end)
    }

    private fun getTodayDate(): String {
        val formatter = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        return formatter.format(Date())
    }

    private fun getStartAndEndOfMonth(): Pair<String, String> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        val start = calendar.time
        calendar.add(Calendar.MONTH, 1)
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.add(Calendar.DATE, -1)
        val end = calendar.time

        val formatter = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        return formatter.format(start) to formatter.format(end)
    }

    private fun getStartAndEndOfYear(): Pair<String, String> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.MONTH, 0)
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        val start = calendar.time
        calendar.set(Calendar.MONTH, 11)
        calendar.set(Calendar.DAY_OF_MONTH, 31)
        val end = calendar.time

        val formatter = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        return formatter.format(start) to formatter.format(end)
    }
}