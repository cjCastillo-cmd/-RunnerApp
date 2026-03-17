package com.gymnasioforce.runnerapp.ui.friends

import android.view.*
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.gymnasioforce.runnerapp.databinding.ItemUserBinding
import com.gymnasioforce.runnerapp.network.User

class UserListAdapter(
    private val users: List<User>,
    private val showAddBtn: Boolean,
    private val onAdd: ((User) -> Unit)? = null
) : RecyclerView.Adapter<UserListAdapter.VH>() {

    inner class VH(val b: ItemUserBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemUserBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount() = users.size

    override fun onBindViewHolder(h: VH, pos: Int) {
        val user = users[pos]
        h.b.tvName.text = user.name
        h.b.tvKm.text = "%.1f km".format(user.totalKm)
        h.b.btnAction.visibility = if (showAddBtn) View.VISIBLE else View.GONE
        h.b.btnAction.setOnClickListener { onAdd?.invoke(user) }
        user.photoUrl?.let {
            Glide.with(h.b.ivAvatar).load(it).circleCrop().into(h.b.ivAvatar)
        }
    }
}
