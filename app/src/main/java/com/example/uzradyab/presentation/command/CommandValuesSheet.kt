package com.example.uzradyab.presentation.command

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.uzradyab.R
import com.example.uzradyab.core.utils.FormatUtils.toPersianDigits
import com.example.uzradyab.ui.theme.themedColor

private val FieldShape = RoundedCornerShape(8.dp)

@Composable
fun CommandValuesSheet(
    command: CommandData,
    fields: List<FieldUiState>,
    canContinue: Boolean,
    onValueChange: (key: String, value: String) -> Unit,
    onContinue: () -> Unit,
    onDismiss: () -> Unit,
) {
    CommandBottomSheet(onDismissRequest = onDismiss) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            CommandIconCircle(command = command, size = 48.dp)
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = command.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = themedColor(light = Color(0xFF333638), dark = Color(0xFFAFB3B6)),
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            groupFieldRows(fields).forEach { row ->
                if (row.size == 2) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        row.forEach { field ->
                            NumberField(field, field.param as NumberParam, onValueChange, Modifier.weight(1f))
                        }
                    }
                } else {
                    val field = row.single()
                    when (val param = field.param) {
                        is NumberParam -> NumberField(field, param, onValueChange, Modifier.fillMaxWidth())
                        is NotifyParam -> NotificationOptionGroup(
                            param = param,
                            field = field,
                            onSelect = { onValueChange(param.key, it) },
                        )
                        is PhoneParam -> PhoneRow(field, param, onValueChange)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        SheetActionButtons(
            primaryText = stringResource(R.string.command_continue),
            onPrimary = onContinue,
            onCancel = onDismiss,
            primaryEnabled = canContinue,
        )
    }
}

/** Two consecutive number fields share a row; everything else gets its own row. */
private fun groupFieldRows(fields: List<FieldUiState>): List<List<FieldUiState>> {
    val rows = mutableListOf<List<FieldUiState>>()
    var i = 0
    while (i < fields.size) {
        val next = fields.getOrNull(i + 1)
        if (fields[i].param is NumberParam && next?.param is NumberParam) {
            rows += listOf(fields[i], next)
            i += 2
        } else {
            rows += listOf(fields[i])
            i += 1
        }
    }
    return rows
}

@Composable
internal fun FieldLabel(text: String, highlighted: Boolean) {
    Text(
        text = text,
        fontSize = 12.sp,
        fontWeight = if (highlighted) FontWeight.SemiBold else FontWeight.Normal,
        color = if (highlighted) commandPrimary() else themedColor(light = Color(0xFF676C70), dark = Color(0xFF929292)),
    )
}

@Composable
private fun FieldError(error: String?) {
    if (error != null) {
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = error, fontSize = 12.sp, color = Color(0xFFE53935))
    }
}

@Composable
private fun NumberField(
    field: FieldUiState,
    param: NumberParam,
    onValueChange: (String, String) -> Unit,
    modifier: Modifier,
) {
    Column(modifier = modifier.alpha(if (field.locked) 0.5f else 1f)) {
        FieldLabel(text = param.label, highlighted = field.highlighted)
        Spacer(modifier = Modifier.height(6.dp))
        CommandInputBox(
            field = field,
            placeholder = param.placeholder,
            unit = param.unit,
            keyboardType = KeyboardType.Number,
            onValueChange = { onValueChange(param.key, it) },
        )
        FieldError(field.error)
    }
}

@Composable
private fun PhoneRow(
    field: FieldUiState,
    param: PhoneParam,
    onValueChange: (String, String) -> Unit,
) {
    val primary = commandPrimary()
    val filled = field.value.isNotBlank()
    Column(modifier = Modifier.alpha(if (field.locked) 0.5f else 1f)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(if (filled) primary else themedColor(light = Color(0xFFE8F1FE), dark = Color(0xFF1E3A5F))),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = param.index.toString().toPersianDigits(),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (filled) Color.White else primary,
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                CommandInputBox(
                    field = field,
                    placeholder = "09xxxxxxxxx",
                    unit = null,
                    keyboardType = KeyboardType.Phone,
                    onValueChange = { onValueChange(param.key, it) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
        if (field.error != null) {
            Row {
                Spacer(modifier = Modifier.width(40.dp))
                Column { FieldError(field.error) }
            }
        }
    }
}

/** 48dp input with locked / highlighted / error borders and the unit text at the end. */
@Composable
private fun CommandInputBox(
    field: FieldUiState,
    placeholder: String,
    unit: String?,
    keyboardType: KeyboardType,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier.fillMaxWidth(),
) {
    val primary = commandPrimary()
    val glow = primary.copy(alpha = 0.15f)
    val borderColor = when {
        field.locked -> themedColor(light = Color(0xFFE5E7EB), dark = Color(0xFF455A64))
        field.error != null -> Color(0xFFE53935)
        field.highlighted -> primary
        else -> themedColor(light = Color(0xFFA9AEB2), dark = Color(0xFF5C6F7D))
    }
    val background = when {
        field.locked -> themedColor(light = Color(0xFFF3F4F6), dark = Color(0xFF1A1D23))
        else -> themedColor(light = Color.White, dark = Color(0xFF1A1D23))
    }
    val textColor = themedColor(light = Color(0xFF333638), dark = Color(0xFFAFB3B6))
    val greyText = themedColor(light = Color(0xFF676C70), dark = Color(0xFF929292))

    BasicTextField(
        value = field.value,
        onValueChange = onValueChange,
        enabled = !field.locked,
        singleLine = true,
        textStyle = TextStyle(fontSize = 14.sp, color = textColor),
        cursorBrush = SolidColor(primary),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = modifier
            .height(48.dp)
            .drawBehind {
                if (field.highlighted && field.error == null) {
                    val spread = 3.dp.toPx()
                    drawRoundRect(
                        color = glow,
                        topLeft = Offset(-spread, -spread),
                        size = Size(size.width + spread * 2, size.height + spread * 2),
                        cornerRadius = CornerRadius(8.dp.toPx() + spread),
                    )
                }
            }
            .clip(FieldShape)
            .background(background)
            .border(1.dp, borderColor, FieldShape),
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                    if (field.value.isEmpty()) {
                        Text(text = placeholder, fontSize = 13.sp, color = greyText)
                    }
                    innerTextField()
                }
                if (unit != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = unit, fontSize = 11.sp, color = greyText)
                }
            }
        },
    )
}
