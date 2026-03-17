package com.gymnasioforce.runnerapp.ui.profile

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gymnasioforce.runnerapp.data.Achievement
import com.gymnasioforce.runnerapp.databinding.ItemAchievementBinding

class AchievementAdapter(
    private val achievements: List<Achievement>
) : RecyclerView.Adapter<AchievementAdapter.VH>() {

    inner class VH(val b: ItemAchievementBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemAchievementBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount() = achievements.size

    override fun onBindViewHolder(h: VH, pos: Int) {
        val a = achievements[pos]
        h.b.tvTitle.text = h.b.root.context.getString(a.titleRes)
        h.b.tvDescription.text = h.b.root.context.getString(a.descriptionRes)
        h.b.progressBar.progress = (a.progress * 100).toInt()

        if (a.unlocked) {
            h.b.ivIcon.alpha = 1f
            h.b.ivIcon.setColorFilter(h.b.root.context.getColor(com.gymnasioforce.runnerapp.R.color.accent))
            h.b.tvTitle.alpha = 1f
        } else {
            h.b.ivIcon.alpha = 0.3f
            h.b.ivIcon.setColorFilter(h.b.root.context.getColor(com.gymnasioforce.runnerapp.R.color.text_tertiary))
            h.b.tvTitle.alpha = 0.5f
        }
    }
}