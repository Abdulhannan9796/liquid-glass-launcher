package com.liquidglass.launcher.util

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.util.Log

/**
 * Generates a soft "frosted glass" backdrop from the device wallpaper.
 *
 * Design note: true real-time backdrop blur (blurring whatever is live
 * behind a translucent panel, frame by frame) isn't something a regular
 * Android app can do — that level of compositor access isn't exposed to
 * apps. Every glass-style Android launcher fakes it the same way: blur the
 * wallpaper once, then crop the blurred copy to line up behind each glass
 * panel. That's what this does. It only re-runs when the wallpaper changes,
 * not per frame, so it's cheap even on lower-end chipsets.
 */
object BlurUtil {

    private const val DOWNSCALE = 10
    private const val RADIUS = 6
    private const val PASSES = 2

    fun blurredWallpaper(context: Context, targetW: Int, targetH: Int): Bitmap {
        val full = currentWallpaperBitmap(context, targetW, targetH)
        val smallW = (targetW / DOWNSCALE).coerceAtLeast(8)
        val smallH = (targetH / DOWNSCALE).coerceAtLeast(8)
        val small = Bitmap.createScaledBitmap(full, smallW, smallH, true)

        var blurred = small
        repeat(PASSES) { blurred = boxBlur(blurred, RADIUS) }

        return Bitmap.createScaledBitmap(blurred, targetW, targetH, true)
    }

    private fun currentWallpaperBitmap(context: Context, targetW: Int, targetH: Int): Bitmap {
        return try {
            val drawable = WallpaperManager.getInstance(context).drawable
            val bmp = Bitmap.createBitmap(targetW, targetH, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bmp)
            drawable?.setBounds(0, 0, targetW, targetH)
            drawable?.draw(canvas)
            bmp
        } catch (e: Exception) {
            Log.w("BlurUtil", "Could not read wallpaper, using fallback color: ${e.message}")
            val bmp = Bitmap.createBitmap(targetW, targetH, Bitmap.Config.ARGB_8888)
            Canvas(bmp).drawColor(0xFF1C1C1E.toInt())
            bmp
        }
    }

    /** Simple, dependency-free box blur. Run on a tiny bitmap, so brute force is fine. */
    private fun boxBlur(bitmap: Bitmap, radius: Int): Bitmap {
        val w = bitmap.width
        val h = bitmap.height
        val src = IntArray(w * h)
        bitmap.getPixels(src, 0, w, 0, 0, w, h)
        val dst = IntArray(w * h)

        for (y in 0 until h) {
            for (x in 0 until w) {
                var a = 0
                var r = 0
                var g = 0
                var b = 0
                var count = 0
                for (dy in -radius..radius) {
                    val yy = y + dy
                    if (yy < 0 || yy >= h) continue
                    val rowBase = yy * w
                    for (dx in -radius..radius) {
                        val xx = x + dx
                        if (xx < 0 || xx >= w) continue
                        val p = src[rowBase + xx]
                        a += (p ushr 24) and 0xFF
                        r += (p ushr 16) and 0xFF
                        g += (p ushr 8) and 0xFF
                        b += p and 0xFF
                        count++
                    }
                }
                dst[y * w + x] =
                    ((a / count) shl 24) or ((r / count) shl 16) or ((g / count) shl 8) or (b / count)
            }
        }

        val result = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        result.setPixels(dst, 0, w, 0, 0, w, h)
        return result
    }
}
