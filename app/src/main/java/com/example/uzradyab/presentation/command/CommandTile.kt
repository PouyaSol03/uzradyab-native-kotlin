package com.example.uzradyab.presentation.command

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BatteryAlert
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.CellTower
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.Dns
import androidx.compose.material.icons.outlined.Emergency
import androidx.compose.material.icons.outlined.LocalGasStation
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.MonitorHeart
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Power
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.uzradyab.ui.theme.themedColor

@Composable
internal fun commandPrimary(): Color = themedColor(light = Color(0xFF307EF3), dark = Color(0xFF5F98EC))

internal fun CommandIcon.vector(): ImageVector = when (this) {
    CommandIcon.Status -> Icons.Outlined.MonitorHeart
    CommandIcon.Where -> Icons.Outlined.LocationOn
    CommandIcon.Admin -> Icons.Outlined.Person
    CommandIcon.FuelRelay -> Icons.Outlined.LocalGasStation
    CommandIcon.Restart -> Icons.Outlined.RestartAlt
    CommandIcon.Server -> Icons.Outlined.Dns
    CommandIcon.Time -> Icons.Outlined.Schedule
    CommandIcon.Lbs -> Icons.Outlined.CellTower
    CommandIcon.Speed -> Icons.Outlined.Speed
    CommandIcon.ParkedMove -> Icons.Outlined.DirectionsCar
    CommandIcon.PowerCut -> Icons.Outlined.Power
    CommandIcon.Battery -> Icons.Outlined.BatteryAlert
    CommandIcon.SosNumbers -> Icons.Outlined.Emergency
    CommandIcon.Call -> Icons.Outlined.Call
}

/** Icon circle in the command's tone; filled primary with a white icon when [selected]. */
@Composable
internal fun CommandIconCircle(command: CommandData, size: Dp, selected: Boolean = false) {
    val (background, tint) = when {
        selected -> commandPrimary() to Color.White
        command.tone == CommandTone.Disable ->
            themedColor(light = Color(0xFFF1F2F4), dark = Color(0xFF1A1D23)) to themedColor(light = Color(0xFF676C70), dark = Color(0xFF929292))
        command.tone == CommandTone.Danger ->
            themedColor(light = Color(0xFFFDECEC), dark = Color(0xFF3A1F1F)) to themedColor(light = Color(0xFFD93025), dark = Color(0xFFF28B82))
        else ->
            themedColor(light = Color(0xFFE8F1FE), dark = Color(0xFF1E3A5F)) to commandPrimary()
    }
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(background),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = command.icon.vector(),
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(size / 2),
        )
    }
}

@Composable
fun CommandTile(
    command: CommandData,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(12.dp)
    val background = if (selected) themedColor(light = Color(0xFFF1F6FE), dark = Color(0xFF041839)) else themedColor(light = Color.White, dark = Color(0xFF27343F))
    val border = if (selected) commandPrimary() else themedColor(light = Color(0xFFE5E7EB), dark = Color(0xFF1B1D23))
    val titleColor = if (selected) commandPrimary() else themedColor(light = Color(0xFF333638), dark = Color(0xFFAFB3B6))

    Column(
        modifier = modifier
            .heightIn(min = 104.dp)
            .clip(shape)
            .background(background)
            .border(1.dp, border, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CommandIconCircle(command = command, size = 40.dp, selected = selected)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = command.title,
            color = titleColor,
            fontSize = 12.sp,
            lineHeight = 18.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            maxLines = 3,
        )
    }
}

/** Three-column grid; tiles in a row share the tallest tile's height. */
@Composable
fun CommandGrid(
    commands: List<CommandData>,
    selectedType: String,
    onSelect: (CommandData) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        commands.chunked(3).forEach { row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                row.forEach { command ->
                    CommandTile(
                        command = command,
                        selected = command.type == selectedType,
                        onClick = { onSelect(command) },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                    )
                }
                repeat(3 - row.size) { Spacer(modifier = Modifier.weight(1f)) }
            }
        }
    }
}
