package com.example.uzradyab.presentation.maintenance

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterVintage
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.uzradyab.R
import com.example.uzradyab.core.utils.FormatUtils.toPersianDigits
import com.example.uzradyab.domain.model.Maintenance
import com.example.uzradyab.domain.model.MaintenanceStatusLevel
import com.example.uzradyab.presentation.components.LocalSnackbarController
import com.example.uzradyab.presentation.map.AppTopToolbar
import com.example.uzradyab.presentation.map.BackButton
import com.example.uzradyab.presentation.map.DeviceSelectDialog
import com.example.uzradyab.presentation.map.DeviceSelectTrigger
import com.example.uzradyab.ui.theme.UzradyabTheme
import com.example.uzradyab.ui.theme.themedColor
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToLong

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaintenanceScreen(
    onBackClick: () -> Unit,
    viewModel: MaintenanceViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarController = LocalSnackbarController.current

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            snackbarController.showError(it)
            viewModel.clearMessages()
        }
    }

    LaunchedEffect(state.successMessage) {
        state.successMessage?.let {
            snackbarController.showSuccess(it)
            viewModel.clearMessages()
        }
    }

    var showDeviceSelector by remember { mutableStateOf(false) }
    var serviceToDelete by remember { mutableStateOf<Maintenance?>(null) }
    var serviceToReset by remember { mutableStateOf<Maintenance?>(null) }

    val figmaBackground = themedColor(light = Color(0xFFF6F8FA), dark = Color(0xFF14171D))
    val cardBg = themedColor(light = Color.White, dark = Color(0xFF27343F))

    val selectedDevice = remember(state.devices, state.selectedDeviceId) {
        state.devices.firstOrNull { it.id == state.selectedDeviceId }
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            containerColor = figmaBackground,
            topBar = {
                AppTopToolbar(
                    modifier = Modifier
                        .statusBarsPadding()
                        .height(64.dp),
                    startContent = {
                        BackButton(onClick = onBackClick)
                    },
                    centerContent = {
                        Text(
                            text = stringResource(R.string.str_c2d21116),
                            color = themedColor(light = Color(0xFF676C70), dark = Color(0xFF929292)),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                )
            },
            floatingActionButton = {
                if (state.selectedDeviceId != null) {
                    ExtendedFloatingActionButton(
                        onClick = viewModel::openAddSheet,
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White,
                        icon = { Icon(Icons.Default.Add, contentDescription = null) },
                        text = {
                            Text(
                                text = "افزودن سرویس جدید",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        shape = RoundedCornerShape(16.dp)
                    )
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Device selector row with DeviceSelectTrigger
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    DeviceSelectTrigger(
                        text = selectedDevice?.name ?: "انتخاب دستگاه",
                        onClick = { showDeviceSelector = true },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Current Vehicle Odometer card
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(cardBg)
                            .border(
                                1.dp,
                                themedColor(light = Color(0xFFE2E8F0), dark = Color(0xFF333E48)),
                                RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Speed,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "کیلومتر کارکرد فعلی خودرو:",
                                fontSize = 13.sp,
                                color = UzradyabTheme.colors.textBody
                            )
                        }
                        Text(
                            text = "${formatNumberWithCommas(state.currentOdometerKm.roundToLong()).toPersianDigits()} کیلومتر",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                if (state.isLoading && state.maintenances.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "در حال بارگذاری سرویس‌ها...",
                                color = UzradyabTheme.colors.textBody,
                                fontSize = 14.sp
                            )
                        }
                    }
                } else if (state.maintenances.isEmpty()) {
                    EmptyMaintenanceState(
                        onAddClick = viewModel::openAddSheet,
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(state.maintenances, key = { it.id }) { maintenance ->
                            MaintenanceCard(
                                maintenance = maintenance,
                                currentOdometerKm = state.currentOdometerKm,
                                onResetClick = { serviceToReset = maintenance },
                                onEditClick = { viewModel.openEditSheet(maintenance) },
                                onDeleteClick = { serviceToDelete = maintenance }
                            )
                        }
                    }
                }
            }
        }

        // Device selection dialog
        if (showDeviceSelector) {
            DeviceSelectDialog(
                devices = state.devices,
                selectedDeviceId = state.selectedDeviceId,
                onDeviceClick = { deviceId ->
                    viewModel.selectDevice(deviceId)
                    showDeviceSelector = false
                },
                onDismiss = { showDeviceSelector = false }
            )
        }

        // Add/Edit Bottom Sheet
        if (state.showAddEditSheet) {
            AddEditMaintenanceBottomSheet(
                editing = state.editingMaintenance,
                currentOdometerKm = state.currentOdometerKm,
                isSubmitting = state.isSubmitting,
                onDismiss = viewModel::closeSheet,
                onSave = viewModel::saveService
            )
        }

        // Delete Confirmation Bottom Sheet
        serviceToDelete?.let { item ->
            DeleteMaintenanceBottomSheet(
                maintenance = item,
                isSubmitting = state.isSubmitting,
                onDismiss = { serviceToDelete = null },
                onConfirm = {
                    viewModel.deleteService(item.id)
                    serviceToDelete = null
                }
            )
        }

        // Service Done Bottom Sheet (Accept / Record Maintenance)
        serviceToReset?.let { item ->
            ServiceDoneBottomSheet(
                maintenance = item,
                currentOdometerKm = state.currentOdometerKm,
                isSubmitting = state.isSubmitting,
                onDismiss = { serviceToReset = null },
                onConfirm = { customKm ->
                    viewModel.markServiceDone(item, customKm)
                    serviceToReset = null
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ServiceDoneBottomSheet(
    maintenance: Maintenance,
    currentOdometerKm: Double,
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var customKmStr by remember(maintenance, currentOdometerKm) {
        mutableStateOf(currentOdometerKm.roundToLong().toString())
    }

    val parsedKm = customKmStr.toDoubleOrNull() ?: currentOdometerKm
    val nextServiceKm = parsedKm + maintenance.periodKm

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = themedColor(light = Color.White, dark = Color(0xFF222B35)),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header icon + Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "ثبت انجام سرویس",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = themedColor(light = Color(0xFF1E293B), dark = Color.White)
                    )
                    Text(
                        text = maintenance.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Summary Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(themedColor(light = Color(0xFFF8FAFC), dark = Color(0xFF1C242D)))
                    .border(
                        1.dp,
                        themedColor(light = Color(0xFFE2E8F0), dark = Color(0xFF2F3B48)),
                        RoundedCornerShape(14.dp)
                    )
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("دوره تکرار سرویس:", fontSize = 12.sp, color = UzradyabTheme.colors.textMuted)
                    Text(
                        "${formatNumberWithCommas(maintenance.periodKm.roundToLong()).toPersianDigits()} کیلومتر",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = themedColor(light = Color(0xFF1E293B), dark = Color.White)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("موعد سرویس بعدی:", fontSize = 12.sp, color = UzradyabTheme.colors.textMuted)
                    Text(
                        "${formatNumberWithCommas(nextServiceKm.roundToLong()).toPersianDigits()} کیلومتر",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Kilometer Input Field (pre-filled with current device kilometers)
            OutlinedTextField(
                value = customKmStr,
                onValueChange = { input ->
                    if (input.all(Char::isDigit)) {
                        customKmStr = input
                    }
                },
                label = { Text("کیلومتر انجام سرویس") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                textStyle = androidx.compose.ui.text.TextStyle(textDirection = TextDirection.Ltr),
                modifier = Modifier.fillMaxWidth(),
                supportingText = {
                    Text(
                        text = "کیلومتر شروع از این مقدار محاسبه و سرویس بازنشانی می‌شود.",
                        color = UzradyabTheme.colors.textMuted,
                        fontSize = 11.sp
                    )
                },
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = themedColor(light = Color(0xFFCBD5E1), dark = Color(0xFF475569))
                )
            )

            // Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("انصراف", color = UzradyabTheme.colors.textBody)
                }

                Button(
                    onClick = {
                        val km = customKmStr.toDoubleOrNull() ?: currentOdometerKm
                        onConfirm(km)
                    },
                    modifier = Modifier
                        .weight(1.5f)
                        .height(48.dp),
                    enabled = !isSubmitting,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("تایید و ثبت سرویس", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DeleteMaintenanceBottomSheet(
    maintenance: Maintenance,
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = themedColor(light = Color.White, dark = Color(0xFF222B35)),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header with Red Trash Icon
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFFEE2E2)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(26.dp)
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "حذف سرویس دوره‌ای",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = themedColor(light = Color(0xFF1E293B), dark = Color.White)
                    )
                    Text(
                        text = maintenance.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFEF4444)
                    )
                }
            }

            // Warning message card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(themedColor(light = Color(0xFFFEF2F2), dark = Color(0xFF2E1A1A)))
                    .border(1.dp, Color(0xFFFCA5A5).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "آیا از حذف سرویس «${maintenance.name}» اطمینان دارید؟",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = themedColor(light = Color(0xFF991B1B), dark = Color(0xFFFCA5A5))
                )
                Text(
                    text = "با حذف این مورد، سابقه این سرویس برای خودرو حذف شده و یادآوری دوره‌ای آن متوقف خواهد شد. این عملیات قابل بازگشت نیست.",
                    fontSize = 12.sp,
                    color = themedColor(light = Color(0xFFB91C1C), dark = Color(0xFFF87171)),
                    lineHeight = 20.sp
                )
            }

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("انصراف", color = UzradyabTheme.colors.textBody)
                }

                Button(
                    onClick = onConfirm,
                    modifier = Modifier
                        .weight(1.5f)
                        .height(48.dp),
                    enabled = !isSubmitting,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("حذف سرویس", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
private fun MaintenanceCard(
    maintenance: Maintenance,
    currentOdometerKm: Double,
    onResetClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val cardBg = themedColor(light = Color.White, dark = Color(0xFF27343F))
    val statusLevel = maintenance.statusLevel(currentOdometerKm)
    val remainingKm = maintenance.remainingKm(currentOdometerKm)
    val progress = maintenance.progress(currentOdometerKm)

    val (badgeText, badgeBgColor, badgeTextColor) = when (statusLevel) {
        MaintenanceStatusLevel.Overdue -> Triple(
            "نیاز به تعویض فوری!",
            Color(0xFFFEE2E2),
            Color(0xFFDC2626)
        )
        MaintenanceStatusLevel.Warning -> Triple(
            "نزدیک موعد سرویس",
            Color(0xFFFEF3C7),
            Color(0xFFD97706)
        )
        MaintenanceStatusLevel.Normal -> Triple(
            "وضعیت مناسب",
            Color(0xFFDCFCE7),
            Color(0xFF16A34A)
        )
    }

    val progressColor by animateColorAsState(
        targetValue = when (statusLevel) {
            MaintenanceStatusLevel.Overdue -> Color(0xFFDC2626)
            MaintenanceStatusLevel.Warning -> Color(0xFFF59E0B)
            MaintenanceStatusLevel.Normal -> MaterialTheme.colorScheme.primary
        },
        label = "progressColor"
    )

    var showMenu by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(cardBg)
            .border(1.dp, themedColor(light = Color(0xFFE5E9EF), dark = Color(0xFF333E48)), RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Card Top Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(badgeBgColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getServiceIcon(maintenance.name),
                        contentDescription = null,
                        tint = badgeTextColor,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = maintenance.name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = themedColor(light = Color(0xFF1E293B), dark = Color.White)
                    )

                    // Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(badgeBgColor)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = badgeText,
                            color = badgeTextColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "گزینه‌ها",
                        tint = UzradyabTheme.colors.textMuted
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    modifier = Modifier.background(cardBg)
                ) {
                    DropdownMenuItem(
                        text = { Text("ویرایش سرویس") },
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                        onClick = {
                            showMenu = false
                            onEditClick()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("حذف سرویس", color = Color(0xFFEF4444)) },
                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFEF4444)) },
                        onClick = {
                            showMenu = false
                            onDeleteClick()
                        }
                    )
                }
            }
        }

        // Progress Bar
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (remainingKm <= 0.0) {
                        "${formatNumberWithCommas(abs(remainingKm).roundToLong()).toPersianDigits()} کیلومتر از موعد گذشته!"
                    } else {
                        "${formatNumberWithCommas(remainingKm.roundToLong()).toPersianDigits()} کیلومتر مانده"
                    },
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = badgeTextColor
                )

                Text(
                    text = "${(progress * 100).toInt().toString().toPersianDigits()}٪ مصرف شده",
                    fontSize = 11.sp,
                    color = UzradyabTheme.colors.textMuted
                )
            }

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = progressColor,
                trackColor = themedColor(light = Color(0xFFE2E8F0), dark = Color(0xFF374151))
            )
        }

        // Details 3-column row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(themedColor(light = Color(0xFFF8FAFC), dark = Color(0xFF1E242C)))
                .padding(vertical = 10.dp, horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            MetricItem(
                label = "دوره تعویض",
                value = "${formatNumberWithCommas(maintenance.periodKm.roundToLong()).toPersianDigits()} km"
            )
            MetricDivider()
            MetricItem(
                label = "آخرین سرویس",
                value = "${formatNumberWithCommas(maintenance.startKm.roundToLong()).toPersianDigits()} km"
            )
            MetricDivider()
            MetricItem(
                label = if (remainingKm <= 0.0) "اضافه کارکرد" else "کیلومتر بعدی",
                value = if (remainingKm <= 0.0) {
                    "+${formatNumberWithCommas(abs(remainingKm).roundToLong()).toPersianDigits()}"
                } else {
                    formatNumberWithCommas((maintenance.startKm + maintenance.periodKm).roundToLong()).toPersianDigits()
                }
            )
        }

        // Action: Mark as Serviced Today
        Button(
            onClick = onResetClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(42.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                contentColor = MaterialTheme.colorScheme.primary
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "ثبت انجام سرویس (بروزرسانی به امروز)",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun MetricItem(label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = UzradyabTheme.colors.textMuted
        )
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = themedColor(light = Color(0xFF1E293B), dark = Color.White)
        )
    }
}

