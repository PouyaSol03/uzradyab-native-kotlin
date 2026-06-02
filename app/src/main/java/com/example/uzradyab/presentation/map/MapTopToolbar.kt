package com.example.uzradyab.presentation.map

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.uzradyab.ui.theme.AppBlue
import com.example.uzradyab.ui.theme.AppTextPrimary

@Composable
fun MapTopToolbar(
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "اوزرادیاب",
            color = AppBlue,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
        )
        Box(
            modifier = Modifier
                .size(40.dp)
                .clickable(onClick = onMenuClick),
            contentAlignment = Alignment.Center,
        ) {
            MenuGridIcon()
        }
    }
}

@Composable
private fun MenuGridIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(24.dp)) {
        val dot = 2.96.dp.toPx()
        val xs = listOf(5.5.dp.toPx(), 16.5.dp.toPx())
        val ys = listOf(5.5.dp.toPx(), 17.5.dp.toPx())
        xs.forEach { x ->
            ys.forEach { y ->
                drawCircle(
                    color = AppTextPrimary,
                    radius = dot,
                    center = Offset(x, y),
                )
            }
        }
    }
}
