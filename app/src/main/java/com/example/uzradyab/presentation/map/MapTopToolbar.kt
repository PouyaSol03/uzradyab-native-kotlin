package com.example.uzradyab.presentation.map

import androidx.compose.foundation.Image
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.example.uzradyab.R
import com.example.uzradyab.ui.theme.AppTextPrimary

@Composable
fun MapTopToolbar(
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = painterResource(id = R.drawable.exir_final_logo_blue),
                contentDescription = "اکسیر ردیاب",
                contentScale = ContentScale.FillBounds,
                modifier = Modifier.size(width = 67.dp, height = 24.dp),
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
