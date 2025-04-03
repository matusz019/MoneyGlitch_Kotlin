package com.example.moneyglitch_kotlin

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a single financial transaction.
 *
 * @property id Auto-generated unique identifier for the transaction.
 * @property amount The monetary value of the transaction.
 * @property date The date the transaction occurred, in yyyy-MM-dd format.
 * @property description Optional note about the transaction.
 * @property category Category under which this transaction is classified.
 * @property type Type of transaction, either "income" or "expense".
 */

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val amount: Double,
    val date: String,
    val description: String,
    val category: String,
    val type: String,  // "income" or "expense"
    val recurring: Boolean = false,
    val recurringInterval: String? = null,
    val nextDueDate: String? = null
)
