package com.pplm.saku.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.pplm.saku.data.entity.Transaction

@Dao
interface TransactionDao {
    @Insert
    suspend fun insert(transaction: Transaction)

    @Update
    suspend fun update(transaction: Transaction)

    @Query("SELECT * FROM `transaction` ORDER BY id DESC")
    suspend fun getAll(): List<Transaction>

    @Query("SELECT * FROM `transaction` WHERE id = :id")
    fun getById(id: Long): LiveData<Transaction>

    @Query("SELECT * FROM `transaction` WHERE tanggal = :tanggal ORDER BY id DESC")
    suspend fun getByDate(tanggal: String): List<Transaction>

    @Query("SELECT * FROM `transaction` WHERE tanggal BETWEEN :startDate AND :endDate ORDER BY id DESC")
    suspend fun getByDateRange(startDate: String, endDate: String): List<Transaction>

    @Query("DELETE FROM `transaction` WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Delete
    suspend fun delete(transaction: Transaction)

    @Query("DELETE FROM `transaction`")
    suspend fun deleteAll()

    @Query("SELECT * FROM `transaction` ORDER BY tanggal DESC LIMIT :limit")
    suspend fun getLastTransactions(limit: Int): List<Transaction>

}