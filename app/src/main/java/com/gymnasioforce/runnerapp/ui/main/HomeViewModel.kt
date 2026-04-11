package com.gymnasioforce.runnerapp.ui.main

import android.app.Application
import androidx.lifecycle.*
import com.gymnasioforce.runnerapp.data.repository.RunRepository
import com.gymnasioforce.runnerapp.data.repository.StatsRepository
import com.gymnasioforce.runnerapp.network.MonthlyStats
import com.gymnasioforce.runnerapp.network.Run
import kotlinx.coroutines.launch

/**
 * ViewModel para la pantalla Home.
 * Carga carreras recientes y estadisticas mensuales.
 * Funciona offline gracias a RunRepository y StatsRepository.
 */
class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val runRepo = RunRepository(application)
    private val statsRepo = StatsRepository(application)

    private val _runs = MutableLiveData<List<Run>>(emptyList())
    val runs: LiveData<List<Run>> = _runs

    private val _stats = MutableLiveData<MonthlyStats?>()
    val stats: LiveData<MonthlyStats?> = _stats

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    // Cargar datos: stats y carreras recientes
    fun loadData() {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null

            statsRepo.getMonthlyStats()
                .onSuccess { _stats.value = it }
                .onFailure { _error.value = it.message }

            runRepo.getRuns()
                .onSuccess { _runs.value = it }
                .onFailure { if (_error.value == null) _error.value = it.message }

            _loading.value = false
        }
    }
}
