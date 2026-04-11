package com.gymnasioforce.runnerapp.data.repository

import android.content.Context
import com.gymnasioforce.runnerapp.data.local.AppDatabase
import com.gymnasioforce.runnerapp.data.local.RunEntity
import com.gymnasioforce.runnerapp.network.RetrofitClient
import com.gymnasioforce.runnerapp.network.Run
import com.gymnasioforce.runnerapp.network.SaveRunRequest
import com.gymnasioforce.runnerapp.utils.NetworkHelper
import com.gymnasioforce.runnerapp.utils.Prefs
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Repositorio de carreras — acceso a datos con soporte offline.
 * Estrategia: intentar API primero, fallback a Room si no hay conexion.
 * Las carreras guardadas offline se sincronizan luego con SyncManager.
 */
class RunRepository(private val context: Context) {
    private val api = RetrofitClient.api
    private val dao = AppDatabase.getInstance(context).runDao()

    /**
     * Obtener lista de carreras.
     * Si hay internet: descarga del backend y actualiza cache local.
     * Si no hay internet: devuelve las carreras cacheadas en Room.
     */
    suspend fun getRuns(): Result<List<Run>> {
        return try {
            if (!NetworkHelper.isOnline(context)) {
                // Sin internet: devolver cache local
                val cached = dao.getAll()
                return Result.success(cached.map { it.toRun() })
            }

            val response = api.getRuns()
            if (response.isSuccessful && response.body()?.success == true) {
                val runs = response.body()!!.data!!
                // Actualizar cache: borrar sincronizadas y guardar las nuevas
                dao.deleteSynced()
                dao.insertAll(runs.map { it.toEntity(synced = true) })
                Result.success(runs)
            } else {
                // API fallo: usar cache local
                val cached = dao.getAll()
                if (cached.isNotEmpty()) Result.success(cached.map { it.toRun() })
                else Result.failure(Exception(response.body()?.message ?: "Error"))
            }
        } catch (e: Exception) {
            // Error de red: usar cache local
            val cached = dao.getAll()
            if (cached.isNotEmpty()) Result.success(cached.map { it.toRun() })
            else Result.failure(e)
        }
    }

    /**
     * Guardar una carrera nueva.
     * Si hay internet: envia al backend y guarda localmente.
     * Si no hay internet: guarda localmente con synced=false para sincronizar despues.
     */
    suspend fun saveRun(request: SaveRunRequest): Result<Run> {
        val calories = (request.distanceKm * 70).toInt()
        val pace = if (request.distanceKm > 0)
            (request.durationSec / 60.0) / request.distanceKm else 0.0

        return try {
            if (!NetworkHelper.isOnline(context)) {
                // Sin internet: guardar localmente como no sincronizada
                return saveRunLocally(request, calories, pace)
            }

            val response = api.saveRun(request)
            if (response.isSuccessful && response.body()?.success == true) {
                val run = response.body()!!.data!!
                // Guardar en cache local como sincronizada
                dao.insert(run.toEntity(synced = true))
                Result.success(run)
            } else {
                // API fallo: guardar localmente
                saveRunLocally(request, calories, pace)
            }
        } catch (e: Exception) {
            // Error de red: guardar localmente
            saveRunLocally(request, calories, pace)
        }
    }

    // Guardar carrera solo localmente (modo offline)
    private suspend fun saveRunLocally(request: SaveRunRequest, calories: Int, pace: Double): Result<Run> {
        val now = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(Date())
        val userId = Prefs(context).userId

        val entity = RunEntity(
            serverId = null,
            userId = userId,
            distanceKm = request.distanceKm,
            calories = calories,
            durationSec = request.durationSec,
            startLat = request.startLat,
            startLng = request.startLng,
            endLat = request.endLat,
            endLng = request.endLng,
            avgPace = pace,
            routeJson = request.routeJson,
            createdAt = now,
            synced = false  // Pendiente de sincronizar
        )
        val localId = dao.insert(entity)

        // Devolver como Run para la UI
        val run = Run(
            id = localId.toInt(), userId = userId,
            distanceKm = request.distanceKm, calories = calories,
            durationSec = request.durationSec,
            startLat = request.startLat, startLng = request.startLng,
            endLat = request.endLat, endLng = request.endLng,
            avgPace = pace, routeJson = request.routeJson,
            createdAt = now
        )
        return Result.success(run)
    }

    // Eliminar una carrera
    suspend fun deleteRun(id: Int): Result<Unit> {
        return try {
            if (NetworkHelper.isOnline(context)) {
                val response = api.deleteRun(id)
                if (response.isSuccessful) {
                    dao.getByServerId(id)?.let { dao.delete(it) }
                    Result.success(Unit)
                } else Result.failure(Exception("Error al eliminar"))
            } else {
                // Sin internet: eliminar solo localmente
                dao.getByServerId(id)?.let { dao.delete(it) }
                    ?: dao.getById(id)?.let { dao.delete(it) }
                Result.success(Unit)
            }
        } catch (e: Exception) { Result.failure(e) }
    }

    // Convertir Run (API) a RunEntity (Room)
    private fun Run.toEntity(synced: Boolean = true) = RunEntity(
        serverId = id, userId = userId, distanceKm = distanceKm, calories = calories,
        durationSec = durationSec, startLat = startLat, startLng = startLng,
        endLat = endLat, endLng = endLng, avgPace = avgPace,
        routeJson = routeJson, createdAt = createdAt, synced = synced
    )

    // Convertir RunEntity (Room) a Run (UI)
    private fun RunEntity.toRun() = Run(
        id = serverId ?: id, userId = userId, distanceKm = distanceKm, calories = calories,
        durationSec = durationSec, startLat = startLat, startLng = startLng,
        endLat = endLat, endLng = endLng, avgPace = avgPace,
        routeJson = routeJson, createdAt = createdAt
    )
}
