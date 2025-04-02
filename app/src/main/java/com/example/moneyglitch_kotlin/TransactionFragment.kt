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
 * Fragment that provides a form for user to input new transactions.
 */
class TransactionFragment : Fragment() {

    private var transactionType: String = "income"

    /**
     * Initializes the compose view for the transaction form
     *
     * @param inflater The LayoutInflater object.
     * @param container Optional parent container.
     * @param savedInstanceState Previously saved state, if any.
     * @return The composed view of the transaction form.
     */
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
     * Composable function that renders the transaction form.
     * Users can enter amount, date, category, and description.
     * Submitting the form inserts a transaction into the database.
     */
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun TransactionForm() {
        val db = (requireActivity().application as MoneyGlitchApp).database

        var amount by remember { mutableStateOf("") }
        var date by remember { mutableStateOf("") }
        var description by remember { mutableStateOf("") }
        var selectedCategory by remember { mutableStateOf("") }

        val context = LocalContext.current
        val categoryArrayRes = if (transactionType == "income") R.array.income_categories else R.array.expense_categories
        val categories = context.resources.getStringArray(categoryArrayRes)


        Column(modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)) {

            //Amount
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Enter Amount") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Category Dropdown
            var expanded by remember { mutableStateOf(false) }

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = selectedCategory,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Category") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                    },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    categories.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category) },
                            onClick = {
                                selectedCategory = category
                                expanded = false
                            }
                        )
                    }
                }
            }


            Spacer(modifier = Modifier.height(16.dp))

            // Date picker
            OutlinedTextField(
                value = date,
                onValueChange = {},
                label = { Text("Select Date") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        showDatePicker { picked ->
                            date = picked
                        }
                    },
                readOnly = true,
                enabled = false,
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            //Description
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description (Optional)") },
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

                    val transaction = Transaction(
                        amount = amount.toDoubleOrNull() ?: 0.0,
                        date = date,
                        description = description,
                        category = selectedCategory,
                        type = transactionType
                    )

                    lifecycleScope.launch(Dispatchers.IO) {
                        db.dao.upsertTransaction(transaction)
                    }

                    Toast.makeText(requireContext(), "$transactionType saved!", Toast.LENGTH_SHORT).show()

                    // Reset form
                    amount = ""
                    date = ""
                    description = ""
                    selectedCategory = ""
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Add ${transactionType.replaceFirstChar {
                    if (it.isLowerCase()) it.titlecase(
                        Locale.getDefault()
                    ) else it.toString()
                }}")
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

