package com.example.uzradyab.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import com.example.uzradyab.presentation.components.LocalSnackbarController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.uzradyab.core.biometric.BiometricHelper

import com.example.uzradyab.presentation.map.AppTopToolbar
import com.example.uzradyab.presentation.map.BackButton
import com.example.uzradyab.presentation.map.MenuGridButton
import com.example.uzradyab.R
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import com.example.uzradyab.ui.theme.UzradyabTheme
import com.example.uzradyab.ui.theme.themedColor

@Composable
fun ProfileRoute(
    onLogoutClick: () -> Unit,
    onBackClick: () -> Unit,
    onMenuClick: () -> Unit,
) {
    val viewModel: ProfileViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.signedOut) {
        if (state.signedOut) {
            onLogoutClick()
        }
    }

    ProfileScreen(
        state = state,
        onLogoutClick = viewModel::logout,
        onSaveClick = viewModel::updateProfile,
        onResetSaveSuccess = viewModel::resetSaveSuccess,
        onClearError = viewModel::clearError,
        onBackClick = onBackClick,
        onMenuClick = onMenuClick
    )
}

@Composable
private fun profilePrimary(): Color = themedColor(light = Color(0xFF307EF3), dark = Color(0xFF5F98EC))

@Composable
private fun profileCardColor(): Color = themedColor(light = Color.White, dark = Color(0xFF27343F))

@Composable
private fun profileGreyText(): Color = themedColor(light = Color(0xFF676C70), dark = Color(0xFF929292))

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    state: ProfileUiState,
    onLogoutClick: () -> Unit,
    onSaveClick: (com.example.uzradyab.data.remote.dto.SessionDto) -> Unit,
    onResetSaveSuccess: () -> Unit,
    onClearError: () -> Unit,
    onBackClick: () -> Unit,
    onMenuClick: () -> Unit,
) {
    val context = LocalContext.current
    val snackbarController = LocalSnackbarController.current
    val biometricHelper = remember { BiometricHelper(context) }

    var name by remember(state.sessionDto) { mutableStateOf(state.sessionDto?.name ?: "") }
    var email by remember(state.sessionDto) { mutableStateOf(state.sessionDto?.email ?: "") }
    var password by remember { mutableStateOf("") }
    // Not shown in the UI; sent back unchanged so saving does not clear it.
    val phone = state.sessionDto?.phone ?: ""

    var isBiometricEnabled by remember { mutableStateOf(biometricHelper.isBiometricEnabled()) }

    LaunchedEffect(state.saveSuccess) {
        if (state.saveSuccess) {
            snackbarController.showSuccess("تغییرات با موفقیت ذخیره شد")
            onResetSaveSuccess()
        }
    }

    LaunchedEffect(state.error) {
        if (state.error != null) {
            snackbarController.showError(state.error)
            onClearError()
        }
    }

    val onSave = {
        state.sessionDto?.let { session ->
            val updatedSession = session.copy(
                name = name.takeIf { it.isNotBlank() },
                email = email.takeIf { it.isNotBlank() },
                phone = phone.takeIf { it.isNotBlank() },
                password = password.takeIf { it.isNotBlank() }
            )
            onSaveClick(updatedSession)
        }
        Unit
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            topBar = {
                AppTopToolbar(
                    startContent = {
                        BackButton(onClick = onBackClick)
                    },
                    centerContent = {
                        Text(
                            text = stringResource(R.string.str_a802ae5f),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = profileGreyText()
                        )
                    },
                    endContent = {
                        MenuGridButton(onClick = onMenuClick)
                    },
                    modifier = Modifier
                        .statusBarsPadding()
                        .height(64.dp)
                )
            },
            bottomBar = {
                if (!state.isLoading) {
                    SaveBar(isSaving = state.isSaving, onSave = onSave)
                }
            },
            containerColor = UzradyabTheme.colors.background,
        ) { innerPadding ->
            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = UzradyabTheme.colors.primary)
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    ProfileHeaderCard(name = state.sessionDto?.name.orEmpty(), mobile = state.sessionDto?.email.orEmpty())

                    SectionTitle(stringResource(R.string.profile_section_personal))
                    ProfileCard {
                        ProfileField(
                            label = stringResource(R.string.str_eaa6c1e8),
                            value = name,
                            onValueChange = { name = it },
                            icon = Icons.Outlined.PersonOutline,
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        ProfileField(
                            label = stringResource(R.string.profile_phone),
                            value = email,
                            onValueChange = { email = it },
                            icon = Icons.Outlined.PhoneAndroid,
                            placeholder = "09123456789",
                            ltr = true,
                            keyboardType = KeyboardType.Phone,
                        )
                    }

                    SectionTitle(stringResource(R.string.profile_section_security))
                    ProfileCard {
                        ProfileField(
                            label = stringResource(R.string.profile_new_password),
                            value = password,
                            onValueChange = { password = it },
                            icon = Icons.Outlined.Lock,
                            placeholder = stringResource(R.string.profile_password_hint),
                            ltr = true,
                            keyboardType = KeyboardType.Password,
                            isPassword = true,
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 14.dp),
                            color = themedColor(light = Color(0xFFE5E7EB), dark = Color(0xFF1B1D23)),
                        )
                        BiometricRow(
                            checked = isBiometricEnabled,
                            onCheckedChange = {
                                isBiometricEnabled = it
                                biometricHelper.setBiometricEnabled(it)
                            },
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    LogoutRow(onClick = onLogoutClick)
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun ProfileCard(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(profileCardColor())
            .border(1.dp, themedColor(light = Color(0xFFE5E7EB), dark = Color(0xFF1B1D23)), RoundedCornerShape(12.dp))
            .padding(14.dp),
        content = content,
    )
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        color = profileGreyText(),
        modifier = Modifier.padding(start = 4.dp, top = 16.dp, bottom = 8.dp),
    )
}

/** Avatar with initials, name and mobile number. */
@Composable
private fun ProfileHeaderCard(name: String, mobile: String) {
    val primary = profilePrimary()
    val initials = name.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }.take(2).joinToString("") { it.take(1) }
    ProfileCard {
        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(themedColor(light = Color(0xFFE8F1FE), dark = Color(0xFF1E3A5F))),
                contentAlignment = Alignment.Center,
            ) {
                if (initials.isNotEmpty()) {
                    Text(text = initials, fontSize = 22.sp, fontWeight = FontWeight.SemiBold, color = primary)
                } else {
                    Icon(imageVector = Icons.Filled.Person, contentDescription = null, tint = primary, modifier = Modifier.size(32.dp))
                }
            }
            if (name.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = themedColor(light = Color(0xFF333638), dark = Color(0xFFAFB3B6)),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (mobile.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    Text(text = mobile, fontSize = 13.sp, color = profileGreyText())
                }
            }
        }
    }
}

