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
import android.widget.RadioGroup
import android.widget.RadioButton
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.lifecycle.lifecycleScope

class AddTransactionActivity2 : AppCompatActivity() {
    private lateinit var amountInput: EditText
    private lateinit var descriptionInput: EditText
    private lateinit var spinner: Spinner
    private lateinit var transactionTypeGroup: RadioGroup
    private lateinit var expenseRadio: RadioButton
    private lateinit var incomeRadio: RadioButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_transaction2)

        // Initialize views
        amountInput = findViewById(R.id.amountInput)
        descriptionInput = findViewById(R.id.descriptionInput)
        spinner = findViewById(R.id.spinner)
        transactionTypeGroup = findViewById(R.id.transactionTypeGroup)
        expenseRadio = findViewById(R.id.expenseRadio)
        incomeRadio = findViewById(R.id.incomeRadio)

        val labelLayout = findViewById<TextInputLayout>(R.id.labelLayout)
        val amountLayout = findViewById<TextInputLayout>(R.id.amountLayout)
        val addTransactionBtn = findViewById<Button>(R.id.addTransactionBtn)

        // Set up spinner
        val categories = arrayOf("Food", "Transport", "Entertainment", "Shopping", "Bills", "Other")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        amountInput.addTextChangedListener {
            if (it!!.isNotEmpty()) {
                amountLayout.error = null
            }
        }

        // Set up close button
        findViewById<ImageButton>(R.id.closeBtn).setOnClickListener {
            finish()
        }

        // Set up add transaction button
        addTransactionBtn.setOnClickListener {
            saveTransaction()
        }
    }

    private fun saveTransaction() {
        val amount = amountInput.text.toString().toDoubleOrNull()
        val description = descriptionInput.text.toString()
        val category = spinner.selectedItem.toString()

        if (amount == null || description.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Modify amount based on transaction type
        val finalAmount = if (expenseRadio.isChecked) -amount else amount

        val transaction = Transaction(
            amount = finalAmount,
            description = description,
            label = category
        )

        val db = Room.databaseBuilder(this,
            AppDatabase::class.java,
            "transactions").build()
        lifecycleScope.launch(Dispatchers.IO) {
            db.transactionDao().insertAll(transaction)
            withContext(Dispatchers.Main) {
                finish()
            }
        }
    }
}
