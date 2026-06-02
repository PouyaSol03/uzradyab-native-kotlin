package com.example.uzradyab.presentation.map

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.uzradyab.ui.theme.AppBlue

enum class HomeBottomItem {
    Events,
    Management,
    Map,
    Account,
}

@Composable
fun HomeBottomMenu(
    selectedItem: HomeBottomItem,
    onEventsClick: () -> Unit,
    onManagementClick: () -> Unit,
    onMapClick: () -> Unit,
    onAccountClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Row(
            modifier = modifier
                .width(324.dp)
                .height(57.dp)
                .background(Color(0xFF27343F), RoundedCornerShape(64.dp)),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BottomMenuItem(
                label = "رویـــدادها",
                selected = selectedItem == HomeBottomItem.Events,
                onClick = onEventsClick,
                icon = { AlarmIcon(it) },
            )
            BottomMenuItem(
                label = "مدیریت",
                selected = selectedItem == HomeBottomItem.Management,
                onClick = onManagementClick,
                icon = { CarIcon(it) },
            )
            BottomMenuItem(
                label = "نقـــــشه",
                selected = selectedItem == HomeBottomItem.Map,
                onClick = onMapClick,
                icon = { MapIcon(it) },
            )
            BottomMenuItem(
                label = "حساب کاربری",
                selected = selectedItem == HomeBottomItem.Account,
                onClick = onAccountClick,
                icon = { UserIcon(it) },
            )
        }
    }
}

@Composable
private fun BottomMenuItem(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    icon: @Composable (Color) -> Unit,
) {
    val itemColor = if (selected) Color.White else Color.White.copy(alpha = 0.72f)

    Box(
        modifier = Modifier
            .width(66.dp)
            .height(57.dp)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (selected) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .size(10.dp)
                    .background(AppBlue, CircleShape),
            )
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            icon(itemColor)
            Spacer(modifier = Modifier.height(1.dp))
            Text(
                text = label,
                color = itemColor,
                fontSize = 10.sp,
                fontWeight = if (selected) FontWeight.Normal else FontWeight.Light,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun AlarmIcon(color: Color) {
    Canvas(modifier = Modifier.size(24.dp)) {
        val stroke = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        drawArc(
            color = color,
            startAngle = 205f,
            sweepAngle = 130f,
            useCenter = false,
            topLeft = Offset(4.dp.toPx(), 6.dp.toPx()),
            size = androidx.compose.ui.geometry.Size(16.dp.toPx(), 14.dp.toPx()),
            style = stroke,
        )
        drawLine(color, Offset(12.dp.toPx(), 3.dp.toPx()), Offset(12.dp.toPx(), 5.dp.toPx()), strokeWidth = 2.dp.toPx())
        drawLine(color, Offset(7.dp.toPx(), 21.dp.toPx()), Offset(17.dp.toPx(), 21.dp.toPx()), strokeWidth = 2.dp.toPx())
    }
}

@Composable
private fun CarIcon(color: Color) {
    Canvas(modifier = Modifier.size(24.dp)) {
        val stroke = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        val path = Path().apply {
            moveTo(3.dp.toPx(), 14.dp.toPx())
            lineTo(6.dp.toPx(), 9.dp.toPx())
            lineTo(18.dp.toPx(), 9.dp.toPx())
            lineTo(21.dp.toPx(), 14.dp.toPx())
            lineTo(21.dp.toPx(), 18.dp.toPx())
            lineTo(3.dp.toPx(), 18.dp.toPx())
            close()
        }
        drawPath(path, color, style = stroke)
        drawCircle(color, radius = 1.8.dp.toPx(), center = Offset(7.dp.toPx(), 18.dp.toPx()))
        drawCircle(color, radius = 1.8.dp.toPx(), center = Offset(17.dp.toPx(), 18.dp.toPx()))
    }
}

@Composable
private fun MapIcon(color: Color) {
    Canvas(modifier = Modifier.size(24.dp)) {
        drawLine(color, Offset(6.dp.toPx(), 5.dp.toPx()), Offset(6.dp.toPx(), 19.dp.toPx()), strokeWidth = 2.dp.toPx(), cap = StrokeCap.Round)
        drawLine(color, Offset(12.dp.toPx(), 7.dp.toPx()), Offset(12.dp.toPx(), 21.dp.toPx()), strokeWidth = 2.dp.toPx(), cap = StrokeCap.Round)
        drawLine(color, Offset(18.dp.toPx(), 5.dp.toPx()), Offset(18.dp.toPx(), 19.dp.toPx()), strokeWidth = 2.dp.toPx(), cap = StrokeCap.Round)
        drawLine(color, Offset(6.dp.toPx(), 5.dp.toPx()), Offset(12.dp.toPx(), 7.dp.toPx()), strokeWidth = 2.dp.toPx(), cap = StrokeCap.Round)
        drawLine(color, Offset(12.dp.toPx(), 21.dp.toPx()), Offset(18.dp.toPx(), 19.dp.toPx()), strokeWidth = 2.dp.toPx(), cap = StrokeCap.Round)
    }
}

@Composable
private fun UserIcon(color: Color) {
    Canvas(modifier = Modifier.size(24.dp)) {
        val stroke = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        drawCircle(color, radius = 4.dp.toPx(), center = Offset(12.dp.toPx(), 7.dp.toPx()), style = stroke)
        drawArc(
            color = color,
            startAngle = 205f,
            sweepAngle = 130f,
            useCenter = false,
            topLeft = Offset(5.dp.toPx(), 13.dp.toPx()),
            size = androidx.compose.ui.geometry.Size(14.dp.toPx(), 8.dp.toPx()),
            style = stroke,
        )
    }
}
