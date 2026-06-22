package com.example.uzradyab.presentation.geofence

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Layers
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.Dialog
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
                            .border(
                                width = 1.dp,
                                color = Color(0xFFE0E0E0),
                                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                            )
                            .clickable(enabled = false) { }
                            .padding(top = 12.dp, start = 16.dp, end = 16.dp, bottom = 16.dp)
                            .navigationBarsPadding()
                    ) {
                        Column {
                            Box(
                                modifier = Modifier
                                    .width(40.dp)
                                    .height(5.dp)
                                    .clip(RoundedCornerShape(2.5.dp))
                                    .background(Color(0xFFE0E0E0))
                                    .align(Alignment.CenterHorizontally)
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            if (state.addingMode) {
                                AddGeofencePanel(
                                    name = state.newGeofenceName,
                                    radius = state.newGeofenceRadius,
                                    drawMode = state.drawMode,
                                    pointsCount = state.activeDrawingPoints.size,
                                    onNameChange = { viewModel.updateNewGeofenceName(it) },
                                    onRadiusChange = { viewModel.updateNewGeofenceRadius(it) },
                                    onCancel = { viewModel.toggleAddingMode() },
                                    onSave = { viewModel.saveNewGeofence() },
                                    onModeSelect = { viewModel.setDrawMode(it) },
                                    onUndo = { viewModel.undoLastDrawingPoint() },
                                    onClear = { viewModel.clearDrawingPoints() }
                                )
                            } else {
                                GeofenceListPanel(
                                    geofences = state.geofences,
                                    onAddClick = { viewModel.toggleAddingMode() },
                                    onDeleteClick = { viewModel.deleteGeofence(it) },
                                    onItemClick = { viewModel.selectGeofence(it) }
                                )
                            }
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
    name: String,
    radius: Double,
    drawMode: DrawMode,
    pointsCount: Int,
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
                selected = drawMode == DrawMode.CIRCLE,
                onClick = { onModeSelect(DrawMode.CIRCLE) },
                label = { Text("دایره") }
            )
            FilterChip(
                selected = drawMode == DrawMode.POLYGON,
                onClick = { onModeSelect(DrawMode.POLYGON) },
                label = { Text("چند ضلعی") }
            )
            FilterChip(
                selected = drawMode == DrawMode.LINESTRING,
                onClick = { onModeSelect(DrawMode.LINESTRING) },
                label = { Text("مسیر (خط)") }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        val instructionText = when (drawMode) {
            DrawMode.CIRCLE -> "روی نقشه ضربه بزنید تا مرکز دایره مشخص شود."
            DrawMode.POLYGON -> "برای رسم چند ضلعی، حداقل ۳ نقطه روی نقشه انتخاب کنید."
            DrawMode.LINESTRING -> "برای رسم مسیر، نقاط را روی نقشه انتخاب کنید."
        }
        Text(instructionText, fontSize = 12.sp, color = Color.Gray)

        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text("نام محدوده") },
            modifier = Modifier.fillMaxWidth()
        )

        if (drawMode == DrawMode.CIRCLE) {
            Spacer(modifier = Modifier.height(16.dp))
            Text("شعاع: ${radius.toInt()} متر", fontSize = 14.sp)
            Slider(
                value = radius.toFloat(),
                onValueChange = { onRadiusChange(it.toDouble()) },
                valueRange = 50f..5000f,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedButton(
                    onClick = onUndo,
                    enabled = pointsCount > 0,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Undo, contentDescription = "Undo")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("بازگشت")
                }
                OutlinedButton(
                    onClick = onClear,
                    enabled = pointsCount > 0,
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

            val isValid = when (drawMode) {
                DrawMode.CIRCLE -> pointsCount > 0
                DrawMode.POLYGON -> pointsCount >= 3
                DrawMode.LINESTRING -> pointsCount >= 2
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
    geofences: List<Geofence>,
    onAddClick: () -> Unit,
    onDeleteClick: (Long) -> Unit,
    onItemClick: (Long) -> Unit
) {
    val geofenceToDelete = remember { mutableStateOf<Geofence?>(null) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("محدوده‌های من", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF384C5C))
            Box(
                modifier = Modifier
                    .height(36.dp)
                    .background(AppBlue, RoundedCornerShape(8.dp))
                    .clickable(onClick = onAddClick)
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.Center,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Geofence",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "افزودن محدوده",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }

        if (geofences.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp), contentAlignment = Alignment.Center) {
                Text("هیچ محدوده‌ای ثبت نشده است.", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false),
                contentPadding = PaddingValues(bottom = 8.dp)
            ) {
                items(
                    items = geofences,
                    key = { geofence -> geofence.id }
                ) { geofence ->
                    GeofenceItem(
                        geofence = geofence,
                        onClick = { onItemClick(geofence.id) },
                        onDeleteClick = { geofenceToDelete.value = geofence }
                    )
                }
            }
        }
    }

    if (geofenceToDelete.value != null) {
        DeleteGeofenceDialog(
            geofence = geofenceToDelete.value!!,
            onDismiss = { geofenceToDelete.value = null },
            onConfirm = {
                onDeleteClick(geofenceToDelete.value!!.id)
                geofenceToDelete.value = null
            }
        )
    }
}

@Composable
fun GeofenceItem(
    geofence: Geofence,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF7F9FA))
            .clickable { onClick() }
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
        IconButton(onClick = onDeleteClick) {
            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.7f))
        }
    }
}

@Composable
fun DeleteGeofenceDialog(
    geofence: Geofence,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Title and Icon
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color(0xFFE55353)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "حذف محدوده جغرافیایی ${geofence.name}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF384C5C)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Description
                Text(
                    text = "با حذف محدوده جغرافیایی امکان بازیابی مجدد آن وجود ندارد. آیا از حذف آن مطمئن هستید؟",
                    fontSize = 13.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Right,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onDismiss
                    ) {
                        Text("انصراف", color = AppBlue, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    }
                    Spacer(modifier = Modifier.width(24.dp))
                    Button(
                        onClick = onConfirm,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE55353)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .height(44.dp)
                            .widthIn(min = 120.dp)
                    ) {
                        Text("بله", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
