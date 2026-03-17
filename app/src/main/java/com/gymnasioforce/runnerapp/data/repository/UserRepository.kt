package com.gymnasioforce.runnerapp.data.repository

import com.gymnasioforce.runnerapp.network.*
import okhttp3.MultipartBody

class UserRepository {
    private val api = RetrofitClient.api

    suspend fun getProfile(): Result<User> {
        return try {
            val resp = api.getProfile()
            if (resp.isSuccessful && resp.body()?.success == true) {
                Result.success(resp.body()!!.data!!)
            } else Result.failure(Exception("Error"))
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun updateProfile(name: String, country: String): Result<User> {
        return try {
            val resp = api.updateProfile(mapOf("name" to name, "country" to country))
            if (resp.isSuccessful && resp.body()?.success == true) {
                Result.success(resp.body()!!.data!!)
            } else Result.failure(Exception("Error al guardar"))
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun uploadPhoto(photo: MultipartBody.Part): Result<String?> {
        return try {
            val resp = api.uploadPhoto(photo)
            if (resp.isSuccessful) {
                Result.success(resp.body()?.data?.get("photo_url"))
            } else Result.failure(Exception("Error subiendo foto"))
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun deleteAccount(): Result<Unit> {
        return try {
            val resp = api.deleteAccount()
            if (resp.isSuccessful) Result.success(Unit)
            else Result.failure(Exception("Error eliminando cuenta"))
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun updateFcmToken(token: String): Result<Unit> {
        return try {
            api.updateFcmToken(mapOf("fcm_token" to token))
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun logout(): Result<Unit> {
        return try {
            api.logout()
            Result.success(Unit)
        } catch (_: Exception) { Result.success(Unit) }
    }
}
