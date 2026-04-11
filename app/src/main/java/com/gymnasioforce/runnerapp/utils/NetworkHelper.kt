package com.gymnasioforce.runnerapp.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

/**
 * Helper para verificar conectividad a internet.
 * Usado para decidir si hacer llamadas a la API o usar datos locales.
 */
object NetworkHelper {

    // Verifica si hay conexion a internet disponible
    fun isOnline(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val capabilities = cm.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}
