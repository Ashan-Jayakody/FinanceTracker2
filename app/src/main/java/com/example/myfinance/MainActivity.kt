package com.example.myfinance

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {

    private lateinit var balance: TextView
    private lateinit var budget: TextView
    private lateinit var expense: TextView

    private lateinit var transactions: MutableList<Transaction>
    private lateinit var transactionAdapter: TransactionAdapter
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        balance = findViewById(R.id.balance)
        budget = findViewById(R.id.budget)
        expense = findViewById(R.id.expense)

        transactions = arrayListOf()

        transactionAdapter = TransactionAdapter(transactions)
        layoutManager = LinearLayoutManager(this)

        db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "transactions"
        ).build()


        val recyclerView = findViewById<RecyclerView>(R.id.recyclerview)
        recyclerView.apply {
            adapter = transactionAdapter
            layoutManager = this@MainActivity.layoutManager
        }

        val addBtn = findViewById<FloatingActionButton>(R.id.addBtn)
        addBtn.setOnClickListener {
            val intent = Intent(this, AddTransactionActivity2::class.java)
            startActivity(intent)
        }

        transactionAdapter.onDeleteClick = { transaction ->
            lifecycleScope.launch(Dispatchers.IO) {
                db.transactionDao().delete(transaction)

                val updatedTransactions = db.transactionDao().getAll()

                withContext(Dispatchers.Main) {
                    transactions.clear()
                    transactions.addAll(updatedTransactions)
                    transactionAdapter.setData(transactions)
                    updateDashboard()
                }
            }
        }

    }

    private fun fetchAll() {
        GlobalScope.launch() {
            val allTransactions = db.transactionDao().getAll()

            withContext(Dispatchers.Main) {
                transactions.clear()
                transactions.addAll(allTransactions)
                transactionAdapter.setData(transactions)
                updateDashboard()
            }
        }
    }

    private fun updateDashboard() {
        val totalAmount: Double = transactions.sumOf { it.amount }
        val budgetAmount = transactions.filter { it.amount > 0 }.sumOf { it.amount }
        val expenseAmount = totalAmount - budgetAmount

        balance.text = "$ %.2f".format(totalAmount)
        budget.text = "$ %.2f".format(budgetAmount)
        expense.text = "$ %.2f".format(expenseAmount)
    }

    override fun onResume() {
        super.onResume()
        fetchAll()
    }
}
