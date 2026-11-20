package org.fossify.commons.views

import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.RectF
import android.graphics.drawable.Drawable

/**
 * shiroikuma (sk17): the two parts of the family's switch for a [MyMaterialSwitch] — the TRACK, a fully
 * round outline with nothing inside it, and the THUMB, a solid dot. Neither is ever filled as a slab. The
 * switch tints both (its track and thumb tint lists, blended while the dot travels), so each part only
 * keeps the colour it is handed.
 */
class FamilySwitchDrawable(
    private val track: Boolean,
    private val widthPx: Int,
    private val heightPx: Int,
    private val strokePx: Float,
    private val dotRadiusPx: Float,
) : Drawable() {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val rect = RectF()
    private var tint: ColorStateList? = null

    override fun draw(canvas: Canvas) {
        val b = bounds
        paint.color = tint?.getColorForState(state, Color.TRANSPARENT) ?: Color.TRANSPARENT
        if (track) {
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = strokePx
            val half = strokePx / 2f
            rect.set(b.left + half, b.top + half, b.right - half, b.bottom - half)
            canvas.drawRoundRect(rect, rect.height() / 2f, rect.height() / 2f, paint)
        } else {
            paint.style = Paint.Style.FILL
            canvas.drawCircle(b.exactCenterX(), b.exactCenterY(), dotRadiusPx, paint)
        }
    }

    override fun setTintList(tint: ColorStateList?) {
        this.tint = tint
        invalidateSelf()
    }

    override fun isStateful() = true

    override fun onStateChange(state: IntArray): Boolean {
        invalidateSelf()
        return true
    }

    override fun getIntrinsicWidth() = widthPx

    override fun getIntrinsicHeight() = heightPx

    override fun setAlpha(alpha: Int) {}

    override fun setColorFilter(colorFilter: ColorFilter?) {}

    @Deprecated("Deprecated in Java")
    override fun getOpacity() = PixelFormat.TRANSLUCENT
}
