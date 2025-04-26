package com.example.myfinance

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable


@Entity(tableName = "transaction_table")
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val label: String,
    val amount: Double,
    val description: String): Serializable{

    }