@Composable
private fun MetricDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(24.dp)
            .background(themedColor(light = Color(0xFFE2E8F0), dark = Color(0xFF374151)))
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddEditMaintenanceBottomSheet(
    editing: Maintenance?,
    currentOdometerKm: Double,
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onSave: (name: String, periodKm: Double, startKm: Double) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var name by remember(editing) { mutableStateOf(editing?.name ?: "") }
    var periodKmStr by remember(editing) {
        mutableStateOf(editing?.let { it.periodKm.roundToLong().toString() } ?: "5000")
    }
    var startKmStr by remember(editing, currentOdometerKm) {
        mutableStateOf(
            editing?.let { it.startKm.roundToLong().toString() }
                ?: currentOdometerKm.roundToLong().toString()
        )
    }

    var nameError by remember { mutableStateOf<String?>(null) }
    var periodError by remember { mutableStateOf<String?>(null) }

    // Popular Presets for Iranian vehicles
    val presets = remember {
        listOf(
            PresetService("روغن موتور", 5000),
            PresetService("فیلتر روغن", 10000),
            PresetService("فیلتر هوا", 10000),
            PresetService("فیلتر بنزین", 20000),
            PresetService("لنت ترمز جلو", 30000),
            PresetService("لنت ترمز عقب", 50000),
            PresetService("تسمه تایم", 60000),
            PresetService("ضدیخ و آب رادیاتور", 20000),
            PresetService("روغن گیربکس", 40000),
            PresetService("شمع موتور", 40000),
            PresetService("جابجایی لاستیک‌ها", 20000),
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = themedColor(light = Color.White, dark = Color(0xFF222B35)),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = if (editing != null) "ویرایش سرویس دوره‌ای" else "افزودن سرویس دوره‌ای جدید",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = themedColor(light = Color(0xFF1E293B), dark = Color.White)
            )

            // Preset Chips (shown when creating new)
            if (editing == null) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "انتخاب سریع از موارد پرکاربرد:",
                        fontSize = 12.sp,
                        color = UzradyabTheme.colors.textMuted
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(presets) { preset ->
                            val isSelected = name == preset.name
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                        else themedColor(light = Color(0xFFF1F5F9), dark = Color(0xFF2C3844))
                                    )
                                    .border(
                                        1.dp,
                                        if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable {
                                        name = preset.name
                                        periodKmStr = preset.defaultPeriodKm.toString()
                                        startKmStr = currentOdometerKm.roundToLong().toString()
                                        nameError = null
                                        periodError = null
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "${preset.name} (${preset.defaultPeriodKm.toString().toPersianDigits()} km)",
                                    fontSize = 12.sp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else UzradyabTheme.colors.textBody,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }

            // Name Field
            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    nameError = null
                },
                label = { Text("عنوان سرویس (مثلا تعویض روغن موتور)") },
                modifier = Modifier.fillMaxWidth(),
                isError = nameError != null,
                supportingText = nameError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = themedColor(light = Color(0xFFCBD5E1), dark = Color(0xFF475569))
                )
            )

            // Period Field
            OutlinedTextField(
                value = periodKmStr,
                onValueChange = { input ->
                    if (input.all(Char::isDigit)) {
                        periodKmStr = input
                        periodError = null
                    }
                },
                label = { Text("دوره تکرار به کیلومتر (مثلا 5000)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                textStyle = androidx.compose.ui.text.TextStyle(textDirection = TextDirection.Ltr),
                modifier = Modifier.fillMaxWidth(),
                isError = periodError != null,
                supportingText = periodError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = themedColor(light = Color(0xFFCBD5E1), dark = Color(0xFF475569))
                )
            )

            // Start Odometer Field
            OutlinedTextField(
                value = startKmStr,
                onValueChange = { input ->
                    if (input.all(Char::isDigit)) {
                        startKmStr = input
                    }
                },
                label = { Text("کیلومتر شروع / آخرین بار که تعویض شد") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                textStyle = androidx.compose.ui.text.TextStyle(textDirection = TextDirection.Ltr),
                modifier = Modifier.fillMaxWidth(),
                supportingText = {
                    Text(
                        text = "کیلومتر فعلی خودرو: ${formatNumberWithCommas(currentOdometerKm.roundToLong()).toPersianDigits()} کیلومتر",
                        color = UzradyabTheme.colors.textMuted,
                        fontSize = 11.sp
                    )
                },
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = themedColor(light = Color(0xFFCBD5E1), dark = Color(0xFF475569))
                )
            )

            // Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("انصراف", color = UzradyabTheme.colors.textBody)
                }

                Button(
                    onClick = {
                        val trimmedName = name.trim()
                        val period = periodKmStr.toDoubleOrNull()
                        val start = startKmStr.toDoubleOrNull() ?: currentOdometerKm

                        var hasError = false
                        if (trimmedName.isBlank()) {
                            nameError = "لطفا عنوان سرویس را وارد کنید"
                            hasError = true
                        }
                        if (period == null || period <= 0) {
                            periodError = "دوره سرویس باید بزرگتر از صفر باشد"
                            hasError = true
                        }

                        if (!hasError && period != null) {
                            onSave(trimmedName, period, start)
                        }
                    },
                    modifier = Modifier
                        .weight(1.5f)
                        .height(48.dp),
                    enabled = !isSubmitting,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Text(
                            text = if (editing != null) "ذخیره تغییرات" else "افزودن سرویس",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyMaintenanceState(
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Build,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(40.dp)
                )
            }

            Text(
                text = "هنوز سرویس دوره‌ای ثبت نشده است",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = themedColor(light = Color(0xFF1E293B), dark = Color.White)
            )

            Text(
                text = "با تعریف سرویس‌هایی مثل تعویض روغن، فیلتر و لنت، از موعد سرویس‌های خودرو خود به موقع مطلع شوید.",
                fontSize = 13.sp,
                color = UzradyabTheme.colors.textMuted,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onAddClick,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "افزودن اولین سرویس",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

private data class PresetService(
    val name: String,
    val defaultPeriodKm: Long
)

private fun getServiceIcon(name: String): ImageVector {
    return when {
        name.contains("روغن") -> Icons.Default.Opacity
        name.contains("فیلتر") -> Icons.Default.FilterVintage
        name.contains("هوا") -> Icons.Default.Air
        name.contains("لنت") || name.contains("ترمز") -> Icons.Default.Warning
        name.contains("تسمه") || name.contains("تایم") -> Icons.Default.Settings
        name.contains("لاستیک") -> Icons.Default.DirectionsCar
        else -> Icons.Default.Build
    }
}

private fun formatNumberWithCommas(number: Long): String {
    return NumberFormat.getNumberInstance(Locale.US).format(number)
}
