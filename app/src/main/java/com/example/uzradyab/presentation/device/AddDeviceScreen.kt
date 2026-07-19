package com.example.uzradyab.presentation.device

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

import com.example.uzradyab.presentation.components.LocalSnackbarController
import com.example.uzradyab.presentation.map.AppMenuDialog
import com.example.uzradyab.presentation.map.AppTopToolbar
import com.example.uzradyab.presentation.map.BackButton
import com.example.uzradyab.presentation.map.MenuGridButton
import com.example.uzradyab.R
import androidx.compose.ui.res.stringResource
import com.example.uzradyab.ui.theme.themedColor

@Composable
fun AddDeviceRoute(
    onBackClick: () -> Unit,
    onSignedOut: () -> Unit,
    viewModel: AddDeviceViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val snackbarController = LocalSnackbarController.current

    LaunchedEffect(viewModel.isSuccess) {
        if (viewModel.isSuccess) {
            val message = if (viewModel.isEditMode) "دستگاه با موفقیت ویرایش شد" else "دستگاه با موفقیت ثبت شد"
            snackbarController.showSuccess(message)
            viewModel.clearMessages()
            onBackClick()
        }
    }

    LaunchedEffect(viewModel.errorMessage) {
        viewModel.errorMessage?.let { error ->
            snackbarController.showError(error)
            viewModel.clearMessages()
        }
    }

    LaunchedEffect(viewModel.signedOut) {
        if (viewModel.signedOut) {
            onSignedOut()
        }
    }

    AddDeviceScreen(
        name = viewModel.name,
        onNameChange = viewModel::onNameChange,
        uniqueId = viewModel.uniqueId,
        onUniqueIdChange = viewModel::onUniqueIdChange,
        phone = viewModel.phone,
        onPhoneChange = viewModel::onPhoneChange,
        currentKilometers = viewModel.currentKilometers,
        onCurrentKilometersChange = viewModel::onCurrentKilometersChange,
        isLoading = viewModel.isLoading,
        isFormValid = viewModel.isFormValid,
        onSaveClick = viewModel::saveDevice,
        onBackClick = onBackClick,
        onLogoutClick = viewModel::logout,
        isEditMode = viewModel.isEditMode,
        isReadOnly = viewModel.isReadOnly,
        creditText = viewModel.creditText,
        endCreditText = viewModel.endCreditText
    )
}

@Composable
fun AddDeviceScreen(
    name: String,
    onNameChange: (String) -> Unit,
    uniqueId: String,
    onUniqueIdChange: (String) -> Unit,
    phone: String,
    onPhoneChange: (String) -> Unit,
    currentKilometers: String,
    onCurrentKilometersChange: (String) -> Unit,
    isLoading: Boolean,
    isFormValid: Boolean,
    onSaveClick: () -> Unit,
    onBackClick: () -> Unit,
    onLogoutClick: () -> Unit,
    isEditMode: Boolean,
    isReadOnly: Boolean,
    creditText: String,
    endCreditText: String,
) {
    val figmaBackground = themedColor(light = Color(0xFFF3F4F6), dark = Color(0xFF1A1D23))
    var menuOpen by remember { mutableStateOf(false) }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            topBar = {
                AppTopToolbar(
                    startContent = { BackButton(onClick = onBackClick) },
                    centerContent = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = if (isReadOnly) "مشخصات دستگاه" else if (isEditMode) "تنظیمات دستگاه" else "افزودن دستگاه",
                                color = themedColor(light = Color(0xFF676C70), dark = Color(0xFF929292)),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                            )
                            Icon(
                                imageVector = Icons.Default.DirectionsCar,
                                contentDescription = null,
                                tint = themedColor(light = Color(0xFF676C70), dark = Color(0xFF929292)),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    },
                    endContent = { MenuGridButton(onClick = { menuOpen = true }) },
                    modifier = Modifier
                        .statusBarsPadding()
                        .height(64.dp)
                )
            },
            bottomBar = {
                if (!isReadOnly) {
                    AddDeviceBottomBar(
                        isLoading = isLoading,
                        isFormValid = isFormValid,
                        isEditMode = isEditMode,
                        onSaveClick = onSaveClick
                    )
                }
            },
            containerColor = figmaBackground,
        ) { innerPadding ->
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // Input 1: عنوان دستگاه
                    DeviceTextField(
                        value = name,
                        onValueChange = onNameChange,
                        label = stringResource(R.string.str_e14b74e4),
                        placeholder = "مثلا: پژو پارس",
                        helperText = "عنوان دستگاه برای شناسایی راحت‌تر آن در میان بقیه دستگاه‌های ثبت شده است.",
                        forceLtr = false,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                        enabled = !isReadOnly
                    )

                    // Input 2: شماره سریال دستگاه
                    DeviceTextField(
                        value = uniqueId,
                        onValueChange = onUniqueIdChange,
                        label = stringResource(R.string.str_bbf521c7),
                        placeholder = "مثلا: 123456789",
                        helperText = "شماره سریال دستگاه با عنوان \u202AIMEI\u202C بر روی جعبه دستگاه ردیاب درج شده است.",
                        forceLtr = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        enabled = !isEditMode && !isReadOnly
                    )

                    // Exir credit banner under uniqueId in Edit mode
                    if (isEditMode && endCreditText.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                                .background(themedColor(light = Color(0xFF384C5C), dark = Color(0xFFA0B5C5)), RoundedCornerShape(8.dp))
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = endCreditText,
                                color = themedColor(light = Color(0xFFC0CDD8), dark = Color(0xFF31414F)),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Normal,
                            )
                            Text(
                                text = creditText,
                                color = themedColor(light = Color.White, dark = Color.White),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Normal,
                            )
                        }
                    }

                    // Input 3: شماره سیمکارت دستگاه
                    DeviceTextField(
                        value = phone,
                        onValueChange = onPhoneChange,
                        label = stringResource(R.string.str_575acc6e),
                        placeholder = "مثلا: 09151094755",
                        helperText = "شماره سیم‌کارت موجود در دستگاه.",
                        forceLtr = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        enabled = !isReadOnly
                    )

                    // Input 4: کیلومتر فعلی دستگاه
                    DeviceTextField(
                        value = currentKilometers,
                        onValueChange = onCurrentKilometersChange,
                        label = stringResource(R.string.str_5fc7f3bc),
                        placeholder = "مثلا: 100",
                        helperText = "اختیاری",
                        forceLtr = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        enabled = !isReadOnly
                    )
                }

                if (menuOpen) {
                    AppMenuDialog(
                        onDismiss = { menuOpen = false },
                        onLogoutClick = onLogoutClick,
                        onAddDeviceClick = { menuOpen = false }
                    )
                }
            }
        }
    }
}

