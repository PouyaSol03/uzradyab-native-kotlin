package com.example.uzradyab.presentation.command

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.uzradyab.presentation.components.LocalSnackbarController
import com.example.uzradyab.presentation.map.AppTopToolbar
import com.example.uzradyab.presentation.map.BackButton
import com.example.uzradyab.R
import androidx.compose.ui.res.stringResource

private val COMMAND_TYPES = listOf(
    CommandData("status", "وضعیت دستگاه", "با ارسال این دستور دستگاه وضعیت کلی خود را پاسخ می‌دهد.", "STATUS#"),
    CommandData("admin", "تعریف مدیر", "تعریف شماره خاص جهت دستور به دستگاه.", "SOS,A,{EMAIL}#"),
    CommandData("start", "فعال کردن رله سوخت", "ارسال دستور وصل کردن سوخت خودرو در صورتی که سوخت از قبل توسط رله قطع شده باشد.", "RELAY,0#"),
    CommandData("stop", "غیر فعال کردن رله سوخت", "ارسال دستور قطع سوخت خودرو توسط رله در صورتی که سرعت خودرو کمتر از 10 کیلومتر باشد.", "RELAY,1#"),
    CommandData("reset", "راه اندازی مجدد", "با ارسال این دستور دستگاه خاموش و مجدد راه اندازی می شود.", "RESET#"),
    CommandData("server", "تنظیم سرور", "ارسال این دستور سرور دستگاه را تنظیم کرده و مناسب است برای زمانی که دستگاه تازه فعال می‌شود.", "SERVER,1,uzradyab.ir,5023,0#"),
    CommandData("time", "تنظیم زمان دستگاه", "ارسال این دستور زمان دستگاه را تنظیم کرده و مناسب است برای زمانی که دستگاه تازه فعال می‌شود.", "GMT,E,0,0#"),
    CommandData("lbs", "تنظیم \u202ALBS\u202C", "تنظیم \u202ALBS\u202C", "LBSON#")
)

data class CommandData(val type: String, val title: String, val description: String, val command: String)

@Composable
fun CommandCenterRoute(
    onBackClick: () -> Unit,
    viewModel: CommandCenterViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val snackbarController = LocalSnackbarController.current

    LaunchedEffect(viewModel.isSuccess) {
        if (viewModel.isSuccess) {
            snackbarController.showSuccess("دستور با موفقیت ارسال شد")
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

    CommandCenterScreen(
        isLoading = viewModel.isLoading,
        isSending = viewModel.isSending,
        phoneNumber = viewModel.phoneNumber,
        userEmail = viewModel.userEmail,
        onSendInternet = viewModel::sendCommandInternet,
        onBackClick = onBackClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommandCenterScreen(
    isLoading: Boolean,
    isSending: Boolean,
    phoneNumber: String,
    userEmail: String,
    onSendInternet: (String, String) -> Unit,
    onBackClick: () -> Unit,
) {
    val figmaBackground = Color(0xFFF3F4F6)
    var selectedCommand by remember { mutableStateOf(COMMAND_TYPES.first()) }
    var showModal by remember { mutableStateOf(false) }
    var commandMethod by remember { mutableStateOf("internet") }
    
    val context = LocalContext.current

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            topBar = {
                AppTopToolbar(
                    startContent = { BackButton(onClick = onBackClick) },
                    centerContent = {
                        Text(
                            text = stringResource(R.string.str_68fb631d),
                            color = Color(0xFF676C70),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                        )
                    },
                    modifier = Modifier
                        .statusBarsPadding()
                        .height(64.dp)
                )
            },
            bottomBar = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Button(
                        onClick = { showModal = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF307EF3),
                            contentColor = Color.White
                        )
                    ) {
                        Text(
                            text = stringResource(R.string.str_34df5c35),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }
            },
            containerColor = figmaBackground,
        ) { innerPadding ->
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF307EF3))
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF6A8BA5))
                            .padding(16.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.str_e0e2a491),
                            color = Color.White,
                            fontSize = 12.sp,
                            lineHeight = 22.sp,
                            textAlign = TextAlign.Right
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    COMMAND_TYPES.forEach { command ->
                        val isSelected = selectedCommand.type == command.type
                        val bgColor = if (isSelected) Color(0xFFF1F6FE) else Color.White
                        val borderColor = if (isSelected) Color(0xFF307EF3) else Color(0xFFE5E7EB)

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 10.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(bgColor)
                                .border(1.dp, borderColor, RoundedCornerShape(8.dp))
                                .clickable { selectedCommand = command }
                                .padding(16.dp)
                        ) {
                            Column {
                                Text(
                                    text = command.title,
                                    color = Color(0xFF307EF3),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = command.description,
                                    color = Color(0xFF676C70),
                                    fontSize = 12.sp,
                                    lineHeight = 22.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        if (showModal) {
            ModalBottomSheet(
                onDismissRequest = { showModal = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                containerColor = Color.White
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Text(
                        text = "ارسال دستور \"${selectedCommand.title}\"",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF333638),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.str_de7ae3ed),
                        fontSize = 14.sp,
                        color = Color(0xFF676C70),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Internet Method
                    MethodSelection(
                        method = "internet",
                        title = stringResource(R.string.str_a85af005),
                        description = stringResource(R.string.str_5ea43f41),
                        selectedMethod = commandMethod,
                        onMethodSelect = { commandMethod = it }
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // SMS Method
                    MethodSelection(
                        method = "sms",
                        title = stringResource(R.string.str_02b42328),
                        description = stringResource(R.string.str_5da6626b),
                        selectedMethod = commandMethod,
                        onMethodSelect = { commandMethod = it }
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Button(
                            onClick = {
                                val cmdString = selectedCommand.command.replace("{EMAIL}", userEmail)
                                if (commandMethod == "sms") {
                                    val uri = Uri.parse("smsto:$phoneNumber")
                                    val intent = Intent(Intent.ACTION_SENDTO, uri)
                                    intent.putExtra("sms_body", cmdString)
                                    context.startActivity(intent)
                                    showModal = false
                                } else {
                                    onSendInternet(selectedCommand.type, cmdString)
                                    showModal = false
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF307EF3))
                        ) {
                            if (isSending) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                            } else {
                                Text(stringResource(R.string.str_968581ce), fontSize = 14.sp)
                            }
                        }
                        
                        Button(
                            onClick = { showModal = false },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent,
                                contentColor = Color(0xFF333638)
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBEC1C3))
                        ) {
                            Text(stringResource(R.string.str_c8d2a1fb), fontSize = 14.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun MethodSelection(
    method: String,
    title: String,
    description: String,
    selectedMethod: String,
    onMethodSelect: (String) -> Unit
) {
    val isSelected = selectedMethod == method
    val borderColor = if (isSelected) Color(0xFF307EF3) else Color(0xFFE5E7EB)
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable { onMethodSelect(method) }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = { onMethodSelect(method) },
            colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF307EF3))
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(text = title, color = Color(0xFF307EF3), fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = description, color = Color(0xFF676C70), fontSize = 12.sp, lineHeight = 20.sp)
        }
    }
}
