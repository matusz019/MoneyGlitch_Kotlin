package com.example.moneyglitch_kotlin

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import kotlinx.coroutines.launch
import com.github.mikephil.charting.formatter.ValueFormatter

class BudgetFragment : Fragment() {

    private lateinit var pieChart: PieChart

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_budget, container, false)
        pieChart = view.findViewById(R.id.pieChart)
        setupPieChart()
        loadTransactionData()
        return view
    }

    private fun setupPieChart() {
        pieChart.setUsePercentValues(true)
        pieChart.description.isEnabled = false
        pieChart.setEntryLabelColor(Color.BLACK)
        pieChart.setEntryLabelTextSize(12f)
        pieChart.isDrawHoleEnabled = true
        pieChart.holeRadius = 58f
        pieChart.setHoleColor(Color.WHITE)
        pieChart.transparentCircleRadius = 61f
    }

    private fun loadTransactionData() {
        val db = (requireActivity().application as MoneyGlitchApp).database

        lifecycleScope.launch {
            db.dao.getTransactionsByType("expense").collect { transactions ->
                val categoryTotals = transactions.groupBy { it.category }
                    .mapValues { entry -> entry.value.sumOf { it.amount } }

                val entries = categoryTotals.map { (category, total) ->
                    PieEntry(total.toFloat(), category)
                }

                val dataSet = PieDataSet(entries, "Spending by Category")
                dataSet.colors = ColorTemplate.MATERIAL_COLORS.toList()

                val pieData = PieData(dataSet)
                pieData.setValueTextSize(12f)
                pieData.setValueTextColor(Color.BLACK)

                // Set Value Formatter to show percentage sign
                pieData.setValueFormatter(object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return String.format("%.1f%%", value)
                    }
                })

                pieChart.data = pieData
                pieChart.invalidate() // Refresh chart
            }
        }
    }
}
