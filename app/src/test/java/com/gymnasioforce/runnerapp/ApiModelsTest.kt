package com.gymnasioforce.runnerapp

import com.google.gson.Gson
import com.gymnasioforce.runnerapp.network.*
import org.junit.Assert.*
import org.junit.Test

class ApiModelsTest {

    private val gson = Gson()

    @Test
    fun `ApiResponse deserializa correctamente con success true`() {
        val json = """{"success": true, "data": null, "message": "OK"}"""
        val resp = gson.fromJson(json, ApiResponse::class.java)
        assertTrue(resp.success)
        assertEquals("OK", resp.message)
    }

    @Test
    fun `ApiResponse deserializa correctamente con success false`() {
        val json = """{"success": false, "message": "Error"}"""
        val resp = gson.fromJson(json, ApiResponse::class.java)
        assertFalse(resp.success)
        assertNull(resp.data)
    }

    @Test
    fun `LoginRequest serializa campos correctamente`() {
        val req = LoginRequest("test@test.com", "123456")
        val json = gson.toJson(req)
        assertTrue(json.contains("\"email\":\"test@test.com\""))
        assertTrue(json.contains("\"password\":\"123456\""))
    }

    @Test
    fun `RegisterRequest usa SerializedName para password_confirmation`() {
        val req = RegisterRequest("Juan", "j@test.com", "pass", "pass", "Mexico")
        val json = gson.toJson(req)
        assertTrue(json.contains("password_confirmation"))
        assertFalse(json.contains("passwordConfirmation"))
    }

    @Test
    fun `Run deserializa snake_case a camelCase`() {
        val json = """{
            "id": 1, "user_id": 5, "distance_km": 5.5, "calories": 385,
            "duration_sec": 1800, "start_lat": 19.4, "start_lng": -99.1,
            "end_lat": 19.5, "end_lng": -99.0, "avg_pace": 5.45,
            "route_json": null, "created_at": "2026-03-16"
        }"""
        val run = gson.fromJson(json, Run::class.java)
        assertEquals(1, run.id)
        assertEquals(5, run.userId)
        assertEquals(5.5, run.distanceKm, 0.01)
        assertEquals(385, run.calories)
        assertEquals(1800, run.durationSec)
        assertEquals(5.45, run.avgPace!!, 0.01)
        assertNull(run.routeJson)
    }

    @Test
    fun `SaveRunRequest serializa a snake_case`() {
        val req = SaveRunRequest(10.0, 3600, 19.4, -99.1, 19.5, -99.0)
        val json = gson.toJson(req)
        assertTrue(json.contains("distance_km"))
        assertTrue(json.contains("duration_sec"))
        assertTrue(json.contains("start_lat"))
        assertFalse(json.contains("distanceKm"))
    }

    @Test
    fun `MonthlyStats deserializa correctamente`() {
        val json = """{"total_runs": 10, "total_km": 42.5, "total_calories": 2975, "avg_pace": 5.3}"""
        val stats = gson.fromJson(json, MonthlyStats::class.java)
        assertEquals(10, stats.totalRuns)
        assertEquals(42.5, stats.totalKm, 0.01)
        assertEquals(2975, stats.totalCalories)
        assertEquals(5.3, stats.avgPace, 0.01)
    }

    @Test
    fun `WeeklyCompare calcula diferencia correctamente`() {
        val json = """{"current_week_km": 15.0, "previous_week_km": 10.0, "difference_km": 5.0, "percentage": 50.0}"""
        val compare = gson.fromJson(json, WeeklyCompare::class.java)
        assertEquals(15.0, compare.currentWeekKm, 0.01)
        assertEquals(5.0, compare.differenceKm, 0.01)
    }

    @Test
    fun `User deserializa campos opcionales como null`() {
        val json = """{"id": 1, "name": "Juan", "email": "j@t.com", "country": "MX", "photo_url": null, "email_verified": true, "total_km": 0.0, "total_calories": 0}"""
        val user = gson.fromJson(json, User::class.java)
        assertNull(user.photoUrl)
        assertTrue(user.emailVerified)
    }

    @Test
    fun `LeaderboardEntry identifica usuario propio`() {
        val json = """{"position": 1, "user_id": 5, "name": "Juan", "photo_url": null, "country": "MX", "km_this_month": 50.0, "is_me": true}"""
        val entry = gson.fromJson(json, LeaderboardEntry::class.java)
        assertTrue(entry.isMe)
        assertEquals(50.0, entry.kmThisMonth, 0.01)
    }
}