package org.fossify.commons.extensions

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable

// Fork (sk12): the pill button look shared by commons' dialogs and the consuming apps' own action pills.
// Always outlined — an accent frame on the background colour, never filled. The MAIN action of a block is
// told apart by weight: a thick frame (and, set by the caller, bold text); every other pill has a thin one.

private const val PILL_RADIUS_DP = 100f
private const val PILL_STROKE_MAIN_DP = 3f
private const val PILL_STROKE_DP = 1.25f
private const val PILL_RIPPLE_ALPHA = 0.3f

fun Context.pillDrawable(accent: Int, background: Int, main: Boolean): Drawable {
    val density = resources.displayMetrics.density
    val face = GradientDrawable().apply {
        cornerRadius = PILL_RADIUS_DP * density
        setColor(background)
        setStroke(((if (main) PILL_STROKE_MAIN_DP else PILL_STROKE_DP) * density).toInt().coerceAtLeast(1), accent)
    }
    return RippleDrawable(ColorStateList.valueOf(accent.adjustAlpha(PILL_RIPPLE_ALPHA)), face, null)
}
