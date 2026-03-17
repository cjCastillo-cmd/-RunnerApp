package com.gymnasioforce.runnerapp.ui.friends

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.gymnasioforce.runnerapp.databinding.FragmentFriendsBinding
import com.gymnasioforce.runnerapp.network.FriendRequest
import com.gymnasioforce.runnerapp.network.FriendResponse
import com.gymnasioforce.runnerapp.network.RetrofitClient
import com.gymnasioforce.runnerapp.network.User
import com.gymnasioforce.runnerapp.utils.showToast
import kotlinx.coroutines.launch

class FriendsFragment : Fragment() {

    private lateinit var b: FragmentFriendsBinding

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

        b.swipeRefresh.setColorSchemeColors(0xFFC8FF00.toInt())
        b.swipeRefresh.setProgressBackgroundColorSchemeColor(0xFF141414.toInt())
        b.swipeRefresh.setOnRefreshListener { loadAll() }

        loadAll()
    }

    private fun loadAll() {
        loadLeaderboard()
        loadPendingRequests()
        loadFriends()
        loadDiscover()
        // Detener spinner despues de un momento
        b.swipeRefresh.postDelayed({ b.swipeRefresh.isRefreshing = false }, 2000)
    }

    private fun loadLeaderboard() {
        lifecycleScope.launch {
            try {
                val entries = RetrofitClient.api.getLeaderboard().body()?.data ?: emptyList()
                if (entries.isEmpty()) {
                    b.tvEmptyLeaderboard.visibility = View.VISIBLE
                } else {
                    b.tvEmptyLeaderboard.visibility = View.GONE
                    b.rvLeaderboard.adapter = LeaderboardAdapter(entries)
                }
            } catch (e: Exception) {
                b.tvEmptyLeaderboard.visibility = View.VISIBLE
            }
        }
    }

    private fun loadPendingRequests() {
        lifecycleScope.launch {
            try {
                val pending = RetrofitClient.api.getPendingRequests().body()?.data ?: emptyList()
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
                        onAccept = { friend -> respondRequest(friend.id, "accept") },
                        onReject = { friend -> respondRequest(friend.id, "reject") }
                    )
                }
            } catch (e: Exception) {
                b.tvEmptyRequests.visibility = View.VISIBLE
            }
        }
    }

    private fun respondRequest(requestId: Int, action: String) {
        lifecycleScope.launch {
            try {
                val resp = RetrofitClient.api.respondFriendRequest(FriendResponse(requestId, action))
                if (resp.isSuccessful) {
                    val msg = if (action == "accept") "Solicitud aceptada" else "Solicitud rechazada"
                    showToast(msg)
                    loadPendingRequests()
                    if (action == "accept") {
                        loadFriends()
                        loadLeaderboard()
                    }
                }
            } catch (e: Exception) { showToast("Error: ${e.message}") }
        }
    }

    private fun loadFriends() {
        lifecycleScope.launch {
            try {
                val friends = RetrofitClient.api.getFriends().body()?.data ?: emptyList()
                val users = friends.mapNotNull { it.friend }
                b.rvFriends.adapter = UserListAdapter(users, showAddBtn = false)
                b.tvEmptyFriends.visibility = if (users.isEmpty()) View.VISIBLE else View.GONE
            } catch (e: Exception) { showToast("Error cargando amigos") }
        }
    }

    private fun loadDiscover() {
        lifecycleScope.launch {
            try {
                val users = RetrofitClient.api.getUsersByCountry().body()?.data ?: emptyList()
                b.rvDiscover.adapter = UserListAdapter(users, showAddBtn = true) { user ->
                    sendFriendRequest(user)
                }
                b.tvEmptyDiscover.visibility = if (users.isEmpty()) View.VISIBLE else View.GONE
            } catch (e: Exception) { showToast("Error cargando usuarios") }
        }
    }

    private fun sendFriendRequest(user: User) {
        lifecycleScope.launch {
            try {
                val resp = RetrofitClient.api.sendFriendRequest(FriendRequest(user.id))
                if (resp.isSuccessful) showToast("Solicitud enviada a ${user.name}")
                else showToast("No se pudo enviar la solicitud")
            } catch (e: Exception) { showToast("Error: ${e.message}") }
        }
    }
}
