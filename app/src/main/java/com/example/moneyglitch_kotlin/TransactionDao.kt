package com.example.moneyglitch_kotlin

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for the `transactions` table.
 * Defines methods to perform CRUD operations on transaction data.
 */
@Dao
interface TransactionDao {

    /**
     * Inserts a new transaction or updates an existing one based on primary key.
     *
     * @param transaction The transaction to insert or update.
     */
    @Upsert
    suspend fun upsertTransaction(transaction: Transaction)

    /**
     * Deletes a transaction from the database.
     *
     * @param transaction The transaction to delete.
     */
    @Delete
    suspend fun deleteTransaction(transaction: Transaction)

    /**
     * Retrieves all transactions from the database of a specific type.
     *
     * @return A [Flow] emitting a list of all transactions of a specific type.
     */
    @Query("SELECT * FROM transactions WHERE type = :type")
    fun getTransactionsByType(type: String): Flow<List<Transaction>>

    /**
     * Retrieves all transactions from the database sorted by date in descending order.
     *
     * @return A [Flow] emitting a list of transactions ordered by newest first.
     */
    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllTransactionsDateDescending(): Flow<List<Transaction>>

    /**
     * Returns the total number of transactions stored in the database.
     *
     * @return The count of all transactions as an [Int].
     */
    @Query("SELECT COUNT(*) FROM transactions")
    suspend fun getTransactionCount(): Int

    /**
     * Retrieves all recurring transactions from the database.
     *
     * @return A [Flow] emitting a list of all recurring transactions.
     */
    @Query("SELECT * FROM transactions WHERE isRecurring = 1")
    suspend fun getAllRecurringTransactions(): List<Transaction>

    /**
     * Inserts a new transactions or updates a existing ones based on primary key.
     * Used for loading sample data.
     * @param transactions The list of transactions to be inserted or updated.
     */
    @Upsert
    suspend fun upsertTransactions(transactions: List<Transaction>)


}