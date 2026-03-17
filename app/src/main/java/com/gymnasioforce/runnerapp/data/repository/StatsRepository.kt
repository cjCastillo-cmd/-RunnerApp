package com.gymnasioforce.runnerapp.data.repository

import com.gymnasioforce.runnerapp.network.*

class StatsRepository {
    private val api = RetrofitClient.api

    suspend fun getMonthlyStats(): Result<MonthlyStats> {
        return try {
            val resp = api.getMonthlyStats()
            if (resp.isSuccessful && resp.body()?.success == true) {
                Result.success(resp.body()!!.data!!)
            } else Result.failure(Exception("Error"))
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun getWeeklyCompare(): Result<WeeklyCompare> {
        return try {
            val resp = api.getWeeklyCompare()
            if (resp.isSuccessful && resp.body()?.success == true) {
                Result.success(resp.body()!!.data!!)
            } else Result.failure(Exception("Error"))
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun getMonthlyCompare(): Result<MonthlyCompare> {
        return try {
            val resp = api.getMonthlyCompare()
            if (resp.isSuccessful && resp.body()?.success == true) {
                Result.success(resp.body()!!.data!!)
            } else Result.failure(Exception("Error"))
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun getWeeklyChart(): Result<List<DailyChartData>> {
        return try {
            val resp = api.getWeeklyChart()
            if (resp.isSuccessful && resp.body()?.success == true) {
                Result.success(resp.body()!!.data!!)
            } else Result.success(emptyList())
        } catch (e: Exception) { Result.success(emptyList()) }
    }

    suspend fun getMonthlyChart(): Result<List<MonthlyChartData>> {
        return try {
            val resp = api.getMonthlyChart()
            if (resp.isSuccessful && resp.body()?.success == true) {
                Result.success(resp.body()!!.data!!)
            } else Result.success(emptyList())
        } catch (e: Exception) { Result.success(emptyList()) }
    }

    suspend fun getLeaderboard(): Result<List<LeaderboardEntry>> {
        return try {
            val resp = api.getLeaderboard()
            if (resp.isSuccessful && resp.body()?.success == true) {
                Result.success(resp.body()!!.data!!)
            } else Result.success(emptyList())
        } catch (e: Exception) { Result.failure(e) }
    }
}
