package com.example.moneyglitch_kotlin

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class TransactionFragment : Fragment() {

    private lateinit var etAmount: EditText
    private lateinit var etDate: EditText
    private lateinit var etDescription: EditText
    private lateinit var spinnerCategory: Spinner
    private lateinit var btnSubmit: Button
    private var transactionType: String = "income"  // Default type

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_transaction, container, false)

        // Get Transaction Type ("income" or "expense")
        transactionType = arguments?.getString("type", "income") ?: "income"

        // Initialize UI
        etAmount = view.findViewById(R.id.et_amount)
        etDate = view.findViewById(R.id.et_date)
        etDescription = view.findViewById(R.id.et_description)
        spinnerCategory = view.findViewById(R.id.spinner_category)
        btnSubmit = view.findViewById(R.id.btn_submit)

        setupFormBasedOnType()

        // Date Picker
        etDate.setOnClickListener {
            showDatePicker()
        }

        // Submit Button Click
        btnSubmit.setOnClickListener {
            if (validateForm()) {
                saveTransaction()
            }
        }

        return view
    }

    private fun setupFormBasedOnType() {
        if (transactionType == "income") {
            btnSubmit.text = "Add Income"
            val incomeCategories = arrayOf("Salary", "Scholarship", "Part-time Job", "Gift", "Other")
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, incomeCategories)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerCategory.adapter = adapter
        } else {
            btnSubmit.text = "Add Expense"
            val expenseCategories = arrayOf("Rent", "Food", "Transport", "Shopping", "Bills", "Other")
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, expenseCategories)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerCategory.adapter = adapter
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val datePicker = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val selectedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    .format(Calendar.getInstance().apply { set(year, month, dayOfMonth) }.time)
                etDate.setText(selectedDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePicker.show()
    }

    private fun validateForm(): Boolean {
        var isValid = true

        if (etAmount.text.toString().trim().isEmpty()) {
            etAmount.error = "Amount is required"
            isValid = false
        }

        if (etDate.text.toString().trim().isEmpty()) {
            etDate.error = "Date is required"
            isValid = false
        }

        return isValid
    }

    private fun saveTransaction() {
        val amount = etAmount.text.toString().toDoubleOrNull() ?: 0.0
        val date = etDate.text.toString()
        val description = etDescription.text.toString()
        val category = spinnerCategory.selectedItem.toString()

        Toast.makeText(requireContext(), "$transactionType saved: $amount - $category", Toast.LENGTH_SHORT).show()

        // Reset form
        etAmount.text.clear()
        etDate.text.clear()
        etDescription.text.clear()
        spinnerCategory.setSelection(0)
    }
}