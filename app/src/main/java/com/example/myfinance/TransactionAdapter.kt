package com.example.myfinance

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class TransactionAdapter(private var transactions:MutableList<Transaction>) :
    RecyclerView.Adapter<TransactionAdapter.TransactionHolder>() {

    class TransactionHolder(view: View) : RecyclerView.ViewHolder(view) {
        val label: TextView = view.findViewById(R.id.label)
        val amount: TextView = view.findViewById(R.id.amount)
        val deleteBtn: View = view.findViewById(R.id.deleteBtn)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.transaction_layout, parent, false)
        return TransactionHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionHolder, position: Int) {
        val transaction = transactions[position]
        val context = holder.amount.context



        if (transaction.amount >= 0) {
            holder.amount.text = "+ \$%.2f".format(transaction.amount)
            holder.amount.setTextColor(ContextCompat.getColor(context, R.color.green))
        } else {
            holder.amount.text = "- \$%.2f".format(Math.abs(transaction.amount))
            holder.amount.setTextColor(ContextCompat.getColor(context, R.color.red))
        }
        holder.label.text = transaction.label

        holder.itemView.setOnClickListener{
            val intent = Intent(context, DetailedActivity::class.java)
            intent.putExtra("transaction", transaction)
            context.startActivity(intent)
        }

        holder.deleteBtn.setOnClickListener {
            onDeleteClick?.invoke(transaction)
        }
    }




    override fun getItemCount(): Int {
        return transactions.size
    }

    // Function to update list
    fun setData(transactions: MutableList<Transaction>){
        this.transactions = transactions
        notifyDataSetChanged()
    }

    // Lambda to handle delete click
    var onDeleteClick: ((Transaction) -> Unit)? = null

    fun deleteItem(transaction: Transaction) {
        val position = transactions.indexOf(transaction)
        if (position != -1) {
            transactions.removeAt(position)
            notifyItemRemoved(position)
        }
    }

}
