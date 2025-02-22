package com.example.moneyglitch_kotlin

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class TrendsFragment : Fragment() {

    private lateinit var lineChart: LineChart

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout which contains the LineChart
        val view = inflater.inflate(R.layout.fragment_trends, container, false)
        lineChart = view.findViewById(R.id.lineChart)
        setupChartData()
        return view
    }

    private fun setupChartData() {
        // Get your database instance from your Application class
        val db = (requireActivity().application as MoneyGlitchApp).database

        lifecycleScope.launch {
            // Collect transactions sorted by date (you might want to sort ascending)
            db.dao.getAllTransactionsDateDescending().collect { transactions ->
                // Sort transactions by date ascending
                val sortedTransactions = transactions.sortedBy { it.date }
                val entries = mutableListOf<Entry>()
                var cumulativeAmount = 0f

                sortedTransactions.forEachIndexed { index, transaction ->
                    // For income add, for expense subtract
                    val amount = transaction.amount.toFloat()
                    cumulativeAmount += if (transaction.type == "income") amount else -amount
                    // Here we use the index as the x-value; for more precision convert dates to timestamps.
                    entries.add(Entry(index.toFloat(), cumulativeAmount))
                }

                val dataSet = LineDataSet(entries, "Money Over Time").apply {
                    color = Color.BLUE
                    setDrawCircles(true)
                    circleRadius = 4f
                    setDrawValues(false)
                }
                val lineData = LineData(dataSet)
                lineChart.data = lineData
                lineChart.invalidate()  // Refresh chart
            }
        }
    }
}
