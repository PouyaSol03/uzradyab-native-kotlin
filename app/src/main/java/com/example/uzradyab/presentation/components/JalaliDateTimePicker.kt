package com.example.uzradyab.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.uzradyab.core.utils.JalaliUtils
import com.example.uzradyab.core.utils.JalaliUtils.toPersianDigits
import com.example.uzradyab.ui.theme.AppBlue
import java.util.Calendar
import com.example.uzradyab.R
import androidx.compose.ui.res.stringResource

data class JalaliDateTime(
    val year: Int,
    val month: Int, // 1..12
    val day: Int,
    val hour: Int,
    val minute: Int
)

@Composable
fun JalaliDateTimePicker(
    title: String = "انتخاب تاریخ و زمان",
    initialDateTime: JalaliDateTime? = null,
    onConfirm: (JalaliDateTime) -> Unit,
    onCancel: () -> Unit
) {
    val cal = Calendar.getInstance()
    val gYear = cal.get(Calendar.YEAR)
    val gMonth = cal.get(Calendar.MONTH) + 1
    val gDay = cal.get(Calendar.DAY_OF_MONTH)
    val jDate = JalaliUtils.gregorianToJalali(gYear, gMonth, gDay)
    
    var year by remember { mutableStateOf(initialDateTime?.year ?: jDate[0]) }
    var month by remember { mutableStateOf(initialDateTime?.month ?: jDate[1]) }
    var day by remember { mutableStateOf(initialDateTime?.day ?: jDate[2]) }
    var hour by remember { mutableStateOf(initialDateTime?.hour ?: cal.get(Calendar.HOUR_OF_DAY)) }
    var minute by remember { mutableStateOf(initialDateTime?.minute ?: cal.get(Calendar.MINUTE)) }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF384C5C),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Month Navigation
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF0F4F8), RoundedCornerShape(12.dp))
                    .padding(vertical = 4.dp, horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { 
                    if (month == 1) { month = 12; year-- } else { month-- }
                }) {
                    Icon(Icons.Default.ChevronRight, contentDescription = "Previous", tint = AppBlue)
                }
                
                Text(
                    text = "${JalaliUtils.getMonthName(month)} $year".toPersianDigits(),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF384C5C)
                )
                
                IconButton(onClick = { 
                    if (month == 12) { month = 1; year++ } else { month++ }
                }) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = "Next", tint = AppBlue)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Weekdays Header
            val weekdays = listOf("ش", "ی", "د", "س", "چ", "پ", "ج")
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                weekdays.forEach { wd ->
                    Text(
                        text = wd, 
                        fontSize = 14.sp, 
                        color = Color(0xFF6A8BA5), 
                        modifier = Modifier.weight(1f), 
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Days Grid
            val daysInMonth = JalaliUtils.getDaysInJalaliMonth(year, month)
            val firstDayOfWeek = JalaliUtils.getDayOfWeekJalali(year, month, 1) // 0=Sat
            
            var currentDay = 1
            Column(modifier = Modifier.fillMaxWidth()) {
                for (row in 0..5) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        for (col in 0..6) {
                            if (row == 0 && col < firstDayOfWeek) {
                                Box(modifier = Modifier.weight(1f).aspectRatio(1f))
                            } else if (currentDay <= daysInMonth) {
                                val d = currentDay
                                val isSelected = (d == day)
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .padding(4.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) Color(0xFF384C5C) else Color.Transparent)
                                        .clickable { day = d },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = d.toString().toPersianDigits(),
                                        color = if (isSelected) Color.White else Color(0xFF384C5C),
                                        fontSize = 14.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                                currentDay++
                            } else {
                                Box(modifier = Modifier.weight(1f).aspectRatio(1f))
                            }
                        }
                    }
                    if (currentDay > daysInMonth) break
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = Color(0xFFE5E7EB))
            Spacer(modifier = Modifier.height(16.dp))

            // Time Picker
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                NumberPickerBox(
                    value = hour,
                    label = stringResource(R.string.str_1120f944),
                    range = 0..23,
                    onValueChange = { hour = it }
                )
                Text(
                    text = ":", 
                    fontSize = 24.sp, 
                    fontWeight = FontWeight.Bold, 
                    color = Color(0xFFC0CDD8),
                    modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 16.dp)
                )
                NumberPickerBox(
                    value = minute,
                    label = stringResource(R.string.str_4fec5171),
                    range = 0..59,
                    onValueChange = { minute = it }
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Buttons
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f).height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEFF3F5), contentColor = Color(0xFF6A8BA5)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(stringResource(R.string.str_c8d2a1fb), fontSize = 16.sp)
                }
                Button(
                    onClick = { onConfirm(JalaliDateTime(year, month, day, hour, minute)) },
                    modifier = Modifier.weight(1f).height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AppBlue),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(stringResource(R.string.str_911598cd), fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
fun NumberPickerBox(
    value: Int,
    label: String,
    range: IntRange,
    onValueChange: (Int) -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .border(1.dp, Color(0xFFC0CDD8), RoundedCornerShape(12.dp))
                .background(Color.White, RoundedCornerShape(12.dp))
                .padding(horizontal = 4.dp, vertical = 8.dp)
        ) {
            IconButton(onClick = { 
                var nv = value + 1
                if (nv > range.last) nv = range.first
                onValueChange(nv)
            }, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.ChevronRight, "Up", tint = Color(0xFF6A8BA5))
            }
            Text(
                text = value.toString().padStart(2, '0').toPersianDigits(),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF384C5C),
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            IconButton(onClick = { 
                var nv = value - 1
                if (nv < range.first) nv = range.last
                onValueChange(nv)
            }, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.ChevronLeft, "Down", tint = Color(0xFF6A8BA5))
            }
        }
        Text(text = label, fontSize = 14.sp, color = Color(0xFF6A8BA5), modifier = Modifier.padding(top = 8.dp))
    }
}
