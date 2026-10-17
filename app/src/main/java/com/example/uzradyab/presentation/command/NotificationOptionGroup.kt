package com.example.uzradyab.presentation.command

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.uzradyab.ui.theme.themedColor

private val OptionShape = RoundedCornerShape(8.dp)

/** Notification type choice buttons in a 2-column grid. */
@Composable
fun NotificationOptionGroup(
    param: NotifyParam,
    field: FieldUiState,
    onSelect: (String) -> Unit,
) {
    Column(modifier = Modifier.alpha(if (field.locked) 0.5f else 1f)) {
        FieldLabel(text = param.label, highlighted = field.highlighted)
        Spacer(modifier = Modifier.height(6.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            param.options.chunked(2).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    row.forEach { option ->
                        NotificationOptionButton(
                            option = option,
                            selected = option.value == field.value,
                            field = field,
                            onClick = { onSelect(option.value) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                    if (row.size == 1) Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun NotificationOptionButton(
    option: NotifyOption,
    selected: Boolean,
    field: FieldUiState,
    onClick: () -> Unit,
    modifier: Modifier,
) {
    val primary = commandPrimary()
    val borderColor = when {
        field.locked -> themedColor(light = Color(0xFFE5E7EB), dark = Color(0xFF455A64))
        selected -> primary
        field.highlighted -> primary.copy(alpha = 0.6f)
        else -> themedColor(light = Color(0xFFA9AEB2), dark = Color(0xFF5C6F7D))
    }
    val background = when {
        field.locked -> themedColor(light = Color(0xFFF3F4F6), dark = Color(0xFF1A1D23))
        selected -> themedColor(light = Color(0xFFE8F1FE), dark = Color(0xFF041839))
        else -> themedColor(light = Color.White, dark = Color(0xFF1A1D23))
    }
    val textColor = if (selected) primary else themedColor(light = Color(0xFF333638), dark = Color(0xFFAFB3B6))

    Box(
        modifier = modifier
            .height(44.dp)
            .clip(OptionShape)
            .background(background)
            .border(1.dp, borderColor, OptionShape)
            .clickable(enabled = !field.locked, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = option.label,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = textColor,
        )
    }
}
