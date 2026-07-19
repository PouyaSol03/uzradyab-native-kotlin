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
import com.example.uzradyab.ui.theme.UzradyabTheme
import com.example.uzradyab.ui.theme.themedColor

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
                    colors = listOf(UzradyabTheme.colors.primary, themedColor(light = Color(0xFF3D8AF1), dark = Color(0xFF619CEA)), UzradyabTheme.colors.purple),
                    start = Offset.Zero,
                    end = Offset.Infinite
                )
            )
    ) {
        val pinColor = themedColor(light = Color.White, dark = Color(0xFF27343F))
        val shape1Color = themedColor(light = Color.White, dark = Color(0xFF27343F))
        val shape2Color = themedColor(light = Color(0xFF24366E), dark = Color(0xFF8C9FD9))
        val shape3Color = themedColor(light = Color.White, dark = Color(0xFF27343F))
        val shape4Color = themedColor(light = Color.White, dark = Color(0xFF27343F))
        
        Canvas(modifier = Modifier.fillMaxSize()) {
            fun drawMapPin(center: Offset, scale: Float) {
                val pin = Path().apply {
                    moveTo(center.x, center.y + 25f * scale)
                    cubicTo(
                        center.x - 28f * scale,
                        center.y - 8f * scale,
                        center.x - 19f * scale,
                        center.y - 41f * scale,
                        center.x,
                        center.y - 41f * scale,
                    )
                    cubicTo(
                        center.x + 19f * scale,
                        center.y - 41f * scale,
                        center.x + 28f * scale,
                        center.y - 8f * scale,
                        center.x,
                        center.y + 25f * scale,
                    )
                    close()
                }
                drawPath(pin, color = pinColor.copy(alpha = 0.1f))
                drawCircle(
                    color = pinColor.copy(alpha = 0.1f),
                    radius = 8f * scale,
                    center = Offset(center.x, center.y - 15f * scale),
                )
            }

            drawPath(
                path = Path().apply {
                    moveTo(size.width * 0.06f, 0f)
                    lineTo(size.width * 0.38f, size.height)
                    lineTo(size.width * 0.51f, size.height)
                    lineTo(size.width * 0.22f, 0f)
                    close()
                },
                color = shape1Color.copy(alpha = 0.12f)
            )
            drawPath(
                path = Path().apply {
                    moveTo(size.width * 0.46f, 0f)
                    lineTo(size.width * 0.78f, 0f)
                    lineTo(size.width * 0.8f, size.height * 0.22f)
                    lineTo(size.width * 0.46f, size.height * 0.36f)
                    close()
                },
                color = shape2Color.copy(alpha = 0.16f)
            )
            drawPath(
                path = Path().apply {
                    moveTo(size.width * -0.08f, size.height * 0.52f)
                    cubicTo(size.width * 0.18f, size.height * 0.45f, size.width * 0.54f, size.height * 0.62f, size.width * 0.94f, size.height * 0.36f)
                    cubicTo(size.width * 1.15f, size.height * 0.23f, size.width * 1.18f, size.height * 0.12f, size.width * 0.94f, size.height * 0.08f)
                    cubicTo(size.width * 0.62f, size.height * 0.03f, size.width * 0.34f, size.height * 0.11f, size.width * 0.04f, size.height * 0.2f)
                },
                color = shape3Color.copy(alpha = 0.18f),
                style = Stroke(width = 3f, cap = StrokeCap.Round, pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(14f, 12f)))
            )
            drawPath(
                path = Path().apply {
                    moveTo(size.width * 0.16f, size.height)
                    cubicTo(size.width * 0.32f, size.height * 0.86f, size.width * 0.64f, size.height * 0.93f, size.width * 1.02f, size.height * 0.72f)
                },
                color = shape4Color.copy(alpha = 0.16f),
                style = Stroke(width = 3f, cap = StrokeCap.Round, pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(14f, 12f)))
            )
            drawMapPin(Offset(size.width * 0.65f, size.height * 0.07f), 0.75f)
            drawMapPin(Offset(size.width * 0.16f, size.height * 0.94f), 0.9f)
            drawMapPin(Offset(size.width * 0.96f, size.height * 0.56f), 0.75f)
            drawMapPin(Offset(size.width * 0.04f, size.height * 0.27f), 0.75f)
        }
        content()
    }
}
