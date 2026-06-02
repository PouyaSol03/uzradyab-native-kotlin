package com.example.uzradyab.presentation.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.uzradyab.core.designsystem.AppPrimaryButton
import com.example.uzradyab.core.designsystem.AppTextAction

@Composable
fun UzradyabPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    AppPrimaryButton(
        text = text,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
    )
}

@Composable
fun UzradyabTextAction(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AppTextAction(
        text = text,
        onClick = onClick,
        modifier = modifier,
    )
}
