package com.example.uzradyab.core.designsystem

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.uzradyab.ui.theme.AppBlue
import com.example.uzradyab.ui.theme.AppPurple

@Composable
fun AuthBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(AppBlue, Color(0xFF3D8AF1), AppPurple),
                    start = Offset.Zero,
                    end = Offset.Infinite
                )
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawPath(
                path = Path().apply {
                    moveTo(size.width * 0.06f, 0f)
                    lineTo(size.width * 0.38f, size.height)
                    lineTo(size.width * 0.51f, size.height)
                    lineTo(size.width * 0.22f, 0f)
                    close()
                },
                color = Color.White.copy(alpha = 0.12f)
            )
            drawPath(
                path = Path().apply {
                    moveTo(size.width * 0.46f, 0f)
                    lineTo(size.width * 0.78f, 0f)
                    lineTo(size.width * 0.8f, size.height * 0.22f)
                    lineTo(size.width * 0.46f, size.height * 0.36f)
                    close()
                },
                color = Color(0xFF24366E).copy(alpha = 0.16f)
            )
            drawPath(
                path = Path().apply {
                    moveTo(size.width * -0.08f, size.height * 0.52f)
                    cubicTo(size.width * 0.18f, size.height * 0.45f, size.width * 0.54f, size.height * 0.62f, size.width * 0.94f, size.height * 0.36f)
                    cubicTo(size.width * 1.15f, size.height * 0.23f, size.width * 1.18f, size.height * 0.12f, size.width * 0.94f, size.height * 0.08f)
                    cubicTo(size.width * 0.62f, size.height * 0.03f, size.width * 0.34f, size.height * 0.11f, size.width * 0.04f, size.height * 0.2f)
                },
                color = Color.White.copy(alpha = 0.18f),
                style = Stroke(width = 3f, cap = StrokeCap.Round, pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(14f, 12f)))
            )
            drawPath(
                path = Path().apply {
                    moveTo(size.width * 0.16f, size.height)
                    cubicTo(size.width * 0.32f, size.height * 0.86f, size.width * 0.64f, size.height * 0.93f, size.width * 1.02f, size.height * 0.72f)
                },
                color = Color.White.copy(alpha = 0.16f),
                style = Stroke(width = 3f, cap = StrokeCap.Round, pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(14f, 12f)))
            )
        }
        content()
    }
}
