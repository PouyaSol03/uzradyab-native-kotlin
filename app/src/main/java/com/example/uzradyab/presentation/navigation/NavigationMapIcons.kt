package com.example.uzradyab.presentation.navigation

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.PathParser
import com.example.uzradyab.R
import com.example.uzradyab.ui.theme.RoutingColors

enum class PillStyle { DURATION_SELECTED, DURATION, ROAD, VEHICLE }

/** A bitmap drawn on the map. [id] is the style image name, so equal icons are drawn once. */
sealed interface MarkerIcon {
    val id: String

    data object Puck : MarkerIcon {
        override val id = "nav_puck"
    }

    data object Vehicle : MarkerIcon {
        override val id = "nav_vehicle"
    }

    /** Pill with [parts] laid out right-to-left, e.g. ["114", "·", "متوقف"]. */
    data class Pill(val parts: List<String>, val style: PillStyle) : MarkerIcon {
        override val id = "nav_pill_${style.name}_${parts.joinToString("_")}"
    }

    data class Maneuver(val kind: ManeuverKind) : MarkerIcon {
        override val id = "nav_maneuver_${kind.name}"
    }

    data class Exit(val number: Int) : MarkerIcon {
        override val id = "nav_exit_$number"
    }
}

/** Draws [MarkerIcon]s with Canvas. Sizes are in dp and scaled by the screen density. */
object NavigationMapIcons {

    private var boldTypeface: Typeface? = null
    private var semiBoldTypeface: Typeface? = null

    fun render(context: Context, icon: MarkerIcon): Bitmap = when (icon) {
        MarkerIcon.Puck -> puck(context)
        MarkerIcon.Vehicle -> vehicle(context)
        is MarkerIcon.Pill -> pill(context, icon.parts, icon.style)
        is MarkerIcon.Maneuver -> maneuver(context, icon.kind)
        is MarkerIcon.Exit -> exitBadge(context, icon.number)
    }

    private fun typeface(context: Context, bold: Boolean): Typeface {
        if (bold) {
            boldTypeface?.let { return it }
        } else {
            semiBoldTypeface?.let { return it }
        }
        val font = runCatching {
            ResourcesCompat.getFont(context, if (bold) R.font.vazirmatn_bold else R.font.vazirmatn_semibold)
        }.getOrNull() ?: Typeface.DEFAULT_BOLD
        if (bold) boldTypeface = font else semiBoldTypeface = font
        return font
    }

    private inline fun draw(context: Context, widthDp: Float, heightDp: Float, block: Canvas.() -> Unit): Bitmap {
        val density = context.resources.displayMetrics.density
        val bitmap = Bitmap.createBitmap(
            (widthDp * density).toInt().coerceAtLeast(1),
            (heightDp * density).toInt().coerceAtLeast(1),
            Bitmap.Config.ARGB_8888,
        )
        val canvas = Canvas(bitmap)
        canvas.scale(density, density)
        canvas.block()
        return bitmap
    }

    private fun Color.argb() = toArgb()

    private val shadowColor = android.graphics.Color.argb(40, 20, 24, 40)

    private fun puck(context: Context): Bitmap = draw(context, 40f, 40f) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.color = android.graphics.Color.WHITE
        paint.setShadowLayer(3f, 0f, 1f, shadowColor)
        drawCircle(20f, 20f, 17f, paint)
        paint.clearShadowLayer()
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        paint.color = android.graphics.Color.argb(31, 20, 24, 40)
        drawCircle(20f, 20f, 17f, paint)

