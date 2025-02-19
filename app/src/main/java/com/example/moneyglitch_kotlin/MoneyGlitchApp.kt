package com.example.moneyglitch_kotlin

import android.app.Application
import androidx.room.Room

class MoneyGlitchApp : Application() {
    lateinit var database: TransactionDatabase

    override fun onCreate() {
        super.onCreate()
        database = Room.databaseBuilder(
            applicationContext,
            TransactionDatabase::class.java,
            "transaction_db"
        ).build()
    }
}