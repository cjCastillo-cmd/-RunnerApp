package com.gymnasioforce.runnerapp.data.repository

import android.content.Context
import com.gymnasioforce.runnerapp.data.local.AppDatabase
import com.gymnasioforce.runnerapp.network.*
import com.gymnasioforce.runnerapp.utils.NetworkHelper

/**
 * Repositorio de estadisticas con soporte offline.
 * Si hay internet: obtiene datos del backend.
 * Si no hay internet: calcula stats desde la base de datos local.
 */
class StatsRepository(private val context: Context) {
    private val api = RetrofitClient.api
    private val dao = AppDatabase.getInstance(context).runDao()

    // Estadisticas mensuales — con fallback a calculo local
    suspend fun getMonthlyStats(): Result<MonthlyStats> {
        return try {
            if (!NetworkHelper.isOnline(context)) return getLocalStats()

            val resp = api.getMonthlyStats()
            if (resp.isSuccessful && resp.body()?.success == true) {
                Result.success(resp.body()!!.data!!)
            } else getLocalStats()
        } catch (e: Exception) { getLocalStats() }
    }

    // Calcular stats desde Room cuando no hay internet
    private suspend fun getLocalStats(): Result<MonthlyStats> {
        return try {
            val stats = MonthlyStats(
                totalRuns = dao.getTotalRuns(),
                totalKm = dao.getTotalKm(),
                totalCalories = dao.getTotalCalories(),
                avgPace = dao.getAvgPace()
            )
            Result.success(stats)
        } catch (e: Exception) { Result.failure(e) }
    }

    // Comparacion semanal
    suspend fun getWeeklyCompare(): Result<WeeklyCompare> {
        return try {
            if (!NetworkHelper.isOnline(context)) return Result.failure(Exception("Offline"))
            val resp = api.getWeeklyCompare()
            if (resp.isSuccessful && resp.body()?.success == true) {
                Result.success(resp.body()!!.data!!)
            } else Result.failure(Exception("Error"))
        } catch (e: Exception) { Result.failure(e) }
    }

    // Comparacion mensual
    suspend fun getMonthlyCompare(): Result<MonthlyCompare> {
        return try {
            if (!NetworkHelper.isOnline(context)) return Result.failure(Exception("Offline"))
            val resp = api.getMonthlyCompare()
            if (resp.isSuccessful && resp.body()?.success == true) {
                Result.success(resp.body()!!.data!!)
            } else Result.failure(Exception("Error"))
        } catch (e: Exception) { Result.failure(e) }
    }

    // Chart semanal (solo online)
    suspend fun getWeeklyChart(): Result<List<DailyChartData>> {
        return try {
            if (!NetworkHelper.isOnline(context)) return Result.success(emptyList())
            val resp = api.getWeeklyChart()
            if (resp.isSuccessful && resp.body()?.success == true) {
                Result.success(resp.body()!!.data!!)
            } else Result.success(emptyList())
        } catch (e: Exception) { Result.success(emptyList()) }
    }

    // Chart mensual (solo online)
    suspend fun getMonthlyChart(): Result<List<MonthlyChartData>> {
        return try {
            if (!NetworkHelper.isOnline(context)) return Result.success(emptyList())
            val resp = api.getMonthlyChart()
            if (resp.isSuccessful && resp.body()?.success == true) {
                Result.success(resp.body()!!.data!!)
            } else Result.success(emptyList())
        } catch (e: Exception) { Result.success(emptyList()) }
    }

    // Leaderboard (solo online)
    suspend fun getLeaderboard(): Result<List<LeaderboardEntry>> {
        return try {
            if (!NetworkHelper.isOnline(context)) return Result.success(emptyList())
            val resp = api.getLeaderboard()
            if (resp.isSuccessful && resp.body()?.success == true) {
                Result.success(resp.body()!!.data!!)
            } else Result.success(emptyList())
        } catch (e: Exception) { Result.failure(e) }
    }
}
