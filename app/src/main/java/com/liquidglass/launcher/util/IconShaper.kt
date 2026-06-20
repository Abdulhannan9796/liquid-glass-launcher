package com.liquidglass.launcher.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Path
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sign

object IconShaper {

    /**
     * Builds a superellipse ("squircle") path matching the curvature of an
     * iOS-style app icon. [exponent] of ~5 gives the iOS look; lower values
     * are rounder (closer to a circle), higher values are more square.
     */
    fun squirclePath(size: Float, exponent: Double = 5.0): Path {
        val path = Path()
        val r = size / 2f
        val steps = 120
        for (i in 0..steps) {
            val t = 2.0 * PI * i / steps
            val cosT = cos(t)
            val sinT = sin(t)
            val x = (r + r * sign(cosT) * abs(cosT).pow(2.0 / exponent)).toFloat()
            val y = (r + r * sign(sinT) * abs(sinT).pow(2.0 / exponent)).toFloat()
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        path.close()
        return path
    }

    /** Renders [source] into a square bitmap clipped to a squircle of [sizePx]. */
    fun toSquircle(source: Drawable, sizePx: Int): Bitmap {
        val srcBitmap = drawableToBitmap(source, sizePx)
        val output = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val path = squirclePath(sizePx.toFloat())
        canvas.save()
        canvas.clipPath(path)
        canvas.drawBitmap(srcBitmap, 0f, 0f, null)
        canvas.restore()
        return output
    }

    private fun drawableToBitmap(drawable: Drawable, sizePx: Int): Bitmap {
        if (drawable is BitmapDrawable &&
            drawable.bitmap.width == sizePx &&
            drawable.bitmap.height == sizePx
        ) {
            return drawable.bitmap
        }
        val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, sizePx, sizePx)
        drawable.draw(canvas)
        return bitmap
    }
}
