package com.example.uzradyab.presentation.command

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkHorizontally
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
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.uzradyab.ui.theme.themedColor

private val OptionShape = RoundedCornerShape(8.dp)
private val OptionHeight = 44.dp
private val OptionGap = 8.dp

/** Stiffness 500 / damping 35 (mass 1) expressed as a damping ratio. */
internal fun <T> highlightSpring(): SpringSpec<T> = spring(dampingRatio = 0.78f, stiffness = 500f)

/**
 * Notification type choice buttons in a 2-column grid.
 *
 * Drawn in three layers so the selected highlight can slide between options as one element:
 * option backgrounds, the shared highlight, then labels and touch targets.
 */
@Composable
fun NotificationOptionGroup(
    param: NotifyParam,
    field: FieldUiState,
    onSelect: (String) -> Unit,
) {
    val primary = commandPrimary()
    val selectedValue = field.value.takeIf { value -> param.options.any { it.value == value } }
    val interactions = remember(param) { param.options.associate { it.value to MutableInteractionSource() } }
    val scales = param.options.associate { option ->
        val pressed by interactions.getValue(option.value).collectIsPressedAsState()
        option.value to animateFloatAsState(if (pressed) 0.95f else 1f, label = "optionPress").value
    }

    val selectedIndex = param.options.indexOfFirst { it.value == selectedValue }

    val baseBorder = when {
        field.locked -> themedColor(light = Color(0xFFE5E7EB), dark = Color(0xFF455A64))
        field.highlighted -> primary.copy(alpha = 0.6f)
        else -> themedColor(light = Color(0xFFA9AEB2), dark = Color(0xFF5C6F7D))
    }
    val baseBackground = when {
        field.locked -> themedColor(light = Color(0xFFF3F4F6), dark = Color(0xFF1A1D23))
        else -> themedColor(light = Color.White, dark = Color(0xFF1A1D23))
    }

    Column(modifier = Modifier.alpha(if (field.locked) 0.5f else 1f)) {
        FieldLabel(text = param.label, highlighted = field.highlighted)
        Spacer(modifier = Modifier.height(6.dp))
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val cellWidth = (maxWidth - OptionGap) / 2
            // Grid position of the selected option. offset() mirrors x in RTL, matching the Rows.
            val target: DpOffset? = if (selectedIndex < 0) null else DpOffset(
                x = (cellWidth + OptionGap) * (selectedIndex % 2),
                y = (OptionHeight + OptionGap) * (selectedIndex / 2),
            )
            val highlight = remember { Animatable(DpOffset.Zero, DpOffset.VectorConverter) }
            var highlightPlaced by remember { mutableStateOf(false) }
            LaunchedEffect(target) {
                when {
                    target == null -> highlightPlaced = false
                    !highlightPlaced -> {
                        highlight.snapTo(target)
                        highlightPlaced = true
                    }
                    else -> highlight.animateTo(target, highlightSpring())
                }
            }

            OptionRows(param.options) { option ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(OptionHeight)
                        .graphicsLayer {
                            scaleX = scales.getValue(option.value)
                            scaleY = scaleX
                        }
                        .clip(OptionShape)
                        .background(baseBackground)
                        .border(1.dp, baseBorder, OptionShape),
                )
            }

            if (target != null && highlightPlaced && selectedValue != null) {
                val scale = scales.getValue(selectedValue)
                Box(
                    modifier = Modifier
                        .offset(x = highlight.value.x, y = highlight.value.y)
                        .size(width = cellWidth, height = OptionHeight)
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                        }
                        .clip(OptionShape)
                        .background(themedColor(light = Color(0xFFE8F1FE), dark = Color(0xFF041839)))
                        .border(1.dp, primary, OptionShape),
                )
            }

            OptionRows(param.options) { option ->
                NotificationOptionContent(
                    option = option,
                    selected = option.value == selectedValue,
                    enabled = !field.locked,
                    interactionSource = interactions.getValue(option.value),
                    scale = scales.getValue(option.value),
                    onClick = { onSelect(option.value) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun OptionRows(
    options: List<NotifyOption>,
    item: @Composable RowScope.(NotifyOption) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(OptionGap)) {
        options.chunked(2).forEach { row ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(OptionGap)) {
                row.forEach { option -> item(option) }
                if (row.size == 1) Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun NotificationOptionContent(
    option: NotifyOption,
    selected: Boolean,
    enabled: Boolean,
    interactionSource: MutableInteractionSource,
    scale: Float,
    onClick: () -> Unit,
    modifier: Modifier,
) {
    val primary = commandPrimary()
    val textColor by animateColorAsState(
        targetValue = if (selected) primary else themedColor(light = Color(0xFF333638), dark = Color(0xFFAFB3B6)),
        label = "optionText",
    )

    Row(
        modifier = modifier
            .height(OptionHeight)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(OptionShape)
            .clickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                enabled = enabled,
                onClick = onClick,
            ),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = option.label,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = textColor,
        )
        AnimatedVisibility(
            visible = selected,
            enter = expandHorizontally(highlightSpring()) + scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = 500f)),
            exit = shrinkHorizontally(highlightSpring()) + scaleOut(highlightSpring()),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    tint = primary,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}
