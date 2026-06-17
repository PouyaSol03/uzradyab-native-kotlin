package com.example.uzradyab.presentation.geofence

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.uzradyab.domain.model.Geofence
import com.example.uzradyab.presentation.map.AppTopToolbar
import com.example.uzradyab.ui.theme.AppBlue

@Composable
fun GeofenceRoute(
    onBackClick: () -> Unit,
    viewModel: GeofenceViewModel = hiltViewModel()
) {
    GeofenceScreen(onBackClick = onBackClick, viewModel = viewModel)
}

@Composable
fun GeofenceScreen(
    onBackClick: () -> Unit,
    viewModel: GeofenceViewModel
) {
    val state by viewModel.state.collectAsState()

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
            ) {
                AppTopToolbar(
                    modifier = Modifier.height(64.dp),
                    startContent = {
                        com.example.uzradyab.presentation.map.BackButton(onClick = onBackClick)
                    },
                    centerContent = {
                        Text(
                            text = "محدوده جغرافیایی",
                            color = Color(0xFF676C70),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                    .background(MaterialTheme.colorScheme.background)
            ) {
                GeofenceMap(
                    state = state,
                    onMapClick = { lat, lon ->
                        if (state.addingMode) {
                            viewModel.addDrawingPoint(lat, lon)
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )

                if (state.addingMode || state.geofences.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                            .background(Color.White)
                            .padding(16.dp)
                            .navigationBarsPadding()
                    ) {
                        if (state.addingMode) {
                            AddGeofencePanel(
                                state = state,
                                onNameChange = { viewModel.updateNewGeofence(it, state.newGeofenceRadius) },
                                onRadiusChange = { viewModel.updateNewGeofence(state.newGeofenceName, it) },
                                onCancel = { viewModel.toggleAddingMode() },
                                onSave = { viewModel.saveNewGeofence() },
                                onModeSelect = { viewModel.setDrawMode(it) },
                                onUndo = { viewModel.undoLastDrawingPoint() },
                                onClear = { viewModel.clearDrawingPoints() }
                            )
                        } else {
                            GeofenceListPanel(
                                state = state,
                                onAddClick = { viewModel.toggleAddingMode() },
                                onDeleteClick = { viewModel.deleteGeofence(it) }
                            )
                        }
                    }
                } else {
                    FloatingActionButton(
                        onClick = { viewModel.toggleAddingMode() },
                        containerColor = AppBlue,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(16.dp)
                            .navigationBarsPadding(),
                        shape = androidx.compose.foundation.shape.CircleShape
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = "Add Geofence", tint = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun AddGeofencePanel(
    state: GeofenceState,
    onNameChange: (String) -> Unit,
    onRadiusChange: (Double) -> Unit,
    onCancel: () -> Unit,
    onSave: () -> Unit,
    onModeSelect: (DrawMode) -> Unit,
    onUndo: () -> Unit,
    onClear: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("افزودن محدوده جدید", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF384C5C))
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = state.drawMode == DrawMode.CIRCLE,
                onClick = { onModeSelect(DrawMode.CIRCLE) },
                label = { Text("دایره") }
            )
            FilterChip(
                selected = state.drawMode == DrawMode.POLYGON,
                onClick = { onModeSelect(DrawMode.POLYGON) },
                label = { Text("چند ضلعی") }
            )
            FilterChip(
                selected = state.drawMode == DrawMode.LINESTRING,
                onClick = { onModeSelect(DrawMode.LINESTRING) },
                label = { Text("مسیر (خط)") }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        val instructionText = when (state.drawMode) {
            DrawMode.CIRCLE -> "روی نقشه ضربه بزنید تا مرکز دایره مشخص شود."
            DrawMode.POLYGON -> "برای رسم چند ضلعی، حداقل ۳ نقطه روی نقشه انتخاب کنید."
            DrawMode.LINESTRING -> "برای رسم مسیر، نقاط را روی نقشه انتخاب کنید."
        }
        Text(instructionText, fontSize = 12.sp, color = Color.Gray)
        
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = state.newGeofenceName,
            onValueChange = onNameChange,
            label = { Text("نام محدوده") },
            modifier = Modifier.fillMaxWidth()
        )
        
        if (state.drawMode == DrawMode.CIRCLE) {
            Spacer(modifier = Modifier.height(16.dp))
            Text("شعاع: ${state.newGeofenceRadius.toInt()} متر", fontSize = 14.sp)
            Slider(
                value = state.newGeofenceRadius.toFloat(),
                onValueChange = { onRadiusChange(it.toDouble()) },
                valueRange = 50f..5000f,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedButton(
                    onClick = onUndo,
                    enabled = state.activeDrawingPoints.isNotEmpty(),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Undo, contentDescription = "Undo")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("بازگشت")
                }
                OutlinedButton(
                    onClick = onClear,
                    enabled = state.activeDrawingPoints.isNotEmpty(),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("پاک کردن")
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(
                onClick = onCancel,
                colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray),
                modifier = Modifier.weight(1f)
            ) {
                Text("انصراف", color = Color.Black)
            }
            
            val isValid = when (state.drawMode) {
                DrawMode.CIRCLE -> state.activeDrawingPoints.isNotEmpty()
                DrawMode.POLYGON -> state.activeDrawingPoints.size >= 3
                DrawMode.LINESTRING -> state.activeDrawingPoints.size >= 2
            }
            
            Button(
                onClick = onSave,
                colors = ButtonDefaults.buttonColors(containerColor = AppBlue),
                enabled = isValid,
                modifier = Modifier.weight(1f)
            ) {
                Text("ذخیره", color = Color.White)
            }
        }
    }
}

@Composable
fun GeofenceListPanel(
    state: GeofenceState,
    onAddClick: () -> Unit,
    onDeleteClick: (Long) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().heightIn(min = 200.dp, max = 400.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("محدوده‌های من", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF384C5C))
            IconButton(onClick = onAddClick) {
                Icon(Icons.Default.Add, contentDescription = "Add Geofence", tint = AppBlue)
            }
        }
        
        if (state.geofences.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                Text("هیچ محدوده‌ای ثبت نشده است.", color = Color.Gray)
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f)) {
                items(state.geofences) { geofence ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .background(Color(0xFFF7F9FA), RoundedCornerShape(8.dp))
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Layers, contentDescription = null, tint = AppBlue)
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(geofence.name, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                            if (geofence.isCircle) {
                                Text("شعاع: ${geofence.radius?.toInt()} متر", fontSize = 12.sp, color = Color.Gray)
                            } else {
                                Text("چند ضلعی", fontSize = 12.sp, color = Color.Gray)
                            }
                        }
                        IconButton(onClick = { /* TODO: Implement Edit */ }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.Gray)
                        }
                        IconButton(onClick = { onDeleteClick(geofence.id) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                        }
                    }
                }
            }
        }
    }
}
