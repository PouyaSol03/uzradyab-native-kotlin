package com.example.uzradyab.presentation.auth

import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.graphics.Color
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.uzradyab.core.designsystem.EyeIcon
import com.example.uzradyab.core.designsystem.EyeOffIcon
import com.example.uzradyab.core.designsystem.KeyIcon
import com.example.uzradyab.core.designsystem.PhoneIcon

import com.example.uzradyab.R
import androidx.compose.ui.res.stringResource
import com.example.uzradyab.ui.theme.UzradyabTheme
import com.example.uzradyab.ui.theme.themedColor

internal val AuthPanelWidth = 327.dp
internal val AuthControlWidth = 279.dp

@Composable
internal fun AuthPanel(
    height: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val pathStr = "M19.9606 5.49218C10.527 5.5413 2.71552 13.4528 2.513 23.163L0.000897793 143.605C-0.0553706 146.303 2.02327 148.478 4.64369 148.465C7.26414 148.451 9.43402 146.253 9.49026 143.556L12.0024 23.1136C12.0924 18.7979 15.5642 15.2817 19.7568 15.2599L52.5186 15.0893C56.7113 15.0675 60.0371 18.5483 59.9471 22.864L58.9498 70.6789C58.7461 80.4468 66.4002 88.22 75.7772 88.1712C85.1542 88.1224 93.1342 80.2679 93.3379 70.4998L93.8 48.3457C93.8901 44.03 97.3619 40.5138 101.555 40.492L103.372 40.4825C107.565 40.4607 110.89 43.9415 110.8 48.2571L110.308 71.8506C110.122 80.7659 116.993 87.9566 125.654 87.9115C134.316 87.8664 141.488 80.6026 141.674 71.6873L142.203 46.2861C142.273 42.9689 144.941 40.2661 148.164 40.2493C151.387 40.2325 153.943 42.9081 153.874 46.2254L153.374 70.1873C153.172 79.8975 160.655 87.7293 170.088 87.6802L172.224 87.6691C181.657 87.62 189.469 79.7085 189.671 69.9983L190.176 45.7908C190.243 42.6092 192.802 40.0169 195.893 40.0008C198.984 39.9848 201.436 42.5509 201.37 45.7325L200.865 69.94C200.662 79.6502 208.145 87.4821 217.579 87.433L367.914 86.6503C377.347 86.6012 385.159 78.6897 385.361 68.9795L385.823 46.8254C386.026 37.1152 378.543 29.2833 369.109 29.3324L256.003 29.9213C251.81 29.9431 248.485 26.4623 248.575 22.1466L248.655 18.2747C248.745 13.9591 252.217 10.4428 256.41 10.421L381.852 9.76794C384.472 9.7543 386.642 7.55666 386.699 4.85939C386.755 2.16211 384.676 -0.0133985 382.056 0.000250032L256.614 0.653304C247.18 0.702414 239.369 8.61392 239.166 18.3241L239.085 22.196C238.883 31.9062 246.366 39.7381 255.799 39.689L368.905 39.1001C373.098 39.0783 376.424 42.5591 376.334 46.8748L375.872 69.029C375.782 73.3446 372.31 76.8608 368.117 76.8826L217.783 77.6653C213.59 77.6871 210.264 74.2063 210.354 69.8907L210.859 45.6831C211.038 37.1069 204.429 30.1898 196.097 30.2332C187.765 30.2765 180.866 37.2641 180.687 45.8402L180.182 70.0477C180.092 74.3634 176.62 77.8796 172.428 77.9014L170.292 77.9125C166.099 77.9343 162.774 74.4535 162.864 70.1379L163.363 46.176C163.545 37.4642 156.831 30.4376 148.368 30.4816C139.904 30.5257 132.896 37.6237 132.714 46.3355L132.184 71.7367C132.111 75.2575 129.278 78.126 125.858 78.1438C122.438 78.1616 119.724 75.3219 119.798 71.8012L120.29 48.2077C120.492 38.4975 113.009 30.6657 103.576 30.7148L101.758 30.7243C92.3247 30.7734 84.5132 38.6849 84.3107 48.395L83.8486 70.5492C83.7598 74.8071 80.2301 78.3814 75.9809 78.4035C71.7317 78.4256 68.3504 74.8873 68.4392 70.6295L69.4365 22.8146C69.639 13.1044 62.1559 5.27251 52.7223 5.32163L19.9606 5.49218Z"

    val holePath = remember {
        PathParser().parsePathString(pathStr).toPath()
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .widthIn(max = AuthPanelWidth)
            .height(height)
    ) {
        val maskColor = themedColor(light = Color.Black, dark = Color(0xFFE0E0E0))
        val bgColor = themedColor(light = Color.White, dark = Color(0xFF27343F))
        androidx.compose.foundation.Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = 0.99f }
        ) {
            // Solid white background
            drawRoundRect(
                color = bgColor,
                size = size,
                cornerRadius = CornerRadius(8.dp.toPx())
            )
            
            // SVG punch-out mask
            val scale = size.width / 336f
            val matrix = android.graphics.Matrix()
            matrix.setScale(scale, scale)
            val androidPath = holePath.asAndroidPath()
            val scaledAndroidPath = android.graphics.Path()
            androidPath.transform(matrix, scaledAndroidPath)
            
            translate(left = -198f, top = -44f) {
                drawPath(
                    path = scaledAndroidPath.asComposePath(),
                    color = maskColor,
                    blendMode = BlendMode.Clear
                )
            }
        }
        content()
    }
}

