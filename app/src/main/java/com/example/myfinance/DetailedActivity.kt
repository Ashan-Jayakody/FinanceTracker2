package com.example.myfinance

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import androidx.room.Room
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.Serializable

class DetailedActivity : AppCompatActivity() {
    private lateinit var transaction: Transaction
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_detailed)

        val transaction = intent.getSerializableExtra("transaction") as Transaction

        val labelInput = findViewById<EditText>(R.id.labelInput)
        val amountInput = findViewById<EditText>(R.id.amountInput)
        val descriptionInput = findViewById<EditText>(R.id.descriptionInput)
        val labelLayout = findViewById<TextInputLayout>(R.id.labelLayout)
        val amountLayout = findViewById<TextInputLayout>(R.id.amountLayout)
        val updateBtn = findViewById<Button>(R.id.updateBtn)
        val rootView = findViewById<View>(android.R.id.content)





        labelInput.setText(transaction.label)
        amountInput.setText(transaction.amount.toString())
        descriptionInput.setText(transaction.description)

        rootView.setOnClickListener {
            this.window.decorView.clearFocus()
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(it.windowToken, 0)
        }


        // Remove error when user starts typing
        labelInput.addTextChangedListener {
            updateBtn.visibility = View.VISIBLE
            if (it!!.isNotEmpty()) {
                labelLayout.error = null
            }
        }

        amountInput.addTextChangedListener {
            updateBtn.visibility = View.VISIBLE
            if (it!!.isNotEmpty()) {
                amountLayout.error = null
            }
        }

        descriptionInput.addTextChangedListener {
            updateBtn.visibility = View.VISIBLE
        }

        // Handle transaction addition
        updateBtn.setOnClickListener {
            val label: String = labelInput.text.toString().trim()
            val amount: Double? = amountInput.text.toString().toDoubleOrNull()
            val description: String = descriptionInput.text.toString().trim()

            if (label.isEmpty()) {
                labelLayout.error = "Please enter a valid label"
            }

            if (amount == null) {
                amountLayout.error = "Please enter a valid amount"
            } else {
                val updatedTransaction = Transaction(
                    transaction.id,
                    label,
                    amount,
                    description
                )
                update(updatedTransaction)
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

    private fun update(transaction: Transaction){
        val  db = Room.databaseBuilder(this,
            AppDatabase::class.java,
            "transactions").build()
        GlobalScope.launch {
            db.transactionDao().update(transaction)
            finish()
        }
    }

}