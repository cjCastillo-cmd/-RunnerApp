package com.gymnasioforce.runnerapp.ui.friends

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.gymnasioforce.runnerapp.R
import com.gymnasioforce.runnerapp.databinding.FragmentFriendsBinding
import com.gymnasioforce.runnerapp.utils.showToast

class FriendsFragment : Fragment() {

    private lateinit var b: FragmentFriendsBinding
    private val viewModel: FriendsViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View {
        b = FragmentFriendsBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, state: Bundle?) {
        super.onViewCreated(view, state)
        b.rvLeaderboard.layoutManager = LinearLayoutManager(requireContext())
        b.rvFriends.layoutManager = LinearLayoutManager(requireContext())
        b.rvDiscover.layoutManager = LinearLayoutManager(requireContext())
        b.rvRequests.layoutManager = LinearLayoutManager(requireContext())

        b.swipeRefresh.setColorSchemeColors(requireContext().getColor(R.color.volt))
        b.swipeRefresh.setProgressBackgroundColorSchemeColor(requireContext().getColor(R.color.surface))
        b.swipeRefresh.setOnRefreshListener { viewModel.loadAll() }

        observeViewModel()
        viewModel.loadAll()
    }

    private fun observeViewModel() {
        viewModel.leaderboard.observe(viewLifecycleOwner) { entries ->
            if (entries.isEmpty()) {
                b.tvEmptyLeaderboard.visibility = View.VISIBLE
            } else {
                b.tvEmptyLeaderboard.visibility = View.GONE
                b.rvLeaderboard.adapter = LeaderboardAdapter(entries)
            }
        }

        viewModel.pending.observe(viewLifecycleOwner) { pending ->
            if (pending.isEmpty()) {
                b.tvEmptyRequests.visibility = View.VISIBLE
                b.tvRequestsCount.visibility = View.GONE
                b.rvRequests.visibility = View.GONE
            } else {
                b.tvEmptyRequests.visibility = View.GONE
                b.tvRequestsCount.visibility = View.VISIBLE
                b.tvRequestsCount.text = "${pending.size}"
                b.rvRequests.visibility = View.VISIBLE
                b.rvRequests.adapter = RequestAdapter(pending,
                    onAccept = { friend -> viewModel.respondRequest(friend.id, "accepted") },
                    onReject = { friend -> viewModel.respondRequest(friend.id, "rejected") }
                )
            }
        }

        viewModel.friends.observe(viewLifecycleOwner) { users ->
            b.rvFriends.adapter = UserListAdapter(users, showAddBtn = false)
            b.tvEmptyFriends.visibility = if (users.isEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.discover.observe(viewLifecycleOwner) { users ->
            b.rvDiscover.adapter = UserListAdapter(users, showAddBtn = true) { user ->
                viewModel.sendRequest(user.id, user.name)
            }
            b.tvEmptyDiscover.visibility = if (users.isEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.loading.observe(viewLifecycleOwner) { loading ->
            b.swipeRefresh.isRefreshing = loading
        }

        viewModel.message.observe(viewLifecycleOwner) { msg ->
            msg ?: return@observe
            when {
                msg == "accepted" -> showToast(getString(R.string.success_request_accepted))
                msg == "rejected" -> showToast(getString(R.string.success_request_rejected))
                msg.startsWith("sent:") -> {
                    val name = msg.removePrefix("sent:")
                    showToast(getString(R.string.success_request_sent, name))
                }
                msg == "error" -> showToast(getString(R.string.error_connection))
                msg == "error_send" -> showToast(getString(R.string.error_send_request))
            }
            viewModel.clearMessage()
        }
    }
}
