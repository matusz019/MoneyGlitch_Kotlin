package com.example.moneyglitch_kotlin

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.ValueFormatter
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class TrendsFragment : Fragment() {

    private lateinit var lineChart: LineChart
    private lateinit var spinnerTimeRange: Spinner
    private var transactions = listOf<Transaction>()
    private var selectedTimeRange = "This Month"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_trends, container, false)

        lineChart = view.findViewById(R.id.lineChart)
        spinnerTimeRange = view.findViewById(R.id.spinnerTimeRange)

        setupLineChart()
        setupTimeRangeSpinner()
        loadTransactionData()

        return view
    }

    private fun setupLineChart() {
        lineChart.description.isEnabled = false
        lineChart.setTouchEnabled(true)
        lineChart.setPinchZoom(true)
        lineChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        lineChart.xAxis.setDrawGridLines(false)
        lineChart.axisLeft.setDrawGridLines(false)
        lineChart.axisRight.isEnabled = false
        lineChart.xAxis.granularity = 1f  // Ensure each tick represents one transaction date
    }

    private fun setupTimeRangeSpinner() {
        val timeRangeOptions = resources.getStringArray(R.array.time_range_options)
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, timeRangeOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerTimeRange.adapter = adapter

        spinnerTimeRange.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedTimeRange = timeRangeOptions[position]
                updateLineChart()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun loadTransactionData() {
        val db = (requireActivity().application as MoneyGlitchApp).database

        lifecycleScope.launch {
            db.dao.getAllTransactionsDateDescending().collect { fetchedTransactions ->
                transactions = fetchedTransactions
                updateLineChart()
            }
        }
    }

    private fun updateLineChart() {
        if (transactions.isEmpty()) return

        val filteredTransactions = filterTransactionsByTime(transactions)

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        // Group transactions by date and sum amounts
        val groupedTransactions = filteredTransactions.groupBy { it.date }
            .mapValues { it.value.sumOf { transaction -> transaction.amount } }
            .toSortedMap()

        // Create a list of only the transaction dates
        val transactionDates = groupedTransactions.keys.toList()

        val entries = groupedTransactions.entries.toList().mapIndexed { index, entry ->
            Entry(index.toFloat(), entry.value.toFloat())
        }

        val dataSet = LineDataSet(entries, "Transaction Trends")
        dataSet.color = Color.BLUE
        dataSet.valueTextColor = Color.BLACK
        dataSet.circleRadius = 4f
        dataSet.setDrawValues(true)

        val lineData = LineData(dataSet)
        lineChart.data = lineData

        // Custom formatter to display only transaction dates on the X-axis
        lineChart.xAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                val index = value.toInt()
                return if (index in transactionDates.indices) transactionDates[index] else ""
            }
        }

        lineChart.xAxis.labelCount = transactionDates.size
        lineChart.invalidate() // Refresh chart
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
