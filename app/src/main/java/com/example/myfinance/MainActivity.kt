package com.example.myfinance

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Dispatchers
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

    private var currentBudget: Double = 0.0
    private var totalExpense: Double = 0.0

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

        val setBudgetButton = findViewById<Button>(R.id.setBudgetButton)
        val addBtn = findViewById<FloatingActionButton>(R.id.addBtn)

        setBudgetButton.setOnClickListener {
            showSetBudgetDialog()
        }

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

        // Load budget when app starts
        loadBudget()
    }

    override fun onResume() {
        super.onResume()
        fetchAll()
    }

    private fun fetchAll() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val allTransactions = db.transactionDao().getAll()

                withContext(Dispatchers.Main) {
                    try {
                        transactions.clear()
                        transactions.addAll(allTransactions)
                        transactionAdapter.setData(transactions)
                        updateDashboard()
                        showCategorySummary()
                    } catch (e: Exception) {
                        Toast.makeText(this@MainActivity, "Error updating UI: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Error fetching transactions: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun updateDashboard() {
        try {
            val totalIncome = transactions.filter { it.amount > 0 }.sumOf { it.amount }
            totalExpense = transactions.filter { it.amount < 0 }.sumOf { kotlin.math.abs(it.amount) }

            // Update totalBalance based on currentBudget and totalExpense
            val totalBalance = currentBudget - totalExpense

            expense.text = "$${"%.2f".format(totalExpense)}"
            balance.text = "$${"%.2f".format(totalBalance)}"

            // Check if budget is exceeded
            if (totalExpense > currentBudget) {
                showBudgetExceededWarning(totalExpense, currentBudget)
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error updating dashboard: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showBudgetExceededWarning(totalExpense: Double, budget: Double) {
        val exceededAmount = totalExpense - budget
        val message = "Warning: You have exceeded your budget by $${"%.2f".format(exceededAmount)}!\n\n" +
                     "Total Expenses: $${"%.2f".format(totalExpense)}\n" +
                     "Budget Limit: $${"%.2f".format(budget)}"

        AlertDialog.Builder(this)
            .setTitle("Budget Exceeded!")
            .setMessage(message)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    //display categorical wise expenses
    private fun showCategorySummary() {
        try {
            val categorySummary = transactions
                .filter { it.amount < 0 }
                .groupBy { it.label }
                .mapValues { entry -> entry.value.sumOf { kotlin.math.abs(it.amount) } }

            val summaryText = categorySummary.entries.joinToString("\n\n") { (category, total) ->
                "$category: $${"%.2f".format(total)}"
            }

            val summaryTextView = findViewById<TextView>(R.id.summaryTextView)
            summaryTextView.text = summaryText
        } catch (e: Exception) {
            Toast.makeText(this, "Error showing category summary: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showSetBudgetDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Set New Budget")

        val input = EditText(this)
        input.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        builder.setView(input)

        builder.setPositiveButton("Save") { dialog, _ ->
            val newBudget = input.text.toString().toDoubleOrNull()
            if (newBudget != null) {
                updateBudget(newBudget)
            } else {
                Toast.makeText(this, "Invalid budget", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }

        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }

        builder.show()
    }

    private fun updateBudget(newBudget: Double) {
        try {
            currentBudget = newBudget

            // Save to SharedPreferences
            val sharedPref = getSharedPreferences("MyFinancePrefs", MODE_PRIVATE)
            with(sharedPref.edit()) {
                putFloat("budget", newBudget.toFloat())
                apply()
            }

            budget.text = "$${"%.2f".format(currentBudget)}"
        } catch (e: Exception) {
            Toast.makeText(this, "Error updating budget: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadBudget() {
        try {
            val sharedPref = getSharedPreferences("MyFinancePrefs", MODE_PRIVATE)
            currentBudget = sharedPref.getFloat("budget", 0f).toDouble()

            budget.text = "$${"%.2f".format(currentBudget)}"
        } catch (e: Exception) {
            Toast.makeText(this, "Error loading budget: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
