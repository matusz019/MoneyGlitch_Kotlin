package com.example.moneyglitch_kotlin

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface TransactionDao {

    @Upsert
    suspend fun upsertTransaction(transaction: Transaction)

    @Delete
    suspend fun deleteTransaction(transaction: Transaction)

    @Query("SELECT * FROM transactions WHERE type = :type")
    suspend fun getTransactionsByType(type: String): List<Transaction>

    @Query("SELECT * FROM transactions")
    suspend fun getAllTransactions(): List<Transaction>




}