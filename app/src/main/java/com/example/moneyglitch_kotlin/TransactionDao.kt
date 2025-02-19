package com.example.moneyglitch_kotlin

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Upsert
    suspend fun upsertTransaction(transaction: Transaction)

    @Delete
    suspend fun deleteTransaction(transaction: Transaction)

    @Query("SELECT * FROM transactions WHERE type = :type")
    fun getTransactionsByType(type: String): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions")
    fun getAllTransactions(): Flow<List<Transaction>>




}