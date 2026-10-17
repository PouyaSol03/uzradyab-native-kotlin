package com.example.uzradyab.presentation.command

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.uzradyab.R
import com.example.uzradyab.ui.theme.themedColor

/** Shared Material 3 sheet for the command flow: 28dp top corners, drag handle, 32% scrim. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CommandBottomSheet(
    onDismissRequest: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = themedColor(light = Color.White, dark = Color(0xFF27343F)),
        scrimColor = Color.Black.copy(alpha = 0.32f),
        dragHandle = { BottomSheetDefaults.DragHandle() },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp),
            content = content,
        )
    }
}

/** Primary action and «انصراف», each half the width. */
@Composable
internal fun SheetActionButtons(
    primaryText: String,
    onPrimary: () -> Unit,
    onCancel: () -> Unit,
    primaryEnabled: Boolean = true,
    loading: Boolean = false,
) {
    val primary = commandPrimary()
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Button(
            onClick = onPrimary,
            enabled = primaryEnabled,
            modifier = Modifier
                .weight(1f)
                .height(48.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = primary,
                contentColor = Color.White,
                disabledContainerColor = primary.copy(alpha = 0.5f),
                disabledContentColor = Color.White,
            ),
        ) {
            if (loading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
            } else {
                Text(primaryText, fontSize = 14.sp)
            }
        }

        Button(
            onClick = onCancel,
            modifier = Modifier
                .weight(1f)
                .height(48.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = themedColor(light = Color(0xFF333638), dark = Color(0xFFAFB3B6)),
            ),
            border = BorderStroke(1.dp, themedColor(light = Color(0xFFBEC1C3), dark = Color(0xFF455A64))),
        ) {
            Text(stringResource(R.string.str_c8d2a1fb), fontSize = 14.sp)
        }
    }
}
