package com.example.uzradyab.presentation.map

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
    Icon(
        imageVector = Icons.Default.Notifications,
        contentDescription = "Events",
        tint = color,
        modifier = Modifier.size(24.dp)
    )
}

@Composable
private fun CarIcon(color: Color) {
    Icon(
        imageVector = Icons.Default.DirectionsCar,
        contentDescription = "Management",
        tint = color,
        modifier = Modifier.size(24.dp)
    )
}

@Composable
private fun MapIcon(color: Color) {
    Icon(
        imageVector = Icons.Default.Map,
        contentDescription = "Map",
        tint = color,
        modifier = Modifier.size(24.dp)
    )
}

@Composable
private fun UserIcon(color: Color) {
    Icon(
        imageVector = Icons.Default.Person,
        contentDescription = "Account",
        tint = color,
        modifier = Modifier.size(24.dp)
    )
}
