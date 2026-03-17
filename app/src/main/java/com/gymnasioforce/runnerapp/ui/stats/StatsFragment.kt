package com.gymnasioforce.runnerapp.ui.stats

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.gymnasioforce.runnerapp.R
import com.gymnasioforce.runnerapp.databinding.FragmentStatsBinding
import com.gymnasioforce.runnerapp.network.RetrofitClient
import com.gymnasioforce.runnerapp.utils.showToast
import kotlinx.coroutines.launch

class StatsFragment : Fragment() {

    private lateinit var b: FragmentStatsBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View {
        b = FragmentStatsBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, state: Bundle?) {
        super.onViewCreated(view, state)
        loadStats()
    }

    private fun loadStats() {
        lifecycleScope.launch {
            try {
                val monthly = RetrofitClient.api.getMonthlyStats().body()?.data
                monthly?.let {
                    b.tvMonthKm.text = "%.1f".format(it.totalKm)
                    b.tvMonthKcal.text = "${it.totalCalories}"
                    b.tvMonthRuns.text = "${it.totalRuns}"
                    val p = it.avgPace
                    b.tvMonthPace.text = if (p > 0) "${p.toInt()}:${"%02d".format(((p % 1) * 60).toInt())}" else "-"
                }

                val weekly = RetrofitClient.api.getWeeklyCompare().body()?.data
                weekly?.let {
                    b.tvWeekKm.text = "%.1f".format(it.currentWeekKm)
                    val sign = if (it.differenceKm >= 0) "+" else ""
                    b.tvWeekKmDiff.text = "${sign}%.1f km".format(it.differenceKm)
                }

                val monthlyComp = RetrofitClient.api.getMonthlyCompare().body()?.data
                monthlyComp?.let {
                    val sign = if (it.differenceKm >= 0) "+" else ""
                    b.tvMonthKmDiff.text = "${sign}%.1f km".format(it.differenceKm)
                }
            } catch (e: Exception) { showToast(getString(R.string.error_loading_stats)) }
        }
    }
}
