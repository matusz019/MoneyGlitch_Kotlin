import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.moneyglitch_kotlin.MoneyGlitchApp
import com.example.moneyglitch_kotlin.R
import com.example.moneyglitch_kotlin.TransactionAdapter
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TransactionAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        recyclerView = view.findViewById(R.id.recyclerViewTransactions)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Pass the remove callback to the adapter
        adapter = TransactionAdapter(emptyList()) { transaction ->
            // Remove transaction from the database in a coroutine
            lifecycleScope.launch {
                (requireActivity().application as MoneyGlitchApp).database.dao.deleteTransaction(transaction)
            }
        }
        recyclerView.adapter = adapter
        fetchTransactions()
        return view
    }

    private fun fetchTransactions() {
        val db = (requireActivity().application as MoneyGlitchApp).database

        lifecycleScope.launch {
            db.dao.getAllTransactionsDateDescending().collect { transactions ->
                adapter.updateData(transactions)
            }
        }
    }
}
