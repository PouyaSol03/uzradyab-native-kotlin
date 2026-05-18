package com.example.uzradyab.core.designsystem

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun PhoneIcon(
    modifier: Modifier = Modifier,
    color: Color,
) {
    Canvas(modifier.size(28.dp)) {
        val stroke = Stroke(width = 2.4.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        drawRoundRect(
            color = color,
            topLeft = Offset(size.width * 0.28f, size.height * 0.08f),
            size = Size(size.width * 0.44f, size.height * 0.84f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(5.dp.toPx()),
            style = stroke
        )
        drawCircle(
            color = color,
            radius = 1.6.dp.toPx(),
            center = Offset(size.width * 0.5f, size.height * 0.78f)
        )
    }
}

@Composable
fun KeyIcon(
    modifier: Modifier = Modifier,
    color: Color,
) {
    Canvas(modifier.size(30.dp)) {
        val stroke = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        drawCircle(
            color = color,
            radius = size.minDimension * 0.2f,
            center = Offset(size.width * 0.35f, size.height * 0.62f),
            style = stroke
        )
        drawLine(
            color = color,
            start = Offset(size.width * 0.5f, size.height * 0.48f),
            end = Offset(size.width * 0.78f, size.height * 0.2f),
            strokeWidth = stroke.width,
            cap = StrokeCap.Round
        )
        drawLine(
            color = color,
            start = Offset(size.width * 0.68f, size.height * 0.3f),
            end = Offset(size.width * 0.78f, size.height * 0.4f),
            strokeWidth = stroke.width,
            cap = StrokeCap.Round
        )
    }
}

@Composable
fun EyeOffIcon(
    modifier: Modifier = Modifier,
    color: Color,
) {
    Canvas(modifier.size(30.dp)) {
        val stroke = Stroke(width = 2.4.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        val eye = Path().apply {
            moveTo(size.width * 0.12f, size.height * 0.52f)
            cubicTo(size.width * 0.28f, size.height * 0.22f, size.width * 0.72f, size.height * 0.22f, size.width * 0.88f, size.height * 0.52f)
            cubicTo(size.width * 0.72f, size.height * 0.82f, size.width * 0.28f, size.height * 0.82f, size.width * 0.12f, size.height * 0.52f)
        }
        drawPath(eye, color = color, style = stroke)
        drawCircle(color = color, radius = size.minDimension * 0.09f, center = Offset(size.width * 0.5f, size.height * 0.52f), style = stroke)
        drawLine(
            color = color,
            start = Offset(size.width * 0.18f, size.height * 0.86f),
            end = Offset(size.width * 0.84f, size.height * 0.16f),
            strokeWidth = stroke.width,
            cap = StrokeCap.Round
        )
    }
}
