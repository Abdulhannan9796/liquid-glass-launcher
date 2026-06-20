package com.liquidglass.launcher.view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.util.AttributeSet
import android.widget.FrameLayout

/**
 * A translucent frosted-glass surface. Draws a crop of the pre-blurred
 * wallpaper aligned to this view's on-screen position, layers a soft tint
 * and a thin highlight border on top, and clips its children to a rounded
 * shape — the "liquid glass" card look used throughout the launcher.
 */
class GlassPanel @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    var blurredWallpaper: Bitmap? = null
        set(value) {
            field = value
            invalidate()
        }

    var cornerRadiusPx: Float = 48f
        set(value) {
            field = value
            if (width > 0 && height > 0) rebuildClipPath(width.toFloat(), height.toFloat())
            invalidate()
        }

    var tintColor: Int = 0x33FFFFFF
        set(value) {
            field = value
            invalidate()
        }

    var strokeColor: Int = 0x40FFFFFF
        set(value) {
            field = value
            invalidate()
        }

    private val clipPath = Path()
    private val tintPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2f
    }
    private val bitmapPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    private val locationBuffer = IntArray(2)

    init {
        setWillNotDraw(false)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        rebuildClipPath(w.toFloat(), h.toFloat())
    }

    private fun rebuildClipPath(w: Float, h: Float) {
        clipPath.reset()
        val rect = RectF(0f, 0f, w, h)
        clipPath.addRoundRect(rect, cornerRadiusPx, cornerRadiusPx, Path.Direction.CW)
    }

    override fun dispatchDraw(canvas: Canvas) {
        val saveCount = canvas.save()
        canvas.clipPath(clipPath)

        blurredWallpaper?.let { wallpaper ->
            getLocationInWindow(locationBuffer)
            val left = locationBuffer[0]
            val top = locationBuffer[1]
            val srcRect = Rect(
                left.coerceIn(0, wallpaper.width),
                top.coerceIn(0, wallpaper.height),
                (left + width).coerceIn(0, wallpaper.width),
                (top + height).coerceIn(0, wallpaper.height)
            )
            if (srcRect.width() > 0 && srcRect.height() > 0) {
                canvas.drawBitmap(
                    wallpaper, srcRect,
                    RectF(0f, 0f, width.toFloat(), height.toFloat()),
                    bitmapPaint
                )
            }
        }

        tintPaint.color = tintColor
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), tintPaint)

        // children draw while the clip is still active, so content never
        // spills past the rounded glass edge
        super.dispatchDraw(canvas)

        canvas.restoreToCount(saveCount)

        // crisp, unclipped border drawn last so it isn't cut in half
        strokePaint.color = strokeColor
        canvas.drawPath(clipPath, strokePaint)
    }
}
