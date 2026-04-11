package com.gymnasioforce.runnerapp.data

import android.content.Context
import android.util.Log
import com.gymnasioforce.runnerapp.data.local.AppDatabase
import com.gymnasioforce.runnerapp.network.RetrofitClient
import com.gymnasioforce.runnerapp.network.SaveRunRequest
import com.gymnasioforce.runnerapp.utils.NetworkHelper

/**
 * Administrador de sincronizacion offline.
 * Sube las carreras guardadas localmente al backend cuando hay conexion.
 */
class SyncManager(private val context: Context) {

    private val dao = AppDatabase.getInstance(context).runDao()
    private val api = RetrofitClient.api

    /**
     * Intenta sincronizar todas las carreras pendientes.
     * Retorna la cantidad de carreras sincronizadas exitosamente.
     */
    suspend fun syncPendingRuns(): Int {
        // Verificar conexion antes de intentar
        if (!NetworkHelper.isOnline(context)) return 0

        val unsynced = dao.getUnsynced()
        if (unsynced.isEmpty()) return 0

        var synced = 0
        for (run in unsynced) {
            try {
                // Enviar carrera al backend
                val response = api.saveRun(
                    SaveRunRequest(
                        distanceKm = run.distanceKm,
                        durationSec = run.durationSec,
                        startLat = run.startLat,
                        startLng = run.startLng,
                        endLat = run.endLat,
                        endLng = run.endLng,
                        routeJson = run.routeJson
                    )
                )

                if (response.isSuccessful && response.body()?.success == true) {
                    val serverRun = response.body()!!.data!!
                    // Actualizar con el ID del servidor y marcar como sincronizada
                    dao.update(run.copy(serverId = serverRun.id, synced = true))
                    synced++
                    Log.d("SyncManager", "Carrera ${run.id} sincronizada -> serverId=${serverRun.id}")
                }
            } catch (e: Exception) {
                // Si falla una, continuar con las demas
                Log.e("SyncManager", "Error sincronizando carrera ${run.id}: ${e.message}")
            }
        }

        return synced
    }

    // Cantidad de carreras pendientes de sincronizar
    suspend fun getPendingCount(): Int = dao.getUnsyncedCount()
}
