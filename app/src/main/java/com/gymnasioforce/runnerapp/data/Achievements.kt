package com.gymnasioforce.runnerapp.data

import com.gymnasioforce.runnerapp.R

data class Achievement(
    val id: String,
    val titleRes: Int,
    val descriptionRes: Int,
    val iconRes: Int,
    val unlocked: Boolean,
    val progress: Float // 0.0 a 1.0
)

object AchievementCalculator {

    fun calculate(totalRuns: Int, totalKm: Double, totalCalories: Int): List<Achievement> = listOf(
        // Distancia
        Achievement("km_1", R.string.achievement_first_km, R.string.achievement_first_km_desc,
            R.drawable.ic_run, totalKm >= 1.0, (totalKm / 1.0).coerceAtMost(1.0).toFloat()),
        Achievement("km_10", R.string.achievement_10km, R.string.achievement_10km_desc,
            R.drawable.ic_run, totalKm >= 10.0, (totalKm / 10.0).coerceAtMost(1.0).toFloat()),
        Achievement("km_42", R.string.achievement_marathon, R.string.achievement_marathon_desc,
            R.drawable.ic_run, totalKm >= 42.195, (totalKm / 42.195).coerceAtMost(1.0).toFloat()),
        Achievement("km_100", R.string.achievement_100km, R.string.achievement_100km_desc,
            R.drawable.ic_run, totalKm >= 100.0, (totalKm / 100.0).coerceAtMost(1.0).toFloat()),
        Achievement("km_500", R.string.achievement_500km, R.string.achievement_500km_desc,
            R.drawable.ic_run, totalKm >= 500.0, (totalKm / 500.0).coerceAtMost(1.0).toFloat()),

        // Carreras
        Achievement("runs_1", R.string.achievement_first_run, R.string.achievement_first_run_desc,
            R.drawable.ic_run, totalRuns >= 1, (totalRuns / 1.0).coerceAtMost(1.0).toFloat()),
        Achievement("runs_10", R.string.achievement_10_runs, R.string.achievement_10_runs_desc,
            R.drawable.ic_run, totalRuns >= 10, (totalRuns / 10.0).coerceAtMost(1.0).toFloat()),
        Achievement("runs_50", R.string.achievement_50_runs, R.string.achievement_50_runs_desc,
            R.drawable.ic_run, totalRuns >= 50, (totalRuns / 50.0).coerceAtMost(1.0).toFloat()),
        Achievement("runs_100", R.string.achievement_100_runs, R.string.achievement_100_runs_desc,
            R.drawable.ic_run, totalRuns >= 100, (totalRuns / 100.0).coerceAtMost(1.0).toFloat()),

        // Calorias
        Achievement("cal_1000", R.string.achievement_1000cal, R.string.achievement_1000cal_desc,
            R.drawable.ic_run, totalCalories >= 1000, (totalCalories / 1000.0).coerceAtMost(1.0).toFloat()),
        Achievement("cal_10000", R.string.achievement_10000cal, R.string.achievement_10000cal_desc,
            R.drawable.ic_run, totalCalories >= 10000, (totalCalories / 10000.0).coerceAtMost(1.0).toFloat()),
    )
}