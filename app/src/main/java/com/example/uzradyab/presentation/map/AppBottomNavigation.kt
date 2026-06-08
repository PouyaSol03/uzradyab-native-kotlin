package com.example.uzradyab.presentation.map

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.uzradyab.ui.theme.AppBlue

enum class BottomNavItem {
    ALARM, MANAGEMENT, MAP, ACCOUNT
}

@Composable
fun AppBottomNavigation(
    selectedItem: BottomNavItem = BottomNavItem.MANAGEMENT,
    onItemSelected: (BottomNavItem) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(57.dp)
            .background(Color(0xFF27343F), RoundedCornerShape(64.dp))
            .padding(horizontal = 32.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BottomNavItemView(
            label = "حساب کاربری",
            icon = Icons.Default.Person,
            isSelected = selectedItem == BottomNavItem.ACCOUNT,
            onClick = { onItemSelected(BottomNavItem.ACCOUNT) }
        )
        BottomNavItemView(
            label = "نقشه",
            icon = Icons.Default.Map,
            isSelected = selectedItem == BottomNavItem.MAP,
            onClick = { onItemSelected(BottomNavItem.MAP) }
        )
        BottomNavItemView(
            label = "مدیریت",
            icon = Icons.Default.DirectionsCar,
            isSelected = selectedItem == BottomNavItem.MANAGEMENT,
            showDot = true,
            onClick = { onItemSelected(BottomNavItem.MANAGEMENT) }
        )
        BottomNavItemView(
            label = "رویدادها",
            icon = Icons.Default.Notifications,
            isSelected = selectedItem == BottomNavItem.ALARM,
            onClick = { onItemSelected(BottomNavItem.ALARM) }
        )
    }
}

@Composable
private fun BottomNavItemView(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    showDot: Boolean = false,
    onClick: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) Color.White else Color(0xB8FFFFFF),
                modifier = Modifier.size(24.dp)
            )
            if (showDot && isSelected) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .align(Alignment.TopEnd)
                        .background(AppBlue, CircleShape)
                )
            }
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            color = if (isSelected) Color.White else Color(0xB8FFFFFF),
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Light,
        )
    }
}
