package com.example.moneyglitch_kotlin

import android.app.Application
import androidx.room.Room
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import sampleTransactions
import java.text.SimpleDateFormat
import java.util.*

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
            processDueRecurringTransactions(database.dao)
        }
    }

    /**
     * Checks for due recurring transactions and inserts them when appropriate.
     * This runs once at app startup from a background coroutine.
     */
    private suspend fun processDueRecurringTransactions(dao: TransactionDao) {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        dao.getAllRecurringTransactions().forEach { transaction ->
            if (transaction.nextDueDate != null && transaction.nextDueDate <= today) {
                val newTxn = transaction.copy(
                    id = 0,
                    date = transaction.nextDueDate,
                    isRecurring = false,
                    recurringInterval = null,
                    nextDueDate = null
                )
                dao.upsertTransaction(newTxn)

                // Calculate next due date
                val cal = Calendar.getInstance().apply {
                    time = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(transaction.nextDueDate)!!
                }

                when (transaction.recurringInterval) {
                    "daily" -> cal.add(Calendar.DAY_OF_MONTH, 1)
                    "weekly" -> cal.add(Calendar.WEEK_OF_YEAR, 1)
                    "monthly" -> cal.add(Calendar.MONTH, 1)
                }

                val updatedTxn = transaction.copy(
                    nextDueDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
                )

                dao.upsertTransaction(updatedTxn)
            }
        }
    }
}