@Composable
internal fun AuthTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        color = UzradyabTheme.colors.textPrimary,
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
    val borderColor = if (isFocused) MaterialTheme.colorScheme.primary else UzradyabTheme.colors.inputBorder

    Column(
        modifier = modifier.width(AuthControlWidth),
        horizontalAlignment = Alignment.End,
    ) {
        Text(
            text = label,
            color = UzradyabTheme.colors.textBody,
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
                    color = UzradyabTheme.colors.textBody,
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
                                    color = UzradyabTheme.colors.textMuted,
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
            contentColor = themedColor(light = Color.White, dark = Color.White),
            disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.45f),
            disabledContentColor = themedColor(light = Color.White, dark = Color(0xFF27343F)).copy(alpha = 0.8f),
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
            text = stringResource(R.string.str_a0a519bd),
            color = UzradyabTheme.colors.textBody,
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
                    .border(1.dp, UzradyabTheme.colors.inputBorder, RoundedCornerShape(8.dp))
                    .padding(horizontal = 16.dp)
                    .clickable(enabled = false) {},
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = stringResource(R.string.str_66030b73),
                    color = UzradyabTheme.colors.textBody,
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
    PhoneIcon(color = themedColor(light = Color(0xFFC0CDD8), dark = Color(0xFF31414F)), modifier = Modifier.size(24.dp))
}

@Composable
internal fun PasswordKeyIcon() {
    KeyIcon(color = themedColor(light = Color(0xFFC0CDD8), dark = Color(0xFF31414F)), modifier = Modifier.size(24.dp))
}

@Composable
internal fun PasswordEyeIcon(isVisible: Boolean, onClick: () -> Unit) {
    androidx.compose.material3.IconButton(onClick = onClick) {
        if (isVisible) {
            EyeIcon(color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
        } else {
            EyeOffIcon(color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
        }
    }
}

@Composable
internal fun UserFieldIcon() {
    Icon(
        imageVector = Icons.Default.Person,
        contentDescription = "User",
        tint = themedColor(light = Color(0xFFC0CDD8), dark = Color(0xFF31414F)),
        modifier = Modifier.size(24.dp)
    )
}

@Composable
private fun IranFlagIcon() {
    val white = themedColor(light = Color.White, dark = Color(0xFF27343F))
    val green = themedColor(light = Color(0xFF239F40), dark = Color(0xFF82E398))
    val red = themedColor(light = Color(0xFFDA0000), dark = Color(0xFFF43232))
    
    val textColor = UzradyabTheme.colors.textBody
    Canvas(modifier = Modifier.size(width = 28.dp, height = 18.dp)) {
        drawRoundRect(
            color = white,
            cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx()),
        )
        drawRect(
            color = green,
            size = androidx.compose.ui.geometry.Size(size.width, size.height / 3f),
        )
        drawRect(
            color = red,
            topLeft = Offset(0f, size.height * 2f / 3f),
            size = androidx.compose.ui.geometry.Size(size.width, size.height / 3f),
        )
        drawRoundRect(
            color = textColor,
            cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx()),
            style = Stroke(width = 1.dp.toPx()),
        )
    }
}

@Composable
internal fun PasswordRules(rules: PasswordRuleState) {
    Column(
        modifier = Modifier.width(AuthControlWidth),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.Start,
    ) {
        PasswordRule(text = stringResource(R.string.str_62ad2446), isMet = rules.hasMinimumLength)
        PasswordRule(text = stringResource(R.string.str_7037fc5a), isMet = rules.hasDigit)
        PasswordRule(text = stringResource(R.string.str_7be2abc0), isMet = rules.hasSpecialCharacter)
    }
}

@Composable
internal fun PasswordRule(text: String, isMet: Boolean) {
    val color = if (isMet) MaterialTheme.colorScheme.primary else UzradyabTheme.colors.textBody

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Canvas(modifier = Modifier.size(10.dp)) {
            drawCircle(
                color = color,
                radius = 4.dp.toPx(),
                style = if (isMet) androidx.compose.ui.graphics.drawscope.Fill else Stroke(width = 1.dp.toPx()),
            )
        }
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            color = color,
            fontSize = 10.sp,
            lineHeight = 18.sp,
            textAlign = TextAlign.Right,
        )
    }
}

internal fun formatOtpRemaining(seconds: Int): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return "$minutes:${remainingSeconds.toString().padStart(2, '0')}"
}


@Composable
internal fun OtpInputField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    otpCount: Int = 6,
) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        BasicTextField(
            value = value,
            onValueChange = {
                if (it.length <= otpCount && it.all { char -> char.isDigit() }) {
                    onValueChange(it)
                }
            },
            modifier = modifier.width(AuthControlWidth),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            decorationBox = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    repeat(otpCount) { index ->
                        val char = when {
                            index >= value.length -> ""
                            else -> value[index].toString()
                        }
                        val isFocused = value.length == index || (index == otpCount - 1 && value.length == otpCount)
                        val borderColor = if (isFocused) MaterialTheme.colorScheme.primary else UzradyabTheme.colors.inputBorder

                        Box(
                            modifier = Modifier
                                .width(40.dp)
                                .height(48.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.Transparent)
                                .border(1.dp, borderColor, RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = char,
                                color = UzradyabTheme.colors.textBody,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        )
    }
}



