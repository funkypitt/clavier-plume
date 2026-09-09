package org.futo.inputmethod.latin.plume

import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.RectF
import android.graphics.drawable.Drawable

/**
 * Aperçu de touche « ballon » à la manière d'iOS : la touche semble grandir vers le haut.
 * La vue d'aperçu couvre la touche elle-même (bas) et une zone au-dessus (haut) ; on
 * dessine une bulle large en haut, un col qui se resserre jusqu'à la largeur de la touche,
 * et le corps de la touche en bas — le tout d'une seule couleur (celle de la touche),
 * avec une ombre portée de 1 dp comme les touches.
 *
 * [colWidthPx] largeur de la touche (le col y aboutit), [topHeightPx] hauteur de la bulle.
 */
class PlumeBalloonDrawable(
    private val fillColor: Int,
    private val shadowColor: Int,
    private val density: Float,
) : Drawable() {
    var colWidthPx: Float = 0f
    var topHeightPx: Float = 0f

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val path = Path()

    private fun buildPath(b: RectF): Path {
        val r = 10f * density            // rayon de la bulle
        val kr = 5f * density            // rayon de la touche
        val w = b.width()
        val col = if (colWidthPx > 0f && colWidthPx < w) colWidthPx else w
        val top = if (topHeightPx > 0f) topHeightPx.coerceAtMost(b.height() - kr * 2) else b.height() * 0.45f
        val inset = (w - col) / 2f
        val colTop = b.top + top          // début du col (bas de la bulle)
        val colH = 14f * density          // hauteur de la transition courbe
        val p = Path()
        // haut de la bulle
        p.moveTo(b.left + r, b.top)
        p.lineTo(b.right - r, b.top)
        p.quadTo(b.right, b.top, b.right, b.top + r)
        // côté droit de la bulle jusqu'au col
        p.lineTo(b.right, colTop - r)
        p.quadTo(b.right, colTop, b.right - r * 0.6f, colTop + r * 0.4f)
        // courbe du col vers la touche (côté droit)
        p.cubicTo(b.right - inset * 0.4f, colTop + colH * 0.6f,
                  b.right - inset, colTop + colH * 0.4f,
                  b.right - inset, colTop + colH)
        // côté droit de la touche
        p.lineTo(b.right - inset, b.bottom - kr)
        p.quadTo(b.right - inset, b.bottom, b.right - inset - kr, b.bottom)
        // bas de la touche
        p.lineTo(b.left + inset + kr, b.bottom)
        p.quadTo(b.left + inset, b.bottom, b.left + inset, b.bottom - kr)
        // côté gauche de la touche puis col
        p.lineTo(b.left + inset, colTop + colH)
        p.cubicTo(b.left + inset, colTop + colH * 0.4f,
                  b.left + inset * 0.4f, colTop + colH * 0.6f,
                  b.left + r * 0.6f, colTop + r * 0.4f)
        p.quadTo(b.left, colTop, b.left, colTop - r)
        p.lineTo(b.left, b.top + r)
        p.quadTo(b.left, b.top, b.left + r, b.top)
        p.close()
        return p
    }

    override fun draw(canvas: Canvas) {
        val b = RectF(bounds)
        // marge pour l'ombre à droite/en bas
        b.right -= 1f; b.bottom -= 1.5f * density
        val body = buildPath(b)
        paint.color = shadowColor
        canvas.save(); canvas.translate(0f, 1.5f * density); canvas.drawPath(body, paint); canvas.restore()
        paint.color = fillColor
        canvas.drawPath(body, paint)
    }

    override fun setAlpha(alpha: Int) { paint.alpha = alpha }
    override fun setColorFilter(colorFilter: ColorFilter?) { paint.colorFilter = colorFilter }
    @Deprecated("Deprecated in Java")
    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT
}
