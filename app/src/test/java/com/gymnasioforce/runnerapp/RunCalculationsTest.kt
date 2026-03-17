package com.gymnasioforce.runnerapp

import org.junit.Assert.*
import org.junit.Test

class RunCalculationsTest {

    // Replica la logica de calculo de calorias usada en RunningActivity
    private fun calculateCalories(km: Double): Int = (km * 70).toInt()

    // Replica el formateo de duracion
    private fun formatDuration(sec: Int): String =
        "%02d:%02d:%02d".format(sec / 3600, (sec % 3600) / 60, sec % 60)

    // Replica el formateo de timer corto
    private fun formatTimer(sec: Int): String = "%02d:%02d".format(sec / 60, sec % 60)

    // Replica el formateo de pace
    private fun formatPace(pace: Double): String =
        if (pace > 0) "${pace.toInt()}:${"%02d".format(((pace % 1) * 60).toInt())}" else "-"

    // Replica la validacion de distancia minima
    private fun isRunTooShort(km: Double): Boolean = km < 0.01

    // --- Tests de Calorias ---

    @Test
    fun `calorias para 0 km es 0`() {
        assertEquals(0, calculateCalories(0.0))
    }

    @Test
    fun `calorias para 1 km es 70`() {
        assertEquals(70, calculateCalories(1.0))
    }

    @Test
    fun `calorias para 5 km es 350`() {
        assertEquals(350, calculateCalories(5.0))
    }

    @Test
    fun `calorias para 42_195 km (maraton) es 2953`() {
        assertEquals(2953, calculateCalories(42.195))
    }

    @Test
    fun `calorias para distancia pequena trunca correctamente`() {
        assertEquals(3, calculateCalories(0.05))
    }

    // --- Tests de Formato de Duracion ---

    @Test
    fun `formato duracion 0 segundos`() {
        assertEquals("00:00:00", formatDuration(0))
    }

    @Test
    fun `formato duracion 1 hora exacta`() {
        assertEquals("01:00:00", formatDuration(3600))
    }

    @Test
    fun `formato duracion 30 minutos`() {
        assertEquals("00:30:00", formatDuration(1800))
    }

    @Test
    fun `formato duracion 1h 23m 45s`() {
        assertEquals("01:23:45", formatDuration(5025))
    }

    @Test
    fun `formato duracion mas de 10 horas`() {
        assertEquals("10:00:00", formatDuration(36000))
    }

    // --- Tests de Timer Corto ---

    @Test
    fun `timer 0 segundos`() {
        assertEquals("00:00", formatTimer(0))
    }

    @Test
    fun `timer 5 minutos`() {
        assertEquals("05:00", formatTimer(300))
    }

    @Test
    fun `timer 90 segundos`() {
        assertEquals("01:30", formatTimer(90))
    }

    // --- Tests de Pace ---

    @Test
    fun `pace 0 muestra guion`() {
        assertEquals("-", formatPace(0.0))
    }

    @Test
    fun `pace 5_30 min por km`() {
        assertEquals("5:30", formatPace(5.5))
    }

    @Test
    fun `pace 4_00 min por km`() {
        assertEquals("4:00", formatPace(4.0))
    }

    @Test
    fun `pace 6_15 min por km`() {
        assertEquals("6:15", formatPace(6.25))
    }

    // --- Tests de Distancia Minima ---

    @Test
    fun `0 metros es muy corto`() {
        assertTrue(isRunTooShort(0.0))
    }

    @Test
    fun `5 metros es muy corto`() {
        assertTrue(isRunTooShort(0.005))
    }

    @Test
    fun `10 metros es el limite`() {
        assertFalse(isRunTooShort(0.01))
    }

    @Test
    fun `1 km no es muy corto`() {
        assertFalse(isRunTooShort(1.0))
    }

    // --- Tests de formato KM ---

    @Test
    fun `formato km con 2 decimales`() {
        assertEquals("5.50", "%.2f".format(5.5))
    }

    @Test
    fun `formato km con 1 decimal`() {
        assertEquals("42.2", "%.1f".format(42.195))
    }
}