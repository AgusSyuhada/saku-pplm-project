package com.pplm.saku.ui.home.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import com.pplm.saku.R
import com.pplm.saku.data.AppDatabase
import com.pplm.saku.databinding.FragmentHomeBinding
import com.pplm.saku.databinding.LayoutCustomTabBinding
import com.pplm.saku.repository.TransactionRepository
import com.pplm.saku.ui.adapter.TransactionAdapter
import com.pplm.saku.utils.formatRupiah
import com.pplm.saku.viewmodel.HomeViewModel
import com.pplm.saku.viewmodel.HomeViewModelFactory
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: HomeViewModel
    private lateinit var recentAdapter: TransactionAdapter

    private val tabTitles by lazy {
        listOf(
            getString(R.string.tab_all),
            getString(R.string.tab_today),
            getString(R.string.tab_month),
            getString(R.string.tab_year)
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val db = AppDatabase.getDatabase(requireContext())
        val repository = TransactionRepository(db.transactionDao())

        viewModel = ViewModelProvider(
            this,
            HomeViewModelFactory(repository)
        )[HomeViewModel::class.java]

        setupCustomTabs()

        binding.recyclerRecentTransaction.layoutManager = LinearLayoutManager(requireContext())
        recentAdapter = TransactionAdapter(emptyList(), requireContext()) { id ->
            viewModel.deleteTransactionById(id)
        }
        binding.recyclerRecentTransaction.adapter = recentAdapter

        observeViewModel()
        viewModel.loadData("semua")

        binding.tabTimeRange.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val filter = when (tab?.position) {
                    0 -> "semua"
                    1 -> "hari_ini"
                    2 -> "bulan_ini"
                    3 -> "tahun_ini"
                    else -> "semua"
                }
                viewModel.loadData(filter)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.totalPemasukan.observe(viewLifecycleOwner) { total ->
                        binding.tvTotalPemasukan.text = total.formatRupiah()
                    }
                }
                launch {
                    viewModel.totalPengeluaran.observe(viewLifecycleOwner) { total ->
                        binding.tvTotalPengeluaran.text = total.formatRupiah()
                    }
                }
                launch {
                    viewModel.selisih.observe(viewLifecycleOwner) { value ->
                        val formattedSelisih = when {
                            value > 0 -> "+${value.formatRupiah()}"
                            value < 0 -> "-${(-value).formatRupiah()}"
                            else -> value.formatRupiah()
                        }
                        binding.tvSelisih.text = formattedSelisih
                        binding.tvSelisih.setTextColor(
                            when {
                                value > 0 -> ContextCompat.getColor(requireContext(), android.R.color.holo_green_dark)
                                value < 0 -> ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark)
                                else -> ContextCompat.getColor(requireContext(), R.color.black)
                            }
                        )
                    }
                }
                launch {
                    viewModel.lastThreeTransactions.observe(viewLifecycleOwner) { transactions ->
                        recentAdapter.updateData(transactions)
                    }
                }
            }
        }
    }

    private fun setupCustomTabs() {
        binding.tabTimeRange.removeAllTabs()

        for (title in tabTitles) {
            val tab = binding.tabTimeRange.newTab()
            val customView = LayoutCustomTabBinding.inflate(layoutInflater).root
            (customView as? TextView)?.text = title
            tab.customView = customView
            binding.tabTimeRange.addTab(tab)
        }

        binding.tabTimeRange.getTabAt(0)?.select()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
