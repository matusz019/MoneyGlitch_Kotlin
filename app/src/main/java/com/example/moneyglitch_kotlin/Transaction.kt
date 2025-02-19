package com.example.moneyglitch_kotlin

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val amount: Double,
    val date: String,
    val description: String,
    val category: String,
    val type: String  // "income" or "expense"
)
