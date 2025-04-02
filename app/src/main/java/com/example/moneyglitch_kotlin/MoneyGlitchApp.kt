package com.example.moneyglitch_kotlin

import android.app.Application
import androidx.room.Room
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import sampleTransactions


/**
 * Custom [Application] class for initializing global application state.
 *
 * This class creates a singleton instance of the [TransactionDatabase],
 * which is shared across the app for database operations.
 */
class MoneyGlitchApp : Application() {
    lateinit var database: TransactionDatabase

    override fun onCreate() {
        super.onCreate()
        database = Room.databaseBuilder(
            applicationContext,
            TransactionDatabase::class.java,
            "transaction_db"
        ).build()

        // Load sample data if DB is empty
        CoroutineScope(Dispatchers.IO).launch {
            val existing = database.dao.getTransactionCount()
            if (existing == 0) {
                database.dao.upsertTransactions(sampleTransactions)
            }
        }
    }
}