        val arrow = PathParser.createPathFromPathData("M0 -11 L8 9 L0 5 L-8 9 Z")
        save()
        translate(20f, 20f)
        paint.style = Paint.Style.FILL_AND_STROKE
        paint.strokeJoin = Paint.Join.ROUND
        paint.strokeWidth = 2f
        paint.color = RoutingColors.primary.argb()
        drawPath(arrow, paint)
        restore()
    }

    private fun vehicle(context: Context): Bitmap = draw(context, 42f, 42f) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.color = RoutingColors.vehicle.argb()
        paint.setShadowLayer(3f, 0f, 1f, shadowColor)
        drawCircle(21f, 21f, 18f, paint)
        paint.clearShadowLayer()
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 3f
        paint.color = android.graphics.Color.WHITE
        drawCircle(21f, 21f, 18f, paint)

        paint.strokeWidth = 2f
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeJoin = Paint.Join.ROUND
        save()
        translate(11f, 11f)
        scale(20f / 24f, 20f / 24f)
        NavigationIconPaths.car.forEach { drawPath(PathParser.createPathFromPathData(it), paint) }
        restore()
    }

    private fun maneuver(context: Context, kind: ManeuverKind): Bitmap = draw(context, 36f, 36f) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.color = android.graphics.Color.WHITE
        paint.setShadowLayer(3f, 0f, 1f, shadowColor)
        drawCircle(18f, 18f, 15f, paint)
        paint.clearShadowLayer()
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2.5f
        paint.color = RoutingColors.primary.argb()
        drawCircle(18f, 18f, 15f, paint)

        paint.strokeWidth = 2.4f
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeJoin = Paint.Join.ROUND
        save()
        translate(9f, 9f)
        scale(18f / 24f, 18f / 24f)
        NavigationIcons.maneuverPaths(kind).forEach { drawPath(PathParser.createPathFromPathData(it), paint) }
        restore()
    }

    private fun exitBadge(context: Context, number: Int): Bitmap = draw(context, 36f, 36f) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.color = android.graphics.Color.WHITE
        paint.setShadowLayer(3f, 0f, 1f, shadowColor)
        drawCircle(18f, 18f, 15f, paint)
        paint.clearShadowLayer()
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 3f
        paint.color = RoutingColors.primary.argb()
        drawCircle(18f, 18f, 15f, paint)

        paint.style = Paint.Style.FILL
        paint.typeface = typeface(context, bold = true)
        paint.textSize = 15f
        paint.textAlign = Paint.Align.CENTER
        val text = NavigationFormat.number(number)
        val bounds = android.graphics.Rect()
        paint.getTextBounds(text, 0, text.length, bounds)
        drawText(text, 18f, 18f - bounds.exactCenterY(), paint)
    }

    private fun pill(context: Context, parts: List<String>, style: PillStyle): Bitmap {
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = typeface(context, bold = style != PillStyle.VEHICLE)
            textSize = 12f
        }
        val (background, foreground, border) = when (style) {
            PillStyle.DURATION_SELECTED -> Triple(RoutingColors.primary, Color.White, null)
            PillStyle.DURATION -> Triple(Color.White, RoutingColors.onSurface, null)
            PillStyle.ROAD -> Triple(Color.White, RoutingColors.onPrimaryContainer, RoutingColors.primary)
            PillStyle.VEHICLE -> Triple(RoutingColors.vehicle, Color.White, null)
        }
        val gap = 6f
        val dotSize = if (style == PillStyle.ROAD) 6f else 0f
        val horizontalPadding = if (style == PillStyle.VEHICLE) 10f else 12f
        val height = if (style == PillStyle.VEHICLE) 24f else 30f
        val widths = parts.map { textPaint.measureText(it) }
        val contentWidth = widths.sum() + gap * (parts.size - 1).coerceAtLeast(0) +
            if (dotSize > 0) dotSize + gap else 0f
        val minWidth = if (style == PillStyle.DURATION || style == PillStyle.DURATION_SELECTED) 74f else 0f
        val pillWidth = maxOf(minWidth, contentWidth + horizontalPadding * 2)
        val margin = 4f // room for the shadow

        return draw(context, pillWidth + margin * 2, height + margin * 2) {
            val paint = Paint(Paint.ANTI_ALIAS_FLAG)
            val rect = RectF(margin, margin, margin + pillWidth, margin + height)
            paint.color = background.argb()
            paint.setShadowLayer(3f, 0f, 1.5f, shadowColor)
            drawRoundRect(rect, height / 2, height / 2, paint)
            paint.clearShadowLayer()
            if (border != null) {
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 1.5f
                paint.color = border.argb()
                val inset = RectF(rect).apply { inset(0.75f, 0.75f) }
                drawRoundRect(inset, height / 2, height / 2, paint)
                paint.style = Paint.Style.FILL
            }

            // Lay out right-to-left: the first part sits on the right edge.
            var x = rect.centerX() + contentWidth / 2
            val baseline = rect.centerY() - (textPaint.descent() + textPaint.ascent()) / 2
            if (dotSize > 0) {
                paint.color = RoutingColors.primary.argb()
                drawCircle(x - dotSize / 2, rect.centerY(), dotSize / 2, paint)
                x -= dotSize + gap
            }
            textPaint.color = foreground.argb()
            parts.forEachIndexed { index, part ->
                x -= widths[index]
                drawText(part, x, baseline, textPaint)
                x -= gap
            }
        }
    }
}

/** Path data shared by Compose icons and map bitmaps. */
object NavigationIconPaths {
    val car = listOf("M3 13l2-6h14l2 6v5H3z", "M3 13h18", "M7 18v2", "M17 18v2")
}
