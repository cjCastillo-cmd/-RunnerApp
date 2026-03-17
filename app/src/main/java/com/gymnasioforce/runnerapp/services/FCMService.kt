package com.gymnasioforce.runnerapp.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.gymnasioforce.runnerapp.R
import com.gymnasioforce.runnerapp.network.RetrofitClient
import com.gymnasioforce.runnerapp.ui.main.MainActivity
import com.gymnasioforce.runnerapp.utils.Prefs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Servicio FCM — requiere google-services.json para compilar con Firebase.
 * Mientras no se configure Firebase, este archivo se puede excluir del build
 * o mantener comentada la herencia de FirebaseMessagingService.
 */
class FCMService : android.app.Service() {

    // Cuando se integre Firebase, cambiar a:
    // class FCMService : FirebaseMessagingService()

    override fun onBind(intent: Intent?) = null

    fun onNewToken(token: String) {
        val prefs = Prefs(this)
        if (prefs.token.isNotEmpty()) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    RetrofitClient.api.updateFcmToken(mapOf("fcm_token" to token))
                } catch (_: Exception) {}
            }
        }
    }

    fun handleMessage(title: String, body: String) {
        showNotification(title, body)
    }

    private fun showNotification(title: String, body: String) {
        val channelId = "social_notifications"
        val nm = getSystemService(NotificationManager::class.java)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, getString(R.string.notif_channel_social),
                NotificationManager.IMPORTANCE_DEFAULT
            )
            nm.createNotificationChannel(channel)
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pending = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_run)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pending)
            .build()

        nm.notify(System.currentTimeMillis().toInt(), notification)
    }
}
