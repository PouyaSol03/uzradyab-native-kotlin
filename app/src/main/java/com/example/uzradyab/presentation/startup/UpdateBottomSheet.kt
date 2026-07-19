package com.example.uzradyab.presentation.startup

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.uzradyab.R
import com.example.uzradyab.core.utils.FormatUtils.toPersianDigits
import com.example.uzradyab.data.remote.dto.AppConfigDto
import com.example.uzradyab.ui.theme.themedColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateBottomSheet(
    config: AppConfigDto,
    onUpdateClick: () -> Unit,
    onLaterClick: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    
    ModalBottomSheet(
        onDismissRequest = onLaterClick,
        sheetState = sheetState,
        containerColor = themedColor(light = Color.White, dark = Color(0xFF27343F)),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.exir_final_logo_blue),
                contentDescription = "Logo",
                modifier = Modifier
                    .size(100.dp)
                    .padding(8.dp)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "نسخه ${config.newReleaseCode?.toPersianDigits() ?: ""} آماده به‌روزرسانی!",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = themedColor(light = Color(0xFF1E293B), dark = Color(0xFFBBC8DD)),
                textAlign = TextAlign.Right,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            var expanded by remember { mutableStateOf(false) }
            val displayItems = if (expanded) {
                config.newReleaseDescription
            } else {
                config.newReleaseDescription.take(3)
            }
            
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .animateContentSize()
            ) {
                items(displayItems) { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(top = 8.dp)
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(themedColor(light = Color(0xFF94A3B8), dark = Color(0xFF333E4D)))
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = item.value.toPersianDigits(),
                            fontSize = 14.sp,
                            color = themedColor(light = Color(0xFF475569), dark = Color(0xFFA4B0C1)),
                            lineHeight = 22.sp,
                            textAlign = TextAlign.Right,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                
                if (config.newReleaseDescription.size > 3) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { expanded = !expanded }
                                .padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (expanded) "کمتر" else "بیشتر",
                                color = themedColor(light = Color(0xFF2563EB), dark = Color(0xFF648DE7)),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Icon(
                                imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null,
                                tint = themedColor(light = Color(0xFF2563EB), dark = Color(0xFF648DE7))
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TextButton(
                    onClick = onLaterClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .background(themedColor(light = Color(0xFFF1F5F9), dark = Color(0xFF121F2B)), RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "بعداً",
                        color = themedColor(light = Color(0xFF1E293B), dark = Color(0xFFBBC8DD)),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                Button(
                    onClick = onUpdateClick,
                    modifier = Modifier
                        .weight(2f)
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = themedColor(light = Color(0xFF2563EB), dark = Color(0xFF648DE7)))
                ) {
                    Text(
                        text = "به‌روزرسانی",
                        color = themedColor(light = Color.White, dark = Color.White),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
