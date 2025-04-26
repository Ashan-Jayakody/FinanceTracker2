package com.example.myfinance


import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
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

        val labelInput = findViewById<EditText>(R.id.labelInput)
        val amountInput = findViewById<EditText>(R.id.amountInput)
        val descriptionInput = findViewById<EditText>(R.id.descriptionInput)
        val labelLayout = findViewById<TextInputLayout>(R.id.labelLayout)
        val amountLayout = findViewById<TextInputLayout>(R.id.amountLayout)
        val addTransactionBtn = findViewById<Button>(R.id.addTransactionBtn)


        // Remove error when user starts typing
        labelInput.addTextChangedListener {
            if (it!!.isNotEmpty()) {
                labelLayout.error = null
            }
        }

        amountInput.addTextChangedListener {
            if (it!!.isNotEmpty()) {
                amountLayout.error = null
            }
        }

        // Handle transaction addition
        addTransactionBtn.setOnClickListener {
            val label: String = labelInput.text.toString().trim()
            val amount: Double? = amountInput.text.toString().toDoubleOrNull()
            val description: String = descriptionInput.text.toString().trim()

            if (label.isEmpty()) {
                labelLayout.error = "Please enter a valid label"
            }

            if (amount == null) {
                amountLayout.error = "Please enter a valid amount"
            } else {
                val transaction = Transaction(0, label, amount, description)
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
