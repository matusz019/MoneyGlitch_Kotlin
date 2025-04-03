package com.example.moneyglitch_kotlin

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * Fragment that provides a form for the user to input new recurring transactions.
 */
class RecurringTransactionFragment : Fragment() {

    private var transactionType: String = "income"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        transactionType = arguments?.getString("type", "income") ?: "income"

        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    TransactionForm()
                }
            }
        }
    }

    /**
     * Composable function that renders the recurring transaction form.
     */
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun TransactionForm() {
        val db = (requireActivity().application as MoneyGlitchApp).database

        var amount by remember { mutableStateOf("") }
        var date by remember { mutableStateOf("") }
        var description by remember { mutableStateOf("") }
        var selectedCategory by remember { mutableStateOf("") }
        var repeatInterval by remember { mutableStateOf("None") }
        var occurrences by remember { mutableStateOf("1") }

        val context = LocalContext.current
        val categories = context.resources.getStringArray(
            if (transactionType == "income") R.array.income_categories else R.array.expense_categories
        )

        val intervalOptions = listOf("Daily", "Weekly", "Monthly")

        Column(modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)) {

            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Enter Amount") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Category Dropdown
            var categoryExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(expanded = categoryExpanded, onExpandedChange = { categoryExpanded = !categoryExpanded }) {
                OutlinedTextField(
                    value = selectedCategory,
                    onValueChange = {},
                    label = { Text("Category") },
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = categoryExpanded,
                    onDismissRequest = { categoryExpanded = false }
                ) {
                    categories.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category) },
                            onClick = {
                                selectedCategory = category
                                categoryExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Date Picker
            OutlinedTextField(
                value = date,
                onValueChange = {},
                label = { Text("Start Date") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        showDatePicker { picked -> date = picked }
                    },
                readOnly = true,
                enabled = false,
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Repeat Interval
            var intervalExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(expanded = intervalExpanded, onExpandedChange = { intervalExpanded = !intervalExpanded }) {
                OutlinedTextField(
                    value = repeatInterval,
                    onValueChange = {},
                    label = { Text("Repeat") },
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = intervalExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = intervalExpanded,
                    onDismissRequest = { intervalExpanded = false }
                ) {
                    intervalOptions.forEach {
                        DropdownMenuItem(
                            text = { Text(it) },
                            onClick = {
                                repeatInterval = it
                                intervalExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Number of Occurrences
            OutlinedTextField(
                value = occurrences,
                onValueChange = { occurrences = it },
                label = { Text("Occurrences") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (amount.isBlank() || date.isBlank() || selectedCategory.isBlank()) {
                        Toast.makeText(requireContext(), "Amount, Category and Date are required", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    val baseDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(date)
                    val cal = Calendar.getInstance().apply { time = baseDate!! }

                    val totalOccurrences = occurrences.toIntOrNull() ?: 1

                    lifecycleScope.launch(Dispatchers.IO) {
                        repeat(totalOccurrences) {
                            val txn = Transaction(
                                amount = amount.toDoubleOrNull() ?: 0.0,
                                date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time),
                                description = description,
                                category = selectedCategory,
                                type = transactionType
                            )
                            db.dao.upsertTransaction(txn)

                            when (repeatInterval) {
                                "Daily" -> cal.add(Calendar.DAY_OF_MONTH, 1)
                                "Weekly" -> cal.add(Calendar.WEEK_OF_YEAR, 1)
                                "Monthly" -> cal.add(Calendar.MONTH, 1)
                            }
                        }
                    }

                    Toast.makeText(requireContext(), "Recurring $transactionType saved!", Toast.LENGTH_SHORT).show()

                    amount = ""
                    date = ""
                    description = ""
                    selectedCategory = ""
                    repeatInterval = "None"
                    occurrences = "1"
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Add Recurring ${transactionType.replaceFirstChar { it.uppercase() }}")
            }
        }
    }

    /**
     * Displays a date picker dialog and returns the selected date in yyyy-MM-dd format.
     *
     * @param onDateSelected Callback function to return the selected date.
     */
    private fun showDatePicker(onDateSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        val datePicker = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val selectedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    .format(Calendar.getInstance().apply { set(year, month, dayOfMonth) }.time)
                onDateSelected(selectedDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePicker.show()
    }
}

