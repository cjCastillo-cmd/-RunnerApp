package com.gymnasioforce.runnerapp.ui.friends

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.gymnasioforce.runnerapp.databinding.ItemRequestBinding
import com.gymnasioforce.runnerapp.network.Friend
import com.gymnasioforce.runnerapp.utils.AvatarHelper

class RequestAdapter(
    private val requests: List<Friend>,
    private val onAccept: (Friend) -> Unit,
    private val onReject: (Friend) -> Unit
) : RecyclerView.Adapter<RequestAdapter.VH>() {

    inner class VH(val b: ItemRequestBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemRequestBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount() = requests.size

    override fun onBindViewHolder(h: VH, pos: Int) {
        val friend = requests[pos]
        val user = friend.user
        h.b.tvName.text = user?.name ?: "Usuario"
        h.b.tvMessage.text = "quiere ser tu amigo"

        if (user?.photoUrl != null) {
            Glide.with(h.b.ivAvatar).load(user.photoUrl).circleCrop().into(h.b.ivAvatar)
        } else {
            h.b.ivAvatar.setImageDrawable(
                AvatarHelper.generateInitials(h.b.root.context, user?.name ?: "?")
            )
        }

        h.b.btnAccept.setOnClickListener { onAccept(friend) }
        h.b.btnReject.setOnClickListener { onReject(friend) }
    }
}
