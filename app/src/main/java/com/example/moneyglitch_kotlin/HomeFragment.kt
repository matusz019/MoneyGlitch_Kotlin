package com.example.moneyglitch_kotlin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    this@HomeFragment.HomeScreen()
                }
            }
        }
    }

    @Composable
    fun HomeScreen() {
        val db = (requireActivity().application as MoneyGlitchApp).database
        val transactionsFlow = remember { db.dao.getAllTransactionsDateDescending() }
        val transactions by transactionsFlow.collectAsState(initial = emptyList())

        LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            items(transactions) { transaction ->
                TransactionItem(transaction) {
                    lifecycleScope.launch {
                        db.dao.deleteTransaction(transaction)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }

    @Composable
    fun TransactionItem(transaction: Transaction, onRemove: () -> Unit) {

        val backgroundColour = if (transaction.type == "income"){
            androidx.compose.ui.graphics.Color(0xFF92FC94)
        }else{
            androidx.compose.ui.graphics.Color(0xFFFF99A1)
        }
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = backgroundColour),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ){
                Column(modifier = Modifier.padding(0.dp)) {
                    Text(
                        text = "Category: ${transaction.category}"
                    )
                    Text(
                        text = "Amount: £${transaction.amount}"
                    )
                    Text(
                        text = "Date: ${transaction.date}"
                    )
                    if (transaction.description.isNotBlank()) {
                        Text(
                            text = "Description: ${transaction.description}"
                        )
                    }
                }
                Image(
                    painter = painterResource(id = R.drawable.ic_delete),
                    contentDescription = "Delete",
                    modifier = Modifier
                        .size(32.dp)
                        .clickable(onClick = onRemove)
                )
            }
        }
    }


}
