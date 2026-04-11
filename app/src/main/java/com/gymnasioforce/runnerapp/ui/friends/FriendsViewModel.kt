package com.gymnasioforce.runnerapp.ui.friends

import android.app.Application
import androidx.lifecycle.*
import com.gymnasioforce.runnerapp.data.repository.FriendRepository
import com.gymnasioforce.runnerapp.data.repository.StatsRepository
import com.gymnasioforce.runnerapp.network.*
import kotlinx.coroutines.launch

/**
 * ViewModel para la pantalla social.
 * Maneja leaderboard, solicitudes de amistad, amigos y descubrimiento.
 */
class FriendsViewModel(application: Application) : AndroidViewModel(application) {

    private val friendRepo = FriendRepository()
    private val statsRepo = StatsRepository(application)

    private val _leaderboard = MutableLiveData<List<LeaderboardEntry>>(emptyList())
    val leaderboard: LiveData<List<LeaderboardEntry>> = _leaderboard

    private val _pending = MutableLiveData<List<Friend>>(emptyList())
    val pending: LiveData<List<Friend>> = _pending

    private val _friends = MutableLiveData<List<User>>(emptyList())
    val friends: LiveData<List<User>> = _friends

    private val _discover = MutableLiveData<List<User>>(emptyList())
    val discover: LiveData<List<User>> = _discover

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private val _message = MutableLiveData<String?>()
    val message: LiveData<String?> = _message

    // Cargar todos los datos sociales
    fun loadAll() {
        viewModelScope.launch {
            _loading.value = true
            statsRepo.getLeaderboard().onSuccess { _leaderboard.value = it }
            friendRepo.getPendingRequests().onSuccess { _pending.value = it }
            friendRepo.getFriends().onSuccess { users ->
                _friends.value = users.mapNotNull { it.friend }
            }
            friendRepo.getUsersByCountry().onSuccess { _discover.value = it }
            _loading.value = false
        }
    }

    // Aceptar o rechazar solicitud de amistad
    fun respondRequest(requestId: Int, action: String) {
        viewModelScope.launch {
            friendRepo.respondRequest(requestId, action)
                .onSuccess {
                    _message.value = if (action == "accepted") "accepted" else "rejected"
                    loadAll()
                }
                .onFailure { _message.value = "error" }
        }
    }

    // Enviar solicitud de amistad
    fun sendRequest(userId: Int, userName: String) {
        viewModelScope.launch {
            friendRepo.sendRequest(userId)
                .onSuccess {
                    _message.value = "sent:$userName"
                    // Quitar usuario de la lista de descubrir
                    _discover.value = _discover.value?.filter { it.id != userId }
                }
                .onFailure { _message.value = "error_send" }
        }
    }

    fun clearMessage() { _message.value = null }
}
