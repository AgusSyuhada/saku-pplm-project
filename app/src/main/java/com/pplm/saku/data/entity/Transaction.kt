package com.pplm.saku.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transaction")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val tanggal: String,
    val nominal: Long,
    val kategori: String,
    val catatan: String
)
