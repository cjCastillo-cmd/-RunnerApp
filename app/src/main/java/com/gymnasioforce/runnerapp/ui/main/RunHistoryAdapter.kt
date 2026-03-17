package com.gymnasioforce.runnerapp.ui.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gymnasioforce.runnerapp.databinding.ItemRunBinding
import com.gymnasioforce.runnerapp.network.Run

class RunHistoryAdapter(
    private val runs: List<Run>,
    private val onClick: (Run) -> Unit
) : RecyclerView.Adapter<RunHistoryAdapter.VH>() {

    inner class VH(val b: ItemRunBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemRunBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount() = runs.size

    override fun onBindViewHolder(h: VH, pos: Int) {
        val run = runs[pos]
        h.b.tvKm.text = "%.2f".format(run.distanceKm)
        h.b.tvCalories.text = "${run.calories} kcal"

        val sec = run.durationSec
        h.b.tvDuration.text = "%02d:%02d:%02d".format(sec / 3600, (sec % 3600) / 60, sec % 60)

        h.b.tvDate.text = run.createdAt.take(10)

        h.b.root.setOnClickListener { onClick(run) }
    }
}
