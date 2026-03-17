package com.gymnasioforce.runnerapp.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.gymnasioforce.runnerapp.data.local.AppDatabase
import com.gymnasioforce.runnerapp.data.local.RunEntity
import com.gymnasioforce.runnerapp.R
import com.gymnasioforce.runnerapp.databinding.FragmentHomeBinding
import com.gymnasioforce.runnerapp.network.RetrofitClient
import com.gymnasioforce.runnerapp.network.Run
import com.gymnasioforce.runnerapp.ui.run.RunDetailActivity
import com.gymnasioforce.runnerapp.ui.run.RunningActivity
import com.gymnasioforce.runnerapp.utils.Prefs
import com.gymnasioforce.runnerapp.utils.showToast
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private lateinit var b: FragmentHomeBinding
    private val db by lazy { AppDatabase.getInstance(requireContext()) }

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
        loadStats()
        loadRecentRuns()

        b.swipeRefresh.setColorSchemeColors(requireContext().getColor(R.color.volt))
        b.swipeRefresh.setProgressBackgroundColorSchemeColor(requireContext().getColor(R.color.surface))
        b.swipeRefresh.setOnRefreshListener {
            loadStats()
            loadRecentRuns()
        }
    }

    override fun onResume() {
        super.onResume()
        loadStats()
        loadRecentRuns()
    }

    private fun loadStats() {
        lifecycleScope.launch {
            try {
                val resp = RetrofitClient.api.getMonthlyStats()
                resp.body()?.data?.let { stats ->
                    b.tvTotalKm.text = "%.1f".format(stats.totalKm)
                    b.tvTotalRuns.text = "${stats.totalRuns}"
                    b.tvTotalKcal.text = "${stats.totalCalories}"
                    val p = stats.avgPace
                    b.tvAvgPace.text = if (p > 0) "${p.toInt()}:${"%02d".format(((p % 1) * 60).toInt())}" else "-"
                }
            } catch (e: Exception) {
                showToast(getString(R.string.error_loading_data))
            }
        }
    }

    private fun loadRecentRuns() {
        lifecycleScope.launch {
            try {
                val resp = RetrofitClient.api.getRuns()
                val runs = resp.body()?.data ?: emptyList()

                // Guardar en SQLite local
                val entities = runs.map { it.toEntity() }
                db.runDao().deleteAll()
                db.runDao().insertAll(entities)

                showRuns(runs)
            } catch (e: Exception) {
                // Sin conexion: cargar desde Room
                val local = db.runDao().getAll()
                if (local.isNotEmpty()) {
                    showRuns(local.map { it.toRun() })
                } else {
                    b.tvEmptyRuns.visibility = View.VISIBLE
                }
            } finally {
                b.swipeRefresh.isRefreshing = false
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

    private fun Run.toEntity() = RunEntity(
        id = id, userId = userId, distanceKm = distanceKm, calories = calories,
        durationSec = durationSec, startLat = startLat, startLng = startLng,
        endLat = endLat, endLng = endLng, avgPace = avgPace,
        routeJson = routeJson, createdAt = createdAt
    )

    private fun RunEntity.toRun() = Run(
        id = id, userId = userId, distanceKm = distanceKm, calories = calories,
        durationSec = durationSec, startLat = startLat, startLng = startLng,
        endLat = endLat, endLng = endLng, avgPace = avgPace,
        routeJson = routeJson, createdAt = createdAt
    )
}
