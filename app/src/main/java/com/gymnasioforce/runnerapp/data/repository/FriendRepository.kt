package com.gymnasioforce.runnerapp.data.repository

import com.gymnasioforce.runnerapp.network.*

class FriendRepository {
    private val api = RetrofitClient.api

    suspend fun getFriends(): Result<List<Friend>> {
        return try {
            val resp = api.getFriends()
            if (resp.isSuccessful && resp.body()?.success == true) {
                Result.success(resp.body()!!.data!!)
            } else Result.success(emptyList())
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun getPendingRequests(): Result<List<Friend>> {
        return try {
            val resp = api.getPendingRequests()
            if (resp.isSuccessful && resp.body()?.success == true) {
                Result.success(resp.body()!!.data!!)
            } else Result.success(emptyList())
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun getUsersByCountry(): Result<List<User>> {
        return try {
            val resp = api.getUsersByCountry()
            if (resp.isSuccessful && resp.body()?.success == true) {
                Result.success(resp.body()!!.data!!)
            } else Result.success(emptyList())
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun sendRequest(userId: Int): Result<Unit> {
        return try {
            val resp = api.sendFriendRequest(FriendRequest(userId))
            if (resp.isSuccessful) Result.success(Unit)
            else Result.failure(Exception("Error"))
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun respondRequest(requestId: Int, action: String): Result<Unit> {
        return try {
            val resp = api.respondFriendRequest(FriendResponse(requestId, action))
            if (resp.isSuccessful) Result.success(Unit)
            else Result.failure(Exception("Error"))
        } catch (e: Exception) { Result.failure(e) }
    }
}
