package com.gymnasioforce.runnerapp.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad Room que representa una carrera guardada localmente.
 * Soporta modo offline: las carreras sin sincronizar tienen synced=false
 * y serverId=null hasta que se suban al backend.
 */
@Entity(tableName = "runs")
data class RunEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,   // ID local auto-generado
    val serverId: Int? = null,      // ID del backend (null si no se ha sincronizado)
    val userId: Int,                // ID del usuario
    val distanceKm: Double,         // Distancia en kilometros
    val calories: Int,              // Calorias quemadas
    val durationSec: Int,           // Duracion en segundos
    val startLat: Double,           // Latitud de inicio
    val startLng: Double,           // Longitud de inicio
    val endLat: Double,             // Latitud de fin
    val endLng: Double,             // Longitud de fin
    val avgPace: Double?,           // Ritmo promedio (min/km)
    val routeJson: String?,         // Ruta GPS como JSON array de {lat, lng}
    val createdAt: String,          // Fecha de creacion
    val synced: Boolean = true      // false = pendiente de sincronizar con el backend
)
