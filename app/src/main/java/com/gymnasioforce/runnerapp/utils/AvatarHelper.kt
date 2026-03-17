package com.gymnasioforce.runnerapp.utils

import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.content.Context

object AvatarHelper {

    fun generateInitials(context: Context, name: String, sizeDp: Int = 44): Drawable {
        val density = context.resources.displayMetrics.density
        val sizePx = (sizeDp * density).toInt()

        val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Fondo gris oscuro
        val bgPaint = Paint().apply {
            color = Color.parseColor("#2A2A2A")
            isAntiAlias = true
        }
        canvas.drawCircle(sizePx / 2f, sizePx / 2f, sizePx / 2f, bgPaint)

        // Iniciales en volt
        val initials = name.split(" ")
            .take(2)
            .mapNotNull { it.firstOrNull()?.uppercaseChar() }
            .joinToString("")
            .ifEmpty { "?" }

        val textPaint = Paint().apply {
            color = Color.parseColor("#C8FF00")
            textSize = sizePx * 0.38f
            typeface = Typeface.create("sans-serif-condensed", Typeface.BOLD)
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }

        val textBounds = Rect()
        textPaint.getTextBounds(initials, 0, initials.length, textBounds)
        val y = sizePx / 2f + textBounds.height() / 2f

        canvas.drawText(initials, sizePx / 2f, y, textPaint)

        return BitmapDrawable(context.resources, bitmap)
    }
}
