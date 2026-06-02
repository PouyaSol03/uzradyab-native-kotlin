package com.example.uzradyab.presentation.auth

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.uzradyab.core.designsystem.EyeOffIcon
import com.example.uzradyab.core.designsystem.KeyIcon
import com.example.uzradyab.core.designsystem.PhoneIcon
import com.example.uzradyab.ui.theme.AppInputBorder
import com.example.uzradyab.ui.theme.AppTextBody
import com.example.uzradyab.ui.theme.AppTextMuted
import com.example.uzradyab.ui.theme.AppTextPrimary

internal val AuthPanelWidth = 327.dp
internal val AuthControlWidth = 279.dp

@Composable
internal fun AuthPanel(
    height: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val primaryColor = MaterialTheme.colorScheme.primary

    Box(
        modifier = modifier
            .fillMaxWidth()
            .widthIn(max = AuthPanelWidth)
            .height(height)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White),
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 18.dp.toPx()
            val path = Path().apply {
                moveTo(0f, 51.dp.toPx())
                cubicTo(18.dp.toPx(), 51.dp.toPx(), 15.dp.toPx(), 25.dp.toPx(), 31.dp.toPx(), 25.dp.toPx())
                cubicTo(48.dp.toPx(), 25.dp.toPx(), 43.dp.toPx(), 51.dp.toPx(), 60.dp.toPx(), 51.dp.toPx())
                cubicTo(78.dp.toPx(), 51.dp.toPx(), 72.dp.toPx(), 25.dp.toPx(), 88.dp.toPx(), 25.dp.toPx())
                cubicTo(106.dp.toPx(), 25.dp.toPx(), 100.dp.toPx(), 51.dp.toPx(), 116.dp.toPx(), 51.dp.toPx())
                lineTo(size.width - 25.dp.toPx(), 51.dp.toPx())
                quadraticBezierTo(size.width - 13.dp.toPx(), 51.dp.toPx(), size.width - 13.dp.toPx(), 39.dp.toPx())
                lineTo(size.width - 13.dp.toPx(), 22.dp.toPx())
            }
            drawPath(
                path = path,
                color = primaryColor,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round),
            )
            drawPath(
                path = path,
                color = Color.White,
                style = Stroke(width = 9.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
            )
        }
        content()
    }
}

@Composable
internal fun AuthTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        color = AppTextPrimary,
        fontSize = 20.sp,
        lineHeight = 35.sp,
        fontWeight = FontWeight.ExtraBold,
        textAlign = TextAlign.Center,
        modifier = modifier.fillMaxWidth(),
    )
}

@Composable
internal fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    inputTextDirection: TextDirection = TextDirection.ContentOrLtr,
    leftIcon: (@Composable () -> Unit)? = null,
    rightIcon: (@Composable () -> Unit)? = null,
) {
    var isFocused by remember { mutableStateOf(false) }
    val borderColor = if (isFocused) MaterialTheme.colorScheme.primary else AppInputBorder

    Column(
        modifier = modifier.width(AuthControlWidth),
        horizontalAlignment = Alignment.End,
    ) {
        Text(
            text = label,
            color = AppTextBody,
            fontSize = 14.sp,
            lineHeight = 22.sp,
            textAlign = TextAlign.Right,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(8.dp))
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .width(AuthControlWidth)
                    .height(48.dp)
                    .onFocusChanged { isFocused = it.isFocused },
                singleLine = true,
                keyboardOptions = keyboardOptions,
                visualTransformation = visualTransformation,
                textStyle = TextStyle(
                    color = AppTextBody,
                    fontSize = 14.sp,
                    lineHeight = 22.sp,
                    textDirection = inputTextDirection,
                    textAlign = TextAlign.Right,
                ),
                decorationBox = { innerTextField ->
                    Row(
                        modifier = Modifier
                            .width(AuthControlWidth)
                            .height(48.dp)
                            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier.size(24.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            leftIcon?.invoke()
                        }
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.CenterEnd,
                        ) {
                            if (value.isEmpty()) {
                                Text(
                                    text = placeholder,
                                    color = AppTextMuted,
                                    fontSize = 14.sp,
                                    lineHeight = 22.sp,
                                    textAlign = TextAlign.Right,
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            }
                            innerTextField()
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier.size(24.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            rightIcon?.invoke()
                        }
                    }
                },
            )
        }
    }
}

@Composable
internal fun AuthPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .width(AuthControlWidth)
            .height(48.dp),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White,
            disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.45f),
            disabledContentColor = Color.White.copy(alpha = 0.8f),
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
    ) {
        Text(
            text = text,
            fontSize = 16.sp,
            lineHeight = 28.sp,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
internal fun AuthTextLink(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    fontSize: Int = 14,
) {
    TextButton(
        onClick = onClick,
        modifier = modifier.height(40.dp),
        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary),
    ) {
        Text(
            text = text,
            fontSize = fontSize.sp,
            lineHeight = 24.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
internal fun AuthLanguageField(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.width(AuthControlWidth),
        horizontalAlignment = Alignment.End,
    ) {
        Text(
            text = "زبان برنامه",
            color = AppTextBody,
            fontSize = 14.sp,
            lineHeight = 24.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Right,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(2.dp))
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            Row(
                modifier = Modifier
                    .width(AuthControlWidth)
                    .height(48.dp)
                    .border(1.dp, AppInputBorder, RoundedCornerShape(8.dp))
                    .padding(horizontal = 16.dp)
                    .clickable(enabled = false) {},
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "فارسی",
                    color = AppTextBody,
                    fontSize = 14.sp,
                    lineHeight = 24.sp,
                    fontWeight = FontWeight.Medium,
                )
                Spacer(modifier = Modifier.width(8.dp))
                IranFlagIcon()
            }
        }
    }
}

@Composable
internal fun PhoneFieldIcon() {
    PhoneIcon(color = Color(0xFFC0CDD8), modifier = Modifier.size(24.dp))
}

@Composable
internal fun PasswordKeyIcon() {
    KeyIcon(color = Color(0xFFC0CDD8), modifier = Modifier.size(24.dp))
}

@Composable
internal fun PasswordEyeIcon() {
    EyeOffIcon(color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
}

@Composable
internal fun UserFieldIcon() {
    Canvas(modifier = Modifier.size(24.dp)) {
        val stroke = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        drawCircle(
            color = Color(0xFFC0CDD8),
            radius = 5.75.dp.toPx(),
            center = Offset(size.width / 2f, 7.dp.toPx()),
            style = stroke,
        )
        drawArc(
            color = Color(0xFFC0CDD8),
            startAngle = 205f,
            sweepAngle = 130f,
            useCenter = false,
            topLeft = Offset(3.dp.toPx(), 12.dp.toPx()),
            size = androidx.compose.ui.geometry.Size(18.dp.toPx(), 10.dp.toPx()),
            style = stroke,
        )
    }
}

@Composable
private fun IranFlagIcon() {
    Canvas(modifier = Modifier.size(width = 28.dp, height = 18.dp)) {
        drawRoundRect(
            color = Color.White,
            cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx()),
        )
        drawRect(
            color = Color(0xFF239F40),
            size = androidx.compose.ui.geometry.Size(size.width, size.height / 3f),
        )
        drawRect(
            color = Color(0xFFDA0000),
            topLeft = Offset(0f, size.height * 2f / 3f),
            size = androidx.compose.ui.geometry.Size(size.width, size.height / 3f),
        )
        drawRoundRect(
            color = AppTextBody,
            cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx()),
            style = Stroke(width = 1.dp.toPx()),
        )
    }
}
