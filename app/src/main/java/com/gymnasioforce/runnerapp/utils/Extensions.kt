package com.gymnasioforce.runnerapp.utils

import android.app.Activity
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.gymnasioforce.runnerapp.BuildConfig

fun Activity.showToast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
fun Fragment.showToast(msg: String) = Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()

// Resolver URLs de fotos: si es ruta relativa, agregar base del servidor
fun resolvePhotoUrl(url: String?): String? {
    if (url.isNullOrEmpty()) return null
    if (url.startsWith("http")) return url
    // BASE_URL termina en /api/ — necesitamos la raiz del servidor
    val baseUrl = BuildConfig.BASE_URL.removeSuffix("api/").removeSuffix("/")
    return "$baseUrl$url"
}
