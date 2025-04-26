package com.example.myfinance


import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.TextView
import androidx.core.widget.addTextChangedListener
import androidx.room.Room
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class AddTransactionActivity2 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_transaction2)

        val amountInput = findViewById<EditText>(R.id.amountInput)
        val descriptionInput = findViewById<EditText>(R.id.descriptionInput)
        val labelLayout = findViewById<TextInputLayout>(R.id.labelLayout)
        val amountLayout = findViewById<TextInputLayout>(R.id.amountLayout)
        val addTransactionBtn = findViewById<Button>(R.id.addTransactionBtn)
        val spinner = findViewById<Spinner>(R.id.spinner)

        val categories = listOf("Food", "Transport", "Shopping", "Entertainment", "Others")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spinner.adapter = adapter



        amountInput.addTextChangedListener {
            if (it!!.isNotEmpty()) {
                amountLayout.error = null
            }
        }

        // Handle transaction addition
        addTransactionBtn.setOnClickListener {
            val category = spinner.selectedItem.toString()
            val amount: Double? = amountInput.text.toString().toDoubleOrNull()
            val description: String = descriptionInput.text.toString().trim()


            if (amount == null) {
                amountLayout.error = "Please enter a valid amount"
            } else {
                val transaction = Transaction(0, category, amount, description)
                insert(transaction)

            }
        }

        val closeBtn = findViewById<ImageButton>(R.id.closeBtn)

        closeBtn.setOnClickListener{
            finish()
        }
    }

    private fun addTransaction(label: String, amount: Double) {
        // Logic to store the transaction (e.g., database or local storage)
        println("Transaction Added: Label - $label, Amount - $amount")
    }

    private fun insert(transaction: Transaction){
        val  db = Room.databaseBuilder(this,
            AppDatabase::class.java,
            "transactions").build()
        GlobalScope.launch {
            db.transactionDao().insertAll(transaction)
            finish()
        }
    }
}
