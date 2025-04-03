package com.example.moneyglitch_kotlin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class ShowRecurringFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    RecurringTransactionsScreen()
                }
            }
        }
    }

    @Composable
    fun RecurringTransactionsScreen() {
        val db = (requireActivity().application as MoneyGlitchApp).database
        val transactions by db.dao.getAllRecurringTransactions().collectAsState(initial = emptyList())
        var transactionToCancel by remember { mutableStateOf<Transaction?>(null) }

        LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            items(transactions) { transaction ->
                TransactionItem(transaction) {
                    transactionToCancel = transaction
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        if (transactions.isEmpty()) {
            Text(
                text = "No recurring transactions found.",
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 2,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                softWrap = true,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center

            )

        }

        if (transactionToCancel != null) {
            AlertDialog(
                onDismissRequest = { transactionToCancel = null },
                title = { Text("Cancel Recurring Transaction") },
                text = { Text("Are you sure you want to stop this recurring transaction?") },
                confirmButton = {
                    TextButton(onClick = {
                        lifecycleScope.launch {
                            val cancelled = transactionToCancel!!.copy(
                                isRecurring = false,
                                recurringInterval = null,
                                nextDueDate = null
                            )
                            db.dao.upsertTransaction(cancelled)
                            transactionToCancel = null
                            Toast.makeText(requireContext(), "Recurring transaction cancelled", Toast.LENGTH_SHORT).show()
                        }
                    }) {
                        Text("Yes")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { transactionToCancel = null }) {
                        Text("No")
                    }
                }
            )
        }
    }

    @Composable
    fun TransactionItem(transaction: Transaction, onRequestCancel: () -> Unit) {
        val backgroundColour = if (transaction.type == "income") {
            androidx.compose.ui.graphics.Color(0xFF92FC94)
        } else {
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
            ) {
                Column(modifier = Modifier.padding(0.dp)) {
                    Text(text = "Category: ${transaction.category}")
                    Text(text = "Amount: £${transaction.amount}")
                    Text(text = "Date: ${transaction.date}")
                    if (transaction.description.isNotBlank()) {
                        Text(text = "Description: ${transaction.description}")
                    }
                    Text("Recurring Interval: ${transaction.recurringInterval}")
                }
                Image(
                    painter = painterResource(id = R.drawable.cancel_recurring_icon),
                    contentDescription = "Cancel Recurring Transaction",
                    modifier = Modifier
                        .size(32.dp)
                        .clickable(onClick = onRequestCancel)
                )
            }
        }
    }
}
