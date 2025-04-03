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


/**
 * Fragment that displays a list of recent transactions
 * Users can view transaction details and delete individual entries.
 */
class ShowRecurringFragment : Fragment() {



    /**
     * Inflates the Compose UI content inside the fragment.
     *
     * @param inflater The LayoutInflater object that can be used to inflate views.
     * @param container If non-null, this is the parent view that the fragment's UI should attach to.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state.
     * @return The ComposeView to render.
     */
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

    /**
     * Composable function that renders the full list of transactions in a scrollable column.
     * It observes transaction data from the database and updates the UI reactively.
     */
    @Composable
    fun RecurringTransactionsScreen() {
        val db = (requireActivity().application as MoneyGlitchApp).database
        val transactions by db.dao.getAllRecurringTransactions().collectAsState(initial = emptyList())


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

    /**
     * Composable that renders a single transaction entry inside a [Card].
     * The card color varies based on the transaction type (income or expense),
     * and includes a delete icon that removes the transaction when tapped.
     *
     * @param transaction The transaction to display.
     * @param onRemove Callback to execute when the delete icon is pressed.
     */
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
                    painter = painterResource(id = R.drawable.cancel_recurring_icon),
                    contentDescription = "Cancel Recurring Transaction",
                    modifier = Modifier
                        .size(32.dp)
                        .clickable(onClick = onRemove)
                )
            }
        }
    }


}
