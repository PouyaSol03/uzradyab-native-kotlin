package com.example.uzradyab.presentation.maintenance

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.uzradyab.presentation.map.AppTopToolbar
import com.example.uzradyab.ui.theme.themedColor

@Composable
fun MaintenanceScreen(
    onBackClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(themedColor(light = Color(0xFFF3F4F6), dark = Color(0xFF1A1D23)))
    ) {
        AppTopToolbar(
            modifier = Modifier.statusBarsPadding(),
            startContent = { com.example.uzradyab.presentation.map.BackButton(onClick = onBackClick) },
            centerContent = {
                Text(
                    text = "سرویس های دوره ای",
                    color = themedColor(light = Color(0xFF333638), dark = Color.White),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        )
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "به زودی...",
                color = themedColor(light = Color(0xFF333638), dark = Color.White),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
