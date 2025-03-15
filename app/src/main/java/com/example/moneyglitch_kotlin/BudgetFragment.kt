package com.example.moneyglitch_kotlin

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import com.github.mikephil.charting.formatter.ValueFormatter
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class BudgetFragment : Fragment() {

    private lateinit var pieChart: PieChart
    private lateinit var categoryContainer: LinearLayout
    private lateinit var spinnerTimeRange: Spinner

    private var categoryFilters = mutableSetOf<String>()
    private var allCategories = mutableSetOf<String>()
    private var transactions = listOf<Transaction>()
    private var selectedTimeRange = "This Month"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_budget, container, false)

        pieChart = view.findViewById(R.id.pieChart)
        categoryContainer = view.findViewById(R.id.categoryContainer)
        spinnerTimeRange = view.findViewById(R.id.spinnerTimeRange)

        setupPieChart()
        setupTimeRangeSpinner()
        loadTransactionData()

        return view
    }

    private fun setupPieChart() {
        pieChart.setUsePercentValues(true)
        pieChart.description.isEnabled = false
        pieChart.setEntryLabelColor(Color.BLACK)
        pieChart.setEntryLabelTextSize(12f)
        pieChart.setDrawHoleEnabled(true)
        pieChart.holeRadius = 58f
        pieChart.setHoleColor(Color.WHITE)
        pieChart.transparentCircleRadius = 61f
    }

    private fun setupTimeRangeSpinner() {
        val timeRangeOptions = resources.getStringArray(R.array.time_range_options)
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, timeRangeOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerTimeRange.adapter = adapter

        spinnerTimeRange.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedTimeRange = timeRangeOptions[position]
                updatePieChart()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun loadTransactionData() {
        val db = (requireActivity().application as MoneyGlitchApp).database

        lifecycleScope.launch {
            db.dao.getTransactionsByType("expense").collect { fetchedTransactions ->
                transactions = fetchedTransactions
                allCategories = transactions.map { it.category }.toMutableSet()
                categoryFilters.addAll(allCategories)

                setupCategoryFilters()
                updatePieChart()
            }
        }
    }

    private fun setupCategoryFilters() {
        categoryContainer.removeAllViews()

        allCategories.forEach { category ->
            val checkBox = CheckBox(requireContext()).apply {
                text = category
                isChecked = true
                setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        categoryFilters.add(category)
                    } else {
                        categoryFilters.remove(category)
                    }
                    updatePieChart()
                }
            }
            categoryContainer.addView(checkBox)
        }
    }

    private fun updatePieChart() {
        val filteredTransactions = filterTransactionsByTime(transactions).filter { it.category in categoryFilters }

        val categoryTotals = filteredTransactions.groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount } }

        val entries = categoryTotals.map { (category, total) ->
            PieEntry(total.toFloat(), category)
        }

        val dataSet = PieDataSet(entries, "Spending by Category")
        dataSet.colors = ColorTemplate.MATERIAL_COLORS.toList()

        val pieData = PieData(dataSet)
        pieData.setValueTextSize(12f)
        pieData.setValueTextColor(Color.BLACK)

        pieData.setValueFormatter(object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return String.format("%.1f%%", value)
            }
        })

        pieChart.data = pieData
        pieChart.invalidate()
    }

    private fun filterTransactionsByTime(transactions: List<Transaction>): List<Transaction> {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        val filteredTransactions = when (selectedTimeRange) {
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

        return filteredTransactions
    }
}
