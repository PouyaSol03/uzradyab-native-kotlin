package com.example.uzradyab.presentation.map

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
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
fun AppTopToolbar(
    modifier: Modifier = Modifier,
    startContent: @Composable (() -> Unit)? = null,
    centerContent: @Composable (() -> Unit)? = null,
    endContent: @Composable (() -> Unit)? = null,
) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            if (startContent != null) {
                Box(
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    startContent()
                }
            }
            if (centerContent != null) {
                Box(
                    modifier = Modifier.align(Alignment.Center)
                ) {
                    centerContent()
                }
            }
            if (endContent != null) {
                Box(
                    modifier = Modifier.align(Alignment.CenterEnd)
                ) {
                    endContent()
                }
            }
        }
    }
}

@Composable
fun AppLogo(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(id = R.drawable.exir_final_logo_blue),
        contentDescription = "اکسیر ردیاب",
        contentScale = ContentScale.FillBounds,
        modifier = modifier.size(width = 67.dp, height = 24.dp),
    )
}

@Composable
fun BackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(40.dp)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Default.ChevronLeft,
            contentDescription = "Back",
            tint = AppTextPrimary,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
fun MenuGridButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(40.dp)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Default.GridView,
            contentDescription = "Grid Menu",
            tint = AppTextPrimary,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
fun MapTopToolbar(
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier,
    onBackClick: (() -> Unit)? = null,
) {
    AppTopToolbar(
        modifier = modifier,
        startContent = {
            if (onBackClick != null) {
                BackButton(onClick = onBackClick)
            } else {
                AppLogo()
            }
        },
        endContent = {
            MenuGridButton(onClick = onMenuClick)
        }
    )
}
