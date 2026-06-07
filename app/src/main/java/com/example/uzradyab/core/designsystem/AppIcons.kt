package com.example.uzradyab.core.designsystem

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun PhoneIcon(
    modifier: Modifier = Modifier,
    color: Color,
) {
    Icon(
        imageVector = Icons.Default.PhoneAndroid,
        contentDescription = "Phone",
        tint = color,
        modifier = modifier.size(28.dp)
    )
}

@Composable
fun KeyIcon(
    modifier: Modifier = Modifier,
    color: Color,
) {
    Icon(
        imageVector = Icons.Default.VpnKey,
        contentDescription = "Key",
        tint = color,
        modifier = modifier.size(30.dp)
    )
}

@Composable
fun EyeOffIcon(
    modifier: Modifier = Modifier,
    color: Color,
) {
    Icon(
        imageVector = Icons.Default.VisibilityOff,
        contentDescription = "Hide Password",
        tint = color,
        modifier = modifier.size(30.dp)
    )
}