/** 48dp field with a leading icon; focused border uses the primary colour. */
@Composable
private fun ProfileField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    icon: ImageVector,
    placeholder: String = "",
    ltr: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false,
) {
    val primary = profilePrimary()
    val greyText = profileGreyText()
    val interactionSource = remember { MutableInteractionSource() }
    val focused by interactionSource.collectIsFocusedAsState()
    var passwordVisible by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(8.dp)

    Text(text = label, fontSize = 12.sp, color = if (focused) primary else greyText, fontWeight = if (focused) FontWeight.Medium else FontWeight.Normal)
    Spacer(modifier = Modifier.height(6.dp))
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        interactionSource = interactionSource,
        textStyle = TextStyle(
            fontSize = 14.sp,
            color = themedColor(light = Color(0xFF333638), dark = Color(0xFFAFB3B6)),
            textDirection = if (ltr) TextDirection.Ltr else TextDirection.Content,
        ),
        cursorBrush = SolidColor(primary),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(shape)
            .background(themedColor(light = Color.White, dark = Color(0xFF1A1D23)))
            .border(1.dp, if (focused) primary else themedColor(light = Color(0xFFD5D9DD), dark = Color(0xFF455A64)), shape),
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = if (focused) primary else greyText, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                    if (value.isEmpty() && placeholder.isNotEmpty()) {
                        Text(text = placeholder, fontSize = 13.sp, color = greyText, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    innerTextField()
                }
                if (isPassword) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = if (passwordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                        contentDescription = null,
                        tint = greyText,
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .clickable { passwordVisible = !passwordVisible },
                    )
                }
            }
        },
    )
}

@Composable
private fun BiometricRow(checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    val primary = profilePrimary()
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(themedColor(light = Color(0xFFE8F1FE), dark = Color(0xFF1E3A5F))),
            contentAlignment = Alignment.Center,
        ) {
            Icon(imageVector = Icons.Outlined.Fingerprint, contentDescription = null, tint = primary, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.str_2944a2c7),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = themedColor(light = Color(0xFF333638), dark = Color(0xFFAFB3B6)),
            )
            Text(text = stringResource(R.string.str_f57206ad), fontSize = 11.sp, color = profileGreyText())
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = primary,
            ),
        )
    }
}

@Composable
private fun LogoutRow(onClick: () -> Unit) {
    val danger = themedColor(light = Color(0xFFD93025), dark = Color(0xFFF28B82))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(profileCardColor())
            .border(1.dp, themedColor(light = Color(0xFFE5E7EB), dark = Color(0xFF1B1D23)), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(themedColor(light = Color(0xFFFDECEC), dark = Color(0xFF3A1F1F))),
            contentAlignment = Alignment.Center,
        ) {
            Icon(imageVector = Icons.Outlined.Logout, contentDescription = null, tint = danger, modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(text = stringResource(R.string.profile_logout), fontSize = 14.sp, fontWeight = FontWeight.Medium, color = danger, modifier = Modifier.weight(1f))
        Icon(imageVector = Icons.Outlined.ChevronLeft, contentDescription = null, tint = profileGreyText(), modifier = Modifier.size(20.dp))
    }
}

/** Pinned bottom bar with the single primary save action. */
@Composable
private fun SaveBar(isSaving: Boolean, onSave: () -> Unit) {
    val primary = profilePrimary()
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(profileCardColor())
            .navigationBarsPadding()
            .padding(16.dp),
    ) {
        Button(
            onClick = onSave,
            enabled = !isSaving,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = primary,
                contentColor = Color.White,
                disabledContainerColor = primary.copy(alpha = 0.5f),
                disabledContentColor = Color.White,
            ),
        ) {
            Text(
                text = stringResource(if (isSaving) R.string.profile_saving else R.string.profile_save),
                fontSize = 16.sp,
            )
        }
    }
}
