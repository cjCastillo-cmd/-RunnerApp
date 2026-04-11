package com.gymnasioforce.runnerapp.data.local

import androidx.room.*

/**
 * DAO para acceder a las carreras en la base de datos local.
 * Incluye queries para manejar sincronizacion offline.
 */
@Dao
interface RunDao {

    // Obtener todas las carreras ordenadas por fecha (mas reciente primero)
    @Query("SELECT * FROM runs ORDER BY createdAt DESC")
    suspend fun getAll(): List<RunEntity>

    // Buscar carrera por ID local
    @Query("SELECT * FROM runs WHERE id = :id")
    suspend fun getById(id: Int): RunEntity?

    // Buscar carrera por ID del servidor
    @Query("SELECT * FROM runs WHERE serverId = :serverId")
    suspend fun getByServerId(serverId: Int): RunEntity?

    // Obtener carreras pendientes de sincronizar
    @Query("SELECT * FROM runs WHERE synced = 0")
    suspend fun getUnsynced(): List<RunEntity>

    // Contar carreras pendientes de sincronizar
    @Query("SELECT COUNT(*) FROM runs WHERE synced = 0")
    suspend fun getUnsyncedCount(): Int

    // Insertar una lista de carreras (reemplaza si ya existe)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(runs: List<RunEntity>)

    // Insertar una carrera (reemplaza si ya existe)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(run: RunEntity): Long

    // Actualizar carrera (para marcar como sincronizada)
    @Update
    suspend fun update(run: RunEntity)

    // Eliminar una carrera
    @Delete
    suspend fun delete(run: RunEntity)

    // Eliminar todas las carreras sincronizadas (para refrescar desde el backend)
    @Query("DELETE FROM runs WHERE synced = 1")
    suspend fun deleteSynced()

    // Eliminar todas las carreras
    @Query("DELETE FROM runs")
    suspend fun deleteAll()

    // === Stats locales para modo offline ===

    // Total de km del usuario
    @Query("SELECT COALESCE(SUM(distanceKm), 0) FROM runs")
    suspend fun getTotalKm(): Double

    // Total de calorias
    @Query("SELECT COALESCE(SUM(calories), 0) FROM runs")
    suspend fun getTotalCalories(): Int

    // Total de carreras
    @Query("SELECT COUNT(*) FROM runs")
    suspend fun getTotalRuns(): Int

    // Ritmo promedio
    @Query("SELECT COALESCE(AVG(avgPace), 0) FROM runs WHERE avgPace > 0")
    suspend fun getAvgPace(): Double
}
