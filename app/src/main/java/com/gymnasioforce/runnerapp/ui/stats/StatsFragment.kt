package com.gymnasioforce.runnerapp.ui.stats

import android.graphics.Color
import android.os.Bundle
import android.view.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.gymnasioforce.runnerapp.R
import com.gymnasioforce.runnerapp.databinding.FragmentStatsBinding
import com.gymnasioforce.runnerapp.network.DailyChartData
import com.gymnasioforce.runnerapp.network.MonthlyChartData
import com.gymnasioforce.runnerapp.utils.showToast

class StatsFragment : Fragment() {

    private lateinit var b: FragmentStatsBinding
    private val viewModel: StatsViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View {
        b = FragmentStatsBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, state: Bundle?) {
        super.onViewCreated(view, state)
        observeViewModel()
        viewModel.loadAll()
    }

    private fun observeViewModel() {
        viewModel.monthly.observe(viewLifecycleOwner) { stats ->
            stats ?: return@observe
            b.tvMonthKm.text = "%.1f".format(stats.totalKm)
            b.tvMonthKcal.text = "${stats.totalCalories}"
            b.tvMonthRuns.text = "${stats.totalRuns}"
            val p = stats.avgPace
            b.tvMonthPace.text = if (p > 0) "${p.toInt()}:${"%02d".format(((p % 1) * 60).toInt())}" else "-"
        }

        viewModel.weeklyCompare.observe(viewLifecycleOwner) { weekly ->
            weekly ?: return@observe
            b.tvWeekKm.text = "%.1f".format(weekly.currentWeekKm)
            val sign = if (weekly.differenceKm >= 0) "+" else ""
            b.tvWeekKmDiff.text = "${sign}%.1f km".format(weekly.differenceKm)
        }

        viewModel.monthlyCompare.observe(viewLifecycleOwner) { mc ->
            mc ?: return@observe
            val sign = if (mc.differenceKm >= 0) "+" else ""
            b.tvMonthKmDiff.text = "${sign}%.1f km".format(mc.differenceKm)
        }

        viewModel.weeklyChart.observe(viewLifecycleOwner) { data ->
            if (data.isNotEmpty()) setupWeeklyChart(data)
        }

        viewModel.monthlyChart.observe(viewLifecycleOwner) { data ->
            if (data.isNotEmpty()) setupMonthlyChart(data)
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let { showToast(getString(R.string.error_loading_stats)) }
        }
    }

    private fun setupWeeklyChart(data: List<DailyChartData>) {
        val chart = b.chartWeekly
        val accentColor = ContextCompat.getColor(requireContext(), R.color.accent)
        val textColor = ContextCompat.getColor(requireContext(), R.color.text_secondary)

        val entries = data.mapIndexed { i, d -> BarEntry(i.toFloat(), d.km.toFloat()) }
        val labels = data.map { it.date.takeLast(5) }

        val dataSet = BarDataSet(entries, "").apply {
            color = accentColor
            valueTextColor = textColor
            valueTextSize = 10f
            setDrawValues(true)
        }

        chart.apply {
            this.data = BarData(dataSet).apply { barWidth = 0.6f }
            description.isEnabled = false
            legend.isEnabled = false
            setDrawGridBackground(false)
            setTouchEnabled(false)
            setBackgroundColor(Color.TRANSPARENT)

            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                this.textColor = textColor
                setDrawGridLines(false)
                granularity = 1f
                valueFormatter = IndexAxisValueFormatter(labels)
                textSize = 10f
            }
            axisLeft.apply {
                this.textColor = textColor
                setDrawGridLines(true)
                gridColor = ContextCompat.getColor(requireContext(), R.color.border)
                axisMinimum = 0f
                textSize = 10f
            }
            axisRight.isEnabled = false

            animateY(600)
            invalidate()
        }
    }

    private fun setupMonthlyChart(data: List<MonthlyChartData>) {
        val chart = b.chartMonthly
        val accentColor = ContextCompat.getColor(requireContext(), R.color.accent)
        val textColor = ContextCompat.getColor(requireContext(), R.color.text_secondary)

        val entries = data.mapIndexed { i, d -> Entry(i.toFloat(), d.km.toFloat()) }
        val labels = data.map { it.month.takeLast(2) + "/" + it.month.take(4).takeLast(2) }

        val dataSet = LineDataSet(entries, "").apply {
            color = accentColor
            lineWidth = 2.5f
            setCircleColor(accentColor)
            circleRadius = 4f
            valueTextColor = textColor
            valueTextSize = 10f
            setDrawFilled(true)
            fillColor = accentColor
            fillAlpha = 40
            mode = LineDataSet.Mode.CUBIC_BEZIER
        }

        chart.apply {
            this.data = LineData(dataSet)
            description.isEnabled = false
            legend.isEnabled = false
            setDrawGridBackground(false)
            setTouchEnabled(false)
            setBackgroundColor(Color.TRANSPARENT)

            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                this.textColor = textColor
                setDrawGridLines(false)
                granularity = 1f
                valueFormatter = IndexAxisValueFormatter(labels)
                textSize = 10f
            }
            axisLeft.apply {
                this.textColor = textColor
                setDrawGridLines(true)
                gridColor = ContextCompat.getColor(requireContext(), R.color.border)
                axisMinimum = 0f
                textSize = 10f
            }
            axisRight.isEnabled = false

            animateX(800)
            invalidate()
        }
    }
}
