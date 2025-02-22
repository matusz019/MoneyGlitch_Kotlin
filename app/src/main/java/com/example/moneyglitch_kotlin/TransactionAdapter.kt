package com.example.moneyglitch_kotlin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.core.content.ContextCompat

class TransactionAdapter(
    private var transactions: List<Transaction>,
    private val onRemove: (Transaction) -> Unit
) : RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    class TransactionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtCategory: TextView = view.findViewById(R.id.txtCategory)
        val txtDescription: TextView = view.findViewById(R.id.txtDescription)
        val txtAmount: TextView = view.findViewById(R.id.txtAmount)
        val txtDate: TextView = view.findViewById(R.id.txtDate)
        val btnRemove: ImageButton = view.findViewById(R.id.btnRemove)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val transaction = transactions[position]
        holder.txtCategory.text = transaction.category
        holder.txtDescription.text = transaction.description
        holder.txtAmount.text = "£${transaction.amount}"
        holder.txtDate.text = transaction.date

        // Set background color based on transaction type
        val colorRes = if (transaction.type == "income") {
            R.color.income_colour  // Ensure this color is defined in your colors.xml
        } else {
            R.color.expense_colour // Ensure this color is defined in your colors.xml
        }
        val color = ContextCompat.getColor(holder.itemView.context, colorRes)
        holder.itemView.setBackgroundColor(color)

        // Set remove button click listener
        holder.btnRemove.setOnClickListener {
            onRemove(transaction)
        }
    }

    override fun getItemCount(): Int = transactions.size

    fun updateData(newTransactions: List<Transaction>) {
        transactions = newTransactions
        notifyDataSetChanged()
    }
}

