package com.example.moneyglitch_kotlin

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Room database class for managing transaction-related data.
 * It provides an instance of the [TransactionDao] to perform database operations.
 *
 * @constructor This class should not be instantiated directly; use [getDatabase] instead.
 */
@Database(entities = [Transaction::class],
    version = 1)
abstract class TransactionDatabase : RoomDatabase() {

    /**
     * Provides access to DAO (Data Access Object) methods for transaction operations.
     *
     * @return An implementation of [TransactionDao].
     */
    abstract val dao: TransactionDao

    companion object {
        @Volatile
        private var INSTANCE: TransactionDatabase? = null

        /**
         * Returns a singleton instance of the [TransactionDatabase].
         * If it doesn't exist, it creates a new one.
         *
         * @param context The application context.
         * @return A singleton instance of [TransactionDatabase].
         */
        fun getDatabase(context: Context): TransactionDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TransactionDatabase::class.java,
                    "transaction_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
