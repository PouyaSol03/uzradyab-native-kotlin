package com.example.uzradyab.presentation.map

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    Icon(
        imageVector = Icons.Default.GridView,
        contentDescription = "Grid Menu",
        tint = AppTextPrimary,
        modifier = modifier.size(24.dp)
    )
}
