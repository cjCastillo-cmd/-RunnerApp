package com.gymnasioforce.runnerapp.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "runs")
data class RunEntity(
    @PrimaryKey val id: Int,
    val userId: Int,
    val distanceKm: Double,
    val calories: Int,
    val durationSec: Int,
    val startLat: Double,
    val startLng: Double,
    val endLat: Double,
    val endLng: Double,
    val avgPace: Double?,
    val routeJson: String?,
    val createdAt: String
)
