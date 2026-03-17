package com.gymnasioforce.runnerapp.network

import com.google.gson.annotations.SerializedName

data class ApiResponse<T>(val success: Boolean, val data: T? = null, val message: String? = null)

data class User(
    val id: Int, val name: String, val email: String, val country: String,
    @SerializedName("photo_url") val photoUrl: String?,
    @SerializedName("email_verified") val emailVerified: Boolean,
    @SerializedName("total_km") val totalKm: Double,
    @SerializedName("total_calories") val totalCalories: Int
)

data class RegisterRequest(
    val name: String, val email: String, val password: String,
    @SerializedName("password_confirmation") val passwordConfirmation: String,
    val country: String
)

data class LoginRequest(val email: String, val password: String)
data class VerifyEmailRequest(val email: String, val code: String)
data class AuthResponse(val token: String, val user: User)

data class Run(
    val id: Int, @SerializedName("user_id") val userId: Int,
    @SerializedName("distance_km") val distanceKm: Double, val calories: Int,
    @SerializedName("duration_sec") val durationSec: Int,
    @SerializedName("start_lat") val startLat: Double,
    @SerializedName("start_lng") val startLng: Double,
    @SerializedName("end_lat") val endLat: Double,
    @SerializedName("end_lng") val endLng: Double,
    @SerializedName("avg_pace") val avgPace: Double?,
    @SerializedName("route_json") val routeJson: String?,
    @SerializedName("created_at") val createdAt: String
)

data class SaveRunRequest(
    @SerializedName("distance_km") val distanceKm: Double,
    @SerializedName("duration_sec") val durationSec: Int,
    @SerializedName("start_lat") val startLat: Double,
    @SerializedName("start_lng") val startLng: Double,
    @SerializedName("end_lat") val endLat: Double,
    @SerializedName("end_lng") val endLng: Double,
    @SerializedName("route_json") val routeJson: String? = null
)

data class Friend(
    val id: Int, @SerializedName("user_id") val userId: Int,
    @SerializedName("friend_id") val friendId: Int, val status: String,
    val friend: User?, val user: User?
)

data class FriendRequest(@SerializedName("friend_id") val friendId: Int)
data class FriendResponse(@SerializedName("request_id") val requestId: Int, val action: String)

data class MonthlyStats(
    @SerializedName("total_runs") val totalRuns: Int,
    @SerializedName("total_km") val totalKm: Double,
    @SerializedName("total_calories") val totalCalories: Int,
    @SerializedName("avg_pace") val avgPace: Double
)

data class WeeklyCompare(
    @SerializedName("current_week_km") val currentWeekKm: Double,
    @SerializedName("previous_week_km") val previousWeekKm: Double,
    @SerializedName("difference_km") val differenceKm: Double,
    val percentage: Double?
)

data class MonthlyCompare(
    @SerializedName("current_month_km") val currentMonthKm: Double,
    @SerializedName("previous_month_km") val previousMonthKm: Double,
    @SerializedName("difference_km") val differenceKm: Double,
    val percentage: Double?
)

data class LeaderboardEntry(
    val position: Int, @SerializedName("user_id") val userId: Int,
    val name: String, @SerializedName("photo_url") val photoUrl: String?,
    val country: String, @SerializedName("km_this_month") val kmThisMonth: Double,
    @SerializedName("is_me") val isMe: Boolean
)
