package com.gymnasioforce.runnerapp.ui.profile

import android.app.Application
import androidx.lifecycle.*
import com.gymnasioforce.runnerapp.data.repository.StatsRepository
import com.gymnasioforce.runnerapp.data.repository.UserRepository
import com.gymnasioforce.runnerapp.network.User
import kotlinx.coroutines.launch
import okhttp3.MultipartBody

/**
 * ViewModel para la pantalla de perfil.
 * Maneja datos del usuario, foto, logros y eliminacion de cuenta.
 */
class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val userRepo = UserRepository()
    private val statsRepo = StatsRepository(application)

    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> = _user

    private val _totalRuns = MutableLiveData(0)
    val totalRuns: LiveData<Int> = _totalRuns

    private val _photoUrl = MutableLiveData<String?>()
    val photoUrl: LiveData<String?> = _photoUrl

    private val _message = MutableLiveData<String?>()
    val message: LiveData<String?> = _message

    private val _accountDeleted = MutableLiveData(false)
    val accountDeleted: LiveData<Boolean> = _accountDeleted

    // Cargar perfil del usuario y stats
    fun loadProfile() {
        viewModelScope.launch {
            userRepo.getProfile().onSuccess { user ->
                _user.value = user
                _photoUrl.value = user.photoUrl
            }.onFailure { _message.value = "error_profile" }

            statsRepo.getMonthlyStats()
                .onSuccess { _totalRuns.value = it.totalRuns }
        }
    }

    // Guardar cambios en el perfil
    fun saveProfile(name: String, country: String) {
        viewModelScope.launch {
            userRepo.updateProfile(name, country)
                .onSuccess {
                    _user.value = it
                    _message.value = "saved"
                }
                .onFailure { _message.value = "error_save" }
        }
    }

    // Subir foto de perfil
    fun uploadPhoto(photo: MultipartBody.Part) {
        viewModelScope.launch {
            userRepo.uploadPhoto(photo)
                .onSuccess { _photoUrl.value = it; _message.value = "photo_ok" }
                .onFailure { _message.value = "error_photo" }
        }
    }

    // Eliminar cuenta permanentemente
    fun deleteAccount() {
        viewModelScope.launch {
            userRepo.deleteAccount()
                .onSuccess { _accountDeleted.value = true }
                .onFailure { _message.value = "error_delete" }
        }
    }

    // Cerrar sesion
    fun logout() {
        viewModelScope.launch {
            userRepo.logout()
            _accountDeleted.value = true
        }
    }

    fun clearMessage() { _message.value = null }
}
