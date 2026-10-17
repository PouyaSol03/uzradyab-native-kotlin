package com.example.uzradyab.presentation.command

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Sms
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.AbsoluteAlignment
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.uzradyab.R
import com.example.uzradyab.ui.theme.themedColor

private val MethodShape = RoundedCornerShape(16.dp)
private val MethodGap = 12.dp
private val MethodHeight = 112.dp

private data class SendMethod(val key: String, val label: String, val icon: ImageVector)

@Composable
fun SendMethodSheet(
    commandTitle: String,
    selectedMethod: String,
    isSending: Boolean,
    onMethodSelect: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val methods = listOf(
        SendMethod("internet", stringResource(R.string.str_a85af005), Icons.Outlined.Language),
        SendMethod("sms", stringResource(R.string.str_02b42328), Icons.Outlined.Sms),
    )

    CommandBottomSheet(onDismissRequest = onDismiss) {
        Text(
            text = "ارسال دستور \"$commandTitle\"",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = themedColor(light = Color(0xFF333638), dark = Color(0xFFAFB3B6)),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.str_de7ae3ed),
            fontSize = 14.sp,
            color = themedColor(light = Color(0xFF676C70), dark = Color(0xFF929292)),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(24.dp))

        SendMethodChoices(methods = methods, selectedMethod = selectedMethod, onMethodSelect = onMethodSelect)

        Spacer(modifier = Modifier.height(32.dp))

        SheetActionButtons(
            primaryText = stringResource(R.string.str_968581ce),
            onPrimary = onConfirm,
            onCancel = onDismiss,
            loading = isSending,
        )
    }
}

/** Two equal choice buttons; the selected highlight slides between them as one element. */
@Composable
private fun SendMethodChoices(
    methods: List<SendMethod>,
    selectedMethod: String,
    onMethodSelect: (String) -> Unit,
) {
    val primary = commandPrimary()
    val selectedIndex = methods.indexOfFirst { it.key == selectedMethod }.coerceAtLeast(0)
    val interactions = remember { methods.associate { it.key to MutableInteractionSource() } }
    val scales = methods.associate { method ->
        val pressed by interactions.getValue(method.key).collectIsPressedAsState()
        method.key to animateFloatAsState(if (pressed) 0.96f else 1f, label = "methodPress").value
    }

    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val buttonWidth = (maxWidth - MethodGap) / 2
        val highlightOffset by animateDpAsState(
            targetValue = (buttonWidth + MethodGap) * selectedIndex,
            animationSpec = highlightSpring(),
            label = "methodHighlight",
        )

        Row(horizontalArrangement = Arrangement.spacedBy(MethodGap)) {
            methods.forEach { method ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(MethodHeight)
                        .graphicsLayer {
                            scaleX = scales.getValue(method.key)
                            scaleY = scaleX
                        }
                        .border(BorderStroke(1.5.dp, themedColor(light = Color(0xFFE5E7EB), dark = Color(0xFF455A64))), MethodShape),
                )
            }
        }

        // offset() mirrors in RTL, so index 0 stays on the start side like the Row.
        Box(
            modifier = Modifier
                .offset(x = highlightOffset)
                .width(buttonWidth)
                .height(MethodHeight)
                .graphicsLayer {
                    val scale = scales.getValue(methods[selectedIndex].key)
                    scaleX = scale
                    scaleY = scale
                }
                .clip(MethodShape)
                .background(themedColor(light = Color(0xFFF1F6FE), dark = Color(0xFF041839)))
                .border(BorderStroke(1.5.dp, primary), MethodShape),
        )

        Row(horizontalArrangement = Arrangement.spacedBy(MethodGap)) {
            methods.forEach { method ->
                SendMethodContent(
                    method = method,
                    selected = method.key == selectedMethod,
                    interactionSource = interactions.getValue(method.key),
                    scale = scales.getValue(method.key),
                    onClick = { onMethodSelect(method.key) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun SendMethodContent(
    method: SendMethod,
    selected: Boolean,
    interactionSource: MutableInteractionSource,
    scale: Float,
    onClick: () -> Unit,
    modifier: Modifier,
) {
    val primary = commandPrimary()
    val circleColor by animateColorAsState(
        targetValue = if (selected) primary else themedColor(light = Color(0xFFE8F1FE), dark = Color(0xFF1E3A5F)),
        label = "methodCircle",
    )
    val iconColor by animateColorAsState(targetValue = if (selected) Color.White else primary, label = "methodIcon")
    val labelColor by animateColorAsState(
        targetValue = if (selected) primary else themedColor(light = Color(0xFF333638), dark = Color(0xFFAFB3B6)),
        label = "methodLabel",
    )

    Box(
        modifier = modifier
            .height(MethodHeight)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(MethodShape)
            .clickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                onClick = onClick,
            ),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(circleColor),
                contentAlignment = Alignment.Center,
            ) {
                Icon(imageVector = method.icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = method.label, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = labelColor)
        }

        AnimatedVisibility(
            visible = selected,
            enter = scaleIn(highlightSpring()) + fadeIn(),
            exit = scaleOut(highlightSpring()) + fadeOut(),
            modifier = Modifier
                .align(AbsoluteAlignment.TopLeft)
                .padding(8.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(primary),
                contentAlignment = Alignment.Center,
            ) {
                Icon(imageVector = Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
            }
        }
    }
}
