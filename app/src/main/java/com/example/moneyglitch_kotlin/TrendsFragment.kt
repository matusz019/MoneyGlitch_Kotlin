package com.example.moneyglitch_kotlin

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.ValueFormatter
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * Fragment displaying trends in transaction data using a line chart.
 * It allows users to filter transaction data by different time ranges and view a graphical summary.
 */

class TrendsFragment : Fragment() {

    private var allTransactions by mutableStateOf(emptyList<Transaction>())
    private var selectedTimeRange by mutableStateOf("This Month")

    /**
     * Initializes the fragment view using Jetpack Compose, fetching transaction data from the database.
     *
     * @param inflater The LayoutInflater object.
     * @param container Optional parent view.
     * @param savedInstanceState Previously saved state, if any.
     * @return The composed view of this fragment.
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val db = (requireActivity().application as MoneyGlitchApp).database

        lifecycleScope.launch {
            db.dao.getAllTransactionsDateDescending().collect { transactions ->
                allTransactions = transactions
            }
        }

        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    TrendsScreen()
                }
            }
        }
    }


    /**
     * Displays a screen with a time range dropdown and a line chart of transaction amounts over time.
     * It uses Compose to layout the UI and MPAndroidChart to display the graph.
     */
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun TrendsScreen() {
        val context = LocalContext.current

        val filtered = remember(allTransactions, selectedTimeRange) {
            filterTransactionsByTime(allTransactions)
        }

        // Group by date and sum
        val grouped = filtered.groupBy { it.date }
            .mapValues { it.value.sumOf { txn -> txn.amount } }
            .toSortedMap()

        val dates = grouped.keys.toList()
        val entries = grouped.entries.mapIndexed { index, entry ->
            Entry(index.toFloat(), entry.value.toFloat())
        }

        Column(modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)) {

            // Time Range Dropdown
            val timeOptions = listOf("This Month", "Last Month", "Last 6 Months", "Last Year")
            var expanded by remember { mutableStateOf(false) }

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = selectedTimeRange,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Time Range") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    timeOptions.forEach {
                        DropdownMenuItem(
                            text = { Text(it) },
                            onClick = {
                                selectedTimeRange = it
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Line Chart
            AndroidView(
                factory = { LineChart(context) },
                update = { chart ->
                    val dataSet = LineDataSet(entries, "Transaction Trends").apply {
                        color = Color.BLUE
                        valueTextColor = Color.BLACK
                        circleRadius = 4f
                        setDrawValues(true)
                    }

                    val lineData = LineData(dataSet)

                    chart.data = lineData
                    chart.description.isEnabled = false
                    chart.setTouchEnabled(true)
                    chart.setPinchZoom(true)
                    chart.axisRight.isEnabled = false
                    chart.axisLeft.setDrawGridLines(false)
                    chart.xAxis.setDrawGridLines(false)
                    chart.xAxis.position = XAxis.XAxisPosition.BOTTOM
                    chart.xAxis.granularity = 1f

                    // Show actual dates on x-axis
                    chart.xAxis.valueFormatter = object : ValueFormatter() {
                        override fun getFormattedValue(value: Float): String {
                            val index = value.toInt()
                            return dates.getOrNull(index) ?: ""
                        }
                    }

                    chart.invalidate()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
            )
        }
    }


    /**
     * Filters a list of transactions based on the selected time range.
     *
     * @param transactions The list of transactions to filter.
     * @return The filtered list of transactions.
     */
    private fun filterTransactionsByTime(transactions: List<Transaction>): List<Transaction> {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        return when (selectedTimeRange) {
            "This Month" -> {
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                val startDate = dateFormat.format(calendar.time)
                transactions.filter { it.date >= startDate }
            }
            "Last Month" -> {
                calendar.add(Calendar.MONTH, -1)
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                val startDate = dateFormat.format(calendar.time)
                calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
                val endDate = dateFormat.format(calendar.time)
                transactions.filter { it.date in startDate..endDate }
            }
            "Last 6 Months" -> {
                calendar.add(Calendar.MONTH, -6)
                val startDate = dateFormat.format(calendar.time)
                transactions.filter { it.date >= startDate }
            }
            "Last Year" -> {
                calendar.add(Calendar.YEAR, -1)
                val startDate = dateFormat.format(calendar.time)
                transactions.filter { it.date >= startDate }
            }
            else -> transactions
        }
    }
}
