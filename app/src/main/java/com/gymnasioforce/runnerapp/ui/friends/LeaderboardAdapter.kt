package com.gymnasioforce.runnerapp.ui.friends

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.gymnasioforce.runnerapp.databinding.ItemLeaderboardBinding
import com.gymnasioforce.runnerapp.network.LeaderboardEntry

class LeaderboardAdapter(
    private val entries: List<LeaderboardEntry>
) : RecyclerView.Adapter<LeaderboardAdapter.VH>() {

    inner class VH(val b: ItemLeaderboardBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemLeaderboardBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount() = entries.size

    override fun onBindViewHolder(h: VH, pos: Int) {
        val entry = entries[pos]
        h.b.tvPosition.text = "${entry.position}"
        h.b.tvName.text = entry.name
        h.b.tvCountry.text = entry.country
        h.b.tvKm.text = "%.1f".format(entry.kmThisMonth)

        if (entry.isMe) {
            h.b.tvPosition.setTextColor(h.b.root.context.getColor(com.gymnasioforce.runnerapp.R.color.volt))
        }

        entry.photoUrl?.let {
            Glide.with(h.b.ivAvatar).load(it).circleCrop().into(h.b.ivAvatar)
        }
    }
}
