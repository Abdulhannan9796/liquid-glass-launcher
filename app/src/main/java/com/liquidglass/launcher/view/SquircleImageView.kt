package com.liquidglass.launcher.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Path
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import com.liquidglass.launcher.util.IconShaper

class SquircleImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : AppCompatImageView(context, attrs) {

    private val path = Path()
    private var pathSize = -1f

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val size = minOf(w, h).toFloat()
        if (size > 0f && size != pathSize) {
            pathSize = size
            path.set(IconShaper.squirclePath(size))
        }
    }

    override fun onDraw(canvas: Canvas) {
        if (pathSize > 0f) {
            val save = canvas.save()
            canvas.clipPath(path)
            super.onDraw(canvas)
            canvas.restoreToCount(save)
        } else {
            super.onDraw(canvas)
        }
    }
}