@Composable
private fun AddDeviceBottomBar(
    isLoading: Boolean,
    isFormValid: Boolean,
    isEditMode: Boolean,
    onSaveClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Button(
            onClick = onSaveClick,
            enabled = isFormValid && !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = themedColor(light = Color(0xFF307EF3), dark = Color(0xFF5F98EC)),
                contentColor = themedColor(light = Color.White, dark = Color.White),
                disabledContainerColor = themedColor(light = Color(0xFF307EF3), dark = Color(0xFF5F98EC)).copy(alpha = 0.5f),
                disabledContentColor = themedColor(light = Color.White, dark = Color(0xFF27343F)).copy(alpha = 0.8f)
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = themedColor(light = Color.White, dark = Color.White),
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (isEditMode) "ذخیره تغییرات" else "ذخیــــــره دستگاه",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }
    }
}

@Composable
private fun DeviceTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    helperText: String,
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    forceLtr: Boolean = false,
    enabled: Boolean = true,
) {
    var isFocused by remember { mutableStateOf(false) }
    
    val bgColor = if (enabled) themedColor(light = Color.White, dark = Color(0xFF27343F)) else themedColor(light = Color(0xFFE9ECEF), dark = Color(0xFF1A1F23))
    val borderColor = if (!enabled) themedColor(light = Color(0xFFBEC1C3), dark = Color(0xFF3D4042)) else if (isFocused) themedColor(light = Color(0xFF307EF3), dark = Color(0xFF5F98EC)) else themedColor(light = Color(0xFFAEB1B4), dark = Color(0xFF3D4042))
    val textColor = if (enabled) themedColor(light = Color(0xFF333638), dark = Color(0xFFAFB3B6)) else themedColor(light = Color(0xFF676C70), dark = Color(0xFF929292))

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.End
    ) {
        Text(
            text = label,
            color = themedColor(light = Color(0xFF333638), dark = Color(0xFFAFB3B6)),
            fontSize = 14.sp,
            lineHeight = 22.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Right,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(8.dp))

        val alignment = TextAlign.Right
        val textDirection = if (forceLtr) TextDirection.Ltr else TextDirection.Rtl

        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                enabled = enabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .background(bgColor, RoundedCornerShape(8.dp))
                    .onFocusChanged { isFocused = it.isFocused },
                singleLine = true,
                keyboardOptions = keyboardOptions,
                textStyle = TextStyle(
                    color = textColor,
                    fontSize = 14.sp,
                    lineHeight = 22.sp,
                    textDirection = textDirection,
                    textAlign = alignment,
                ),
                decorationBox = { innerTextField ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End
                    ) {
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            if (value.isEmpty()) {
                                Text(
                                    text = placeholder,
                                    color = themedColor(light = Color(0xFFBEC1C3), dark = Color(0xFF3D4042)),
                                    fontSize = 14.sp,
                                    lineHeight = 22.sp,
                                    textAlign = TextAlign.Right,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            innerTextField()
                        }
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = themedColor(light = Color(0xFF676C70), dark = Color(0xFF929292)),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = helperText,
                color = themedColor(light = Color(0xFF676C70), dark = Color(0xFF929292)),
                fontSize = 12.sp,
                lineHeight = 22.sp,
                textAlign = TextAlign.Right,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
