package com.example.uzradyab.presentation.reports

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.uzradyab.R
import androidx.compose.ui.res.stringResource
import com.example.uzradyab.ui.theme.themedColor

data class ColumnOption(
    val id: String,
    val name: String,
    val isRequired: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColumnsSelectionBottomSheet(
    options: List<ColumnOption>,
    selectedIds: Set<String>,
    onToggle: (String) -> Unit,
    onDismiss: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = themedColor(light = Color.White, dark = Color(0xFF27343F)),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.str_d3ed1245),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = themedColor(light = Color(0xFF1C262E), dark = Color(0xFFC0CDD8))
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = themedColor(light = Color(0xFF676C70), dark = Color(0xFF929292))
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = stringResource(R.string.str_da3316cb),
                    fontSize = 14.sp,
                    color = themedColor(light = Color(0xFF676C70), dark = Color(0xFF929292)),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(options) { option ->
                        val isSelected = selectedIds.contains(option.id) || option.isRequired
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) themedColor(light = Color(0xFFEBF3FF), dark = Color(0xFF00183D)) else themedColor(light = Color(0xFFF8F9FA), dark = Color(0xFF1A1F24)))
                                .clickable(enabled = !option.isRequired) {
                                    onToggle(option.id)
                                }
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = option.name,
                                fontSize = 15.sp,
                                fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                                color = if (isSelected) themedColor(light = Color(0xFF307EF3), dark = Color(0xFF5F98EC)) else themedColor(light = Color(0xFF495057), dark = Color(0xFFACB2B9))
                            )
                            
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = if (option.isRequired) null else { { onToggle(option.id) } },
                                enabled = !option.isRequired,
                                colors = CheckboxDefaults.colors(
                                    checkedColor = themedColor(light = Color(0xFF307EF3), dark = Color(0xFF5F98EC)),
                                    checkmarkColor = themedColor(light = Color.White, dark = Color.White),
                                    uncheckedColor = themedColor(light = Color(0xFFCED4DA), dark = Color(0xFF21262C)),
                                    disabledCheckedColor = themedColor(light = Color(0xFF90CAF9), dark = Color(0xFF074679))
                                )
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = themedColor(light = Color(0xFF307EF3), dark = Color(0xFF5F98EC)))
                ) {
                    Text(stringResource(R.string.str_911598cd), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = themedColor(light = Color.White, dark = Color.White))
                }
            }
        }
    }
}
