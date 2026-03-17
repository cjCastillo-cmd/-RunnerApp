package com.gymnasioforce.runnerapp.ui.stats

import androidx.lifecycle.*
import com.gymnasioforce.runnerapp.data.repository.StatsRepository
import com.gymnasioforce.runnerapp.network.*
import kotlinx.coroutines.launch

class StatsViewModel : ViewModel() {

    private val repo = StatsRepository()

    private val _monthly = MutableLiveData<MonthlyStats?>()
    val monthly: LiveData<MonthlyStats?> = _monthly

    private val _weeklyCompare = MutableLiveData<WeeklyCompare?>()
    val weeklyCompare: LiveData<WeeklyCompare?> = _weeklyCompare

    private val _monthlyCompare = MutableLiveData<MonthlyCompare?>()
    val monthlyCompare: LiveData<MonthlyCompare?> = _monthlyCompare

    private val _weeklyChart = MutableLiveData<List<DailyChartData>>(emptyList())
    val weeklyChart: LiveData<List<DailyChartData>> = _weeklyChart

    private val _monthlyChart = MutableLiveData<List<MonthlyChartData>>(emptyList())
    val monthlyChart: LiveData<List<MonthlyChartData>> = _monthlyChart

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun loadAll() {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null

            repo.getMonthlyStats().onSuccess { _monthly.value = it }
            repo.getWeeklyCompare().onSuccess { _weeklyCompare.value = it }
            repo.getMonthlyCompare().onSuccess { _monthlyCompare.value = it }
            repo.getWeeklyChart().onSuccess { _weeklyChart.value = it }
            repo.getMonthlyChart().onSuccess { _monthlyChart.value = it }

            _loading.value = false
        }
    }
}
