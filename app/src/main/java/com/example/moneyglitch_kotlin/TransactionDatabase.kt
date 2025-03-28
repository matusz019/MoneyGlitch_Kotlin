package com.example.moneyglitch_kotlin

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [Transaction::class],
    version = 1
)
abstract class TransactionDatabase: RoomDatabase() {
    abstract val dao: TransactionDao
}