package com.gymnasioforce.runnerapp.services

import android.annotation.SuppressLint
import android.app.*
import android.content.Intent
import android.location.Location
import android.os.*
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*
import com.gymnasioforce.runnerapp.R
import com.gymnasioforce.runnerapp.ui.run.RunningActivity

class RunningService : Service() {

    companion object {
        const val CHANNEL_ID      = "running_channel"
        const val MILESTONE_CH_ID = "milestone_channel"
        const val ACTION_START    = "ACTION_START"
        const val ACTION_STOP     = "ACTION_STOP"

        var isRunning     = false
        var totalDistance = 0.0
        var durationSec   = 0
        var startLocation: Location? = null
        var lastLocation:  Location? = null
        val routePoints   = mutableListOf<Map<String, Double>>()
    }

    private lateinit var fusedClient: FusedLocationProviderClient
    private val handler = Handler(Looper.getMainLooper())
    private val binder  = LocalBinder()
    private var lastMilestoneKm = 0

    inner class LocalBinder : Binder() {
        fun getService(): RunningService = this@RunningService
    }

    override fun onBind(intent: Intent?) = binder

    override fun onCreate() {
        super.onCreate()
        fusedClient = LocationServices.getFusedLocationProviderClient(this)
        createNotificationChannel()
        createMilestoneChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startRun()
            ACTION_STOP  -> stopRun()
        }
        return START_NOT_STICKY
    }

    private fun startRun() {
        isRunning     = true
        totalDistance = 0.0
        durationSec   = 0
        startLocation = null
        lastLocation  = null
        lastMilestoneKm = 0
        routePoints.clear()

        startForeground(1, buildNotification(getString(R.string.notif_run_started)))
        startLocationUpdates()
        startTimer()
    }

    private fun stopRun() {
        isRunning = false
        handler.removeCallbacksAndMessages(null)
        fusedClient.removeLocationUpdates(locationCallback)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun startTimer() {
        handler.post(object : Runnable {
            override fun run() {
                if (isRunning) {
                    durationSec++
                    handler.postDelayed(this, 1000)
                }
            }
        })
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        val req = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 3000L)
            .setMinUpdateDistanceMeters(5f)
            .build()

        fusedClient.requestLocationUpdates(req, locationCallback, Looper.getMainLooper())
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            val location = result.lastLocation ?: return

            if (startLocation == null) startLocation = location

            lastLocation?.let { prev ->
                val dist = prev.distanceTo(location)
                if (dist > 3) {
                    totalDistance += dist
                    routePoints.add(mapOf("lat" to location.latitude, "lng" to location.longitude))
                }
            }
            lastLocation = location

            val km = totalDistance / 1000.0
            updateNotification(getString(R.string.notif_running).format(km))

            // Notificacion por cada km completado
            val currentKm = km.toInt()
            if (currentKm > lastMilestoneKm && currentKm > 0) {
                lastMilestoneKm = currentKm
                notifyMilestone(currentKm)
                vibrate()
            }
        }
    }

    private fun notifyMilestone(km: Int) {
        val nm = getSystemService(NotificationManager::class.java)
        val notification = NotificationCompat.Builder(this, MILESTONE_CH_ID)
            .setContentTitle(getString(R.string.notif_milestone_title, km))
            .setContentText(getString(R.string.notif_milestone_text, km))
            .setSmallIcon(R.drawable.ic_run)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        nm.notify(100 + km, notification)
    }

    @Suppress("DEPRECATION")
    private fun vibrate() {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = getSystemService(VibratorManager::class.java)
            vm.defaultVibrator
        } else {
            getSystemService(Vibrator::class.java)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            vibrator.vibrate(500)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, getString(R.string.notif_channel_running), NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    private fun createMilestoneChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                MILESTONE_CH_ID, getString(R.string.notif_channel_milestone), NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = getString(R.string.notif_channel_milestone_desc)
                enableVibration(true)
            }
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    private fun buildNotification(text: String): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, RunningActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Runner App")
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_run)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    private fun updateNotification(text: String) {
        val nm = getSystemService(NotificationManager::class.java)
        nm.notify(1, buildNotification(text))
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }
}
