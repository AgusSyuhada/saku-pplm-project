package com.pplm.saku.ui.adapter

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.pplm.saku.R
import com.pplm.saku.data.entity.Transaction
import com.pplm.saku.databinding.ItemTransactionBinding
import com.pplm.saku.ui.detail.DetailTransactionActivity
import java.text.NumberFormat
import java.util.Locale

class TransactionAdapter(
    private var transactionList: List<Transaction>,
    private val context: Context,
    private val onDelete: (Long) -> Unit
) : RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newList: List<Transaction>) {
        transactionList = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val binding = ItemTransactionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TransactionViewHolder(binding)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val transaction = transactionList[position]
        val isIncome = transaction.kategori.equals("Pemasukan", true)

        val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        val formattedAmount = currencyFormatter.format(transaction.nominal.toDouble())

        holder.binding.tvNominal.apply {
            text = if (isIncome) "+$formattedAmount" else "-$formattedAmount"
            setTextColor(
                context.getColor(
                    if (isIncome) android.R.color.holo_green_dark else android.R.color.holo_red_dark
                )
            )
        }

        val categoryText = when (transaction.kategori) {
            "Pemasukan" -> context.getString(R.string.income)
            "Pengeluaran" -> context.getString(R.string.expense)
            else -> transaction.kategori
        }

        holder.binding.tvKategori.text = categoryText
        holder.binding.tvTanggal.text = transaction.tanggal

        holder.binding.root.setOnClickListener {
            val intent = Intent(context, DetailTransactionActivity::class.java).apply {
                putExtra("TRANSACTION_ID", transaction.id)
            }
            context.startActivity(intent)
        }

        holder.binding.ivDelete.setOnClickListener {
            AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.delete_transaction_title))
                .setMessage(context.getString(R.string.delete_transaction_message))
                .setPositiveButton(context.getString(R.string.yes)) { _, _ ->
                    onDelete(transaction.id)
                }
                .setNegativeButton(context.getString(R.string.cancel), null)
                .show()
        }
    }

    override fun getItemCount(): Int = transactionList.size

    class TransactionViewHolder(val binding: ItemTransactionBinding) : RecyclerView.ViewHolder(binding.root)
}
