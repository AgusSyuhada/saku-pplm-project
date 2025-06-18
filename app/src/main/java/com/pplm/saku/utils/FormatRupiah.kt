package com.pplm.saku.utils

import java.text.NumberFormat
import java.util.Locale

fun Long.formatRupiah(): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    return formatter.format(this)
}
