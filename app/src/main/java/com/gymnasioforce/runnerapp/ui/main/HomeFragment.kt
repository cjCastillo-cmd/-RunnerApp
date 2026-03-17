package com.gymnasioforce.runnerapp.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.gymnasioforce.runnerapp.R
import com.gymnasioforce.runnerapp.databinding.FragmentHomeBinding
import com.gymnasioforce.runnerapp.network.Run
import com.gymnasioforce.runnerapp.ui.run.RunDetailActivity
import com.gymnasioforce.runnerapp.ui.run.RunningActivity
import com.gymnasioforce.runnerapp.utils.Prefs
import com.gymnasioforce.runnerapp.utils.showToast

class HomeFragment : Fragment() {

    private lateinit var b: FragmentHomeBinding
    private val viewModel: HomeViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View {
        b = FragmentHomeBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, state: Bundle?) {
        super.onViewCreated(view, state)
        b.tvUserName.text = Prefs(requireContext()).userName.uppercase()
        b.rvRecentRuns.layoutManager = LinearLayoutManager(requireContext())
        b.btnStart.setOnClickListener {
            startActivity(Intent(requireContext(), RunningActivity::class.java))
        }

        b.swipeRefresh.setColorSchemeColors(requireContext().getColor(R.color.volt))
        b.swipeRefresh.setProgressBackgroundColorSchemeColor(requireContext().getColor(R.color.surface))
        b.swipeRefresh.setOnRefreshListener { viewModel.loadData() }

        observeViewModel()
        if (viewModel.runs.value.isNullOrEmpty()) {
            viewModel.loadData()
        }
    }

    private fun observeViewModel() {
        viewModel.stats.observe(viewLifecycleOwner) { stats ->
            stats ?: return@observe
            b.tvTotalKm.text = "%.1f".format(stats.totalKm)
            b.tvTotalRuns.text = "${stats.totalRuns}"
            b.tvTotalKcal.text = "${stats.totalCalories}"
            val p = stats.avgPace
            b.tvAvgPace.text = if (p > 0) "${p.toInt()}:${"%02d".format(((p % 1) * 60).toInt())}" else "-"
        }

        viewModel.runs.observe(viewLifecycleOwner) { runs ->
            showRuns(runs)
        }

        viewModel.loading.observe(viewLifecycleOwner) { loading ->
            b.swipeRefresh.isRefreshing = loading
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            if (!error.isNullOrEmpty() && viewModel.loading.value == false) {
                showToast(getString(R.string.error_loading_data))
            }
        }
    }

    private fun showRuns(runs: List<Run>) {
        if (runs.isEmpty()) {
            b.tvEmptyRuns.visibility = View.VISIBLE
            b.rvRecentRuns.visibility = View.GONE
        } else {
            b.tvEmptyRuns.visibility = View.GONE
            b.rvRecentRuns.visibility = View.VISIBLE
            b.rvRecentRuns.adapter = RunHistoryAdapter(runs) { run ->
                val intent = Intent(requireContext(), RunDetailActivity::class.java).apply {
                    putExtra("run_id", run.id)
                    putExtra("distance_km", run.distanceKm)
                    putExtra("duration_sec", run.durationSec)
                    putExtra("calories", run.calories)
                    putExtra("avg_pace", run.avgPace ?: 0.0)
                    putExtra("route_json", run.routeJson)
                    putExtra("created_at", run.createdAt)
                }
                startActivity(intent)
            }
        }
    }
}
