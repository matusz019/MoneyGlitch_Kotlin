package com.example.moneyglitch_kotlin

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class BudgetFragment : Fragment() {

    private var allTransactions by mutableStateOf(emptyList<Transaction>())
    private var selectedTimeRange by mutableStateOf("This Month")
    private var categoryFilters by mutableStateOf(setOf<String>())

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val db = (requireActivity().application as MoneyGlitchApp).database

        lifecycleScope.launch {
            db.dao.getAllTransactionsDateDescending().collect { transactions ->
                allTransactions = transactions
                val expenseCategories = transactions.filter { it.type == "expense" }.map { it.category }.toSet()
                categoryFilters = expenseCategories
            }
        }

        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    BudgetScreen()
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun BudgetScreen() {
        val context = LocalContext.current
        val filteredTransactions = remember(allTransactions, selectedTimeRange, categoryFilters) {
            filterTransactionsByTime(allTransactions).filter {
                it.type == "expense" && it.category in categoryFilters
            }
        }

        val totalIncome = filterTransactionsByTime(allTransactions).filter { it.type == "income" }.sumOf { it.amount }
        val totalExpense = filteredTransactions.sumOf { it.amount }
        val totalChange = totalIncome - totalExpense

        val availableCategories = allTransactions.filter { it.type == "expense" }.map { it.category }.toSet()

        Column(modifier = Modifier.padding(16.dp)) {

            // Time Range Dropdown
            var expanded by remember { mutableStateOf(false) }
            val timeOptions = listOf("This Month", "Last Month", "Last 6 Months", "Last Year")

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = selectedTimeRange,
                    onValueChange = {},
                    label = { Text("Time Range") },
                    readOnly = true,
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

            // Pie Chart
            AndroidView(
                factory = { PieChart(context) },
                update = { chart ->
                    val categoryTotals = filteredTransactions.groupBy { it.category }
                        .mapValues { entry -> entry.value.sumOf { it.amount } }

                    val entries = categoryTotals.map { (category, total) ->
                        PieEntry(total.toFloat(), category)
                    }

                    val dataSet = PieDataSet(entries, "Spending by Category").apply {
                        colors = ColorTemplate.MATERIAL_COLORS.toList()
                    }

                    val pieData = PieData(dataSet).apply {
                        setValueTextSize(12f)
                        setValueTextColor(Color.BLACK)
                        setValueFormatter(object : ValueFormatter() {
                            override fun getFormattedValue(value: Float): String {
                                return "%.1f%%".format(value)
                            }
                        })
                    }

                    chart.data = pieData
                    chart.setUsePercentValues(true)
                    chart.description.isEnabled = false
                    chart.setEntryLabelColor(Color.BLACK)
                    chart.setEntryLabelTextSize(12f)
                    chart.invalidate()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Totals
            Text("Total Income: £%.2f".format(totalIncome), color = androidx.compose.ui.graphics.Color(0xFF388E3C))
            Text("Total Spent: £%.2f".format(totalExpense), color = androidx.compose.ui.graphics.Color(0xFFD32F2F))
            Text(
                "Total Change: £%.2f".format(totalChange),
                color = if (totalChange >= 0) androidx.compose.ui.graphics.Color(0xFF388E3C)
                else androidx.compose.ui.graphics.Color(0xFFD32F2F)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Filters
            Text("Filter by Category:", style = MaterialTheme.typography.titleMedium)
            LazyColumn {
                items(availableCategories.toList()) { category ->
                    val isChecked = category in categoryFilters
                    Row(
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Checkbox(
                            checked = isChecked,
                            onCheckedChange = {
                                categoryFilters = if (it) {
                                    categoryFilters + category
                                } else {
                                    categoryFilters - category
                                }
                            }
                        )
                        Text(text = category)
                    }
                }
            }
        }
    }

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
