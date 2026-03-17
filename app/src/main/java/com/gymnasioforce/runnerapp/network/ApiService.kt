package com.gymnasioforce.runnerapp.network

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // Auth
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<ApiResponse<Map<String, Any>>>

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<ApiResponse<AuthResponse>>

    @POST("auth/verify-email")
    suspend fun verifyEmail(@Body request: VerifyEmailRequest): Response<ApiResponse<AuthResponse>>

    @POST("auth/resend-code")
    suspend fun resendCode(@Body body: Map<String, String>): Response<ApiResponse<Map<String, String>>>

    @POST("auth/forgot-password")
    suspend fun forgotPassword(@Body body: Map<String, String>): Response<ApiResponse<Map<String, String>>>

    @POST("auth/reset-password")
    suspend fun resetPassword(@Body body: Map<String, String>): Response<ApiResponse<Map<String, String>>>

    @POST("auth/logout")
    suspend fun logout(): Response<ApiResponse<Map<String, String>>>

    // Usuario
    @GET("user/profile")
    suspend fun getProfile(): Response<ApiResponse<User>>

    @POST("user/update")
    suspend fun updateProfile(@Body body: Map<String, String>): Response<ApiResponse<User>>

    @Multipart
    @POST("user/upload-photo")
    suspend fun uploadPhoto(@Part photo: MultipartBody.Part): Response<ApiResponse<Map<String, String>>>

    @GET("users/by-country")
    suspend fun getUsersByCountry(): Response<ApiResponse<List<User>>>

    // Carreras
    @POST("runs")
    suspend fun saveRun(@Body request: SaveRunRequest): Response<ApiResponse<Run>>

    @GET("runs")
    suspend fun getRuns(): Response<ApiResponse<List<Run>>>

    @GET("runs/{id}")
    suspend fun getRun(@Path("id") id: Int): Response<ApiResponse<Run>>

    @DELETE("runs/{id}")
    suspend fun deleteRun(@Path("id") id: Int): Response<ApiResponse<Map<String, String>>>

    // Amigos
    @POST("friends/request")
    suspend fun sendFriendRequest(@Body request: FriendRequest): Response<ApiResponse<Friend>>

    @POST("friends/respond")
    suspend fun respondFriendRequest(@Body response: FriendResponse): Response<ApiResponse<Friend>>

    @GET("friends")
    suspend fun getFriends(): Response<ApiResponse<List<Friend>>>

    @GET("friends/pending")
    suspend fun getPendingRequests(): Response<ApiResponse<List<Friend>>>

    // Estadisticas
    @GET("stats/monthly")
    suspend fun getMonthlyStats(): Response<ApiResponse<MonthlyStats>>

    @GET("stats/weekly-compare")
    suspend fun getWeeklyCompare(): Response<ApiResponse<WeeklyCompare>>

    @GET("stats/monthly-compare")
    suspend fun getMonthlyCompare(): Response<ApiResponse<MonthlyCompare>>

    @GET("stats/leaderboard")
    suspend fun getLeaderboard(): Response<ApiResponse<List<LeaderboardEntry>>>

    // Charts
    @GET("charts/weekly")
    suspend fun getWeeklyChart(): Response<ApiResponse<List<DailyChartData>>>

    @GET("charts/monthly")
    suspend fun getMonthlyChart(): Response<ApiResponse<List<MonthlyChartData>>>

    // FCM token
    @POST("user/fcm-token")
    suspend fun updateFcmToken(@Body body: Map<String, String>): Response<ApiResponse<Any>>

    // Foto de carrera
    @Multipart
    @POST("runs/{id}/photo")
    suspend fun uploadRunPhoto(
        @Path("id") runId: Int,
        @Part photo: MultipartBody.Part
    ): Response<ApiResponse<Map<String, String>>>

    // Eliminar cuenta
    @DELETE("user/account")
    suspend fun deleteAccount(): Response<ApiResponse<Any>>
}
