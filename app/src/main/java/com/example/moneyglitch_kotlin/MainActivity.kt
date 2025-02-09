package com.example.moneyglitch_kotlin

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNavigationView: BottomNavigationView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        bottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigationView.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.bottom_home -> {
                    replaceFragment(HomeFragment())
                    true
                }
                R.id.bottom_budget -> {
                    replaceFragment(BudgetFragment())
                    true
                }
                R.id.bottom_trends -> {
                    replaceFragment(TrendsFragment())
                    true
                }
                else -> false
            }
        }

        replaceFragment(HomeFragment())
        bottomNavigationView.selectedItemId = R.id.bottom_home

        val fab: FloatingActionButton = findViewById(R.id.fab)
        val fabMenu: LinearLayout = findViewById(R.id.fab_menu_container)
        var isFabMenuOpen = false

        fab.setOnClickListener {
            isFabMenuOpen = if (isFabMenuOpen) {
                fabMenu.visibility = View.GONE
                false
            } else {
                fabMenu.visibility = View.VISIBLE
                true
            }
        }

        val fabIncome: View = findViewById(R.id.btn_option_income)
        fabIncome.setOnClickListener {
            openTransactionFragment("income")
            clearBottomNavSelection()
        }

        val fabExpense: View = findViewById(R.id.btn_option_expense)
        fabExpense.setOnClickListener {
            openTransactionFragment("expense")
            clearBottomNavSelection()
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.frame_container, fragment)
            .commit()
    }

    private fun clearBottomNavSelection() {
        bottomNavigationView.menu.setGroupCheckable(0, true, false) // Temporarily disable checkable state
        for (i in 0 until bottomNavigationView.menu.size()) {
            bottomNavigationView.menu.getItem(i).isChecked = false // Uncheck all items
        }
        bottomNavigationView.menu.setGroupCheckable(0, true, true) // Re-enable checkable state
    }

    private fun openTransactionFragment(type: String) {
        val fragment = TransactionFragment()
        val bundle = Bundle()
        bundle.putString("type", type)  // Attach "type" to bundle
        fragment.arguments = bundle

        supportFragmentManager.beginTransaction()
            .replace(R.id.frame_container, fragment)
            .addToBackStack(null)  // Allows back navigation
            .commit()
    }

}
