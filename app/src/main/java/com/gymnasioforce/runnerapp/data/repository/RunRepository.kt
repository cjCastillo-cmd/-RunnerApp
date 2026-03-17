package com.gymnasioforce.runnerapp.data.repository

import android.content.Context
import com.gymnasioforce.runnerapp.data.local.AppDatabase
import com.gymnasioforce.runnerapp.data.local.RunEntity
import com.gymnasioforce.runnerapp.network.RetrofitClient
import com.gymnasioforce.runnerapp.network.Run
import com.gymnasioforce.runnerapp.network.SaveRunRequest

class RunRepository(context: Context) {
    private val api = RetrofitClient.api
    private val dao = AppDatabase.getInstance(context).runDao()

    suspend fun getRuns(): Result<List<Run>> {
        return try {
            val response = api.getRuns()
            if (response.isSuccessful && response.body()?.success == true) {
                val runs = response.body()!!.data!!
                dao.deleteAll()
                dao.insertAll(runs.map { it.toEntity() })
                Result.success(runs)
            } else {
                val cached = dao.getAll()
                if (cached.isNotEmpty()) Result.success(cached.map { it.toRun() })
                else Result.failure(Exception(response.body()?.message ?: "Error"))
            }
        } catch (e: Exception) {
            val cached = dao.getAll()
            if (cached.isNotEmpty()) Result.success(cached.map { it.toRun() })
            else Result.failure(e)
        }
    }

    suspend fun saveRun(request: SaveRunRequest): Result<Run> {
        return try {
            val response = api.saveRun(request)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.data!!)
            } else Result.failure(Exception(response.body()?.message ?: "Error al guardar"))
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun deleteRun(id: Int): Result<Unit> {
        return try {
            val response = api.deleteRun(id)
            if (response.isSuccessful) {
                dao.getById(id)?.let { dao.delete(it) }
                Result.success(Unit)
            } else Result.failure(Exception("Error al eliminar"))
        } catch (e: Exception) { Result.failure(e) }
    }

    private fun Run.toEntity() = RunEntity(
        id = id, userId = userId, distanceKm = distanceKm, calories = calories,
        durationSec = durationSec, startLat = startLat, startLng = startLng,
        endLat = endLat, endLng = endLng, avgPace = avgPace,
        routeJson = routeJson, createdAt = createdAt
    )

    private fun RunEntity.toRun() = Run(
        id = id, userId = userId, distanceKm = distanceKm, calories = calories,
        durationSec = durationSec, startLat = startLat, startLng = startLng,
        endLat = endLat, endLng = endLng, avgPace = avgPace,
        routeJson = routeJson, createdAt = createdAt
    )
}
