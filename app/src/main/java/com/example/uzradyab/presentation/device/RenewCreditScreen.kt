package com.example.uzradyab.presentation.device

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.uzradyab.R
import com.example.uzradyab.presentation.components.LocalSnackbarController
import com.example.uzradyab.presentation.map.AppTopToolbar
import com.example.uzradyab.presentation.map.BackButton
import androidx.compose.ui.res.stringResource
import com.example.uzradyab.ui.theme.themedColor

@Composable
fun RenewCreditRoute(
    onBackClick: () -> Unit,
    viewModel: RenewCreditViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val snackbarController = LocalSnackbarController.current

    LaunchedEffect(viewModel.errorMessage) {
        viewModel.errorMessage?.let {
            snackbarController.showError(it)
            viewModel.clearError()
        }
    }

    LaunchedEffect(viewModel.paymentUrl) {
        viewModel.paymentUrl?.let { url ->
            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url))
            context.startActivity(intent)
            viewModel.onPaymentUrlHandled()
        }
    }

    RenewCreditScreen(
        state = viewModel,
        onBackClick = onBackClick,
        onPayClick = viewModel::pay,
        onPlanSelect = viewModel::onPlanSelected,
        onGatewaySelect = viewModel::onGatewaySelected
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RenewCreditScreen(
    state: RenewCreditViewModel,
    onBackClick: () -> Unit,
    onPayClick: () -> Unit,
    onPlanSelect: (Int) -> Unit,
    onGatewaySelect: (String) -> Unit
) {
    val device = state.device
    val selectedPlan = state.getSelectedPlan()

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            topBar = {
                AppTopToolbar(
                    startContent = { BackButton(onClick = onBackClick) },
                    centerContent = {
                        Text(
                            text = stringResource(R.string.str_96ad5971),
                            color = themedColor(light = Color(0xFF676C70), dark = Color(0xFF929292)),
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
                if (selectedPlan != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Button(
                            onClick = onPayClick,
                            enabled = !state.isProcessing,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = themedColor(light = Color(0xFF307EF3), dark = Color(0xFF5F98EC)),
                                contentColor = themedColor(light = Color.White, dark = Color.White),
                                disabledContainerColor = themedColor(light = Color(0xFF307EF3), dark = Color(0xFF5F98EC)).copy(alpha = 0.5f),
                            ),
                        ) {
                            if (state.isProcessing) {
                                CircularProgressIndicator(color = themedColor(light = Color.White, dark = Color.White), modifier = Modifier.size(24.dp))
                            } else {
                                val amountFormatted = formatPrice(selectedPlan.amount)
                                Text(
                                    text = "پرداخت - ${selectedPlan.period} $amountFormatted",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            },
            containerColor = themedColor(light = Color(0xFFF3F4F6), dark = Color(0xFF1A1D23))
        ) { paddingValues ->
            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = themedColor(light = Color(0xFF307EF3), dark = Color(0xFF5F98EC)))
                }
            } else if (device != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Device Info Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = themedColor(light = Color.White, dark = Color(0xFF27343F))),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = device.name,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = themedColor(light = Color.Black, dark = Color(0xFFE0E0E0)),
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                            
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(stringResource(R.string.str_fa837aa3), fontSize = 14.sp, color = themedColor(light = Color.Gray, dark = Color(0xFFA0A0A0)))
                                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                    Text(device.uniqueId, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = themedColor(light = Color.DarkGray, dark = Color(0xFFB0B0B0)))
                                }
                            }
                            
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(stringResource(R.string.str_be9638ce), fontSize = 14.sp, color = themedColor(light = Color.Gray, dark = Color(0xFFA0A0A0)))
                                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                    Text(device.phone ?: "—", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = themedColor(light = Color.DarkGray, dark = Color(0xFFB0B0B0)))
                                }
                            }

                            if (state.accountCharges.isEmpty()) {
                                Text(stringResource(R.string.str_de13da6c), color = themedColor(light = Color.Gray, dark = Color(0xFFA0A0A0)), modifier = Modifier.padding(vertical = 16.dp))
                            } else {
                                state.accountCharges.forEach { charge ->
                                    val isSelected = state.selectedPlanId == charge.id
                                    val borderColor = if (isSelected) themedColor(light = Color(0xFF93C5FD), dark = Color(0xFF023C7D)) else themedColor(light = Color(0xFFE5E7EB), dark = Color(0xFF1B1D23))
                                    val borderWidth = if (isSelected) 2.dp else 1.dp

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 6.dp)
                                            .border(borderWidth, borderColor, RoundedCornerShape(8.dp))
                                            .clickable { onPlanSelect(charge.id) }
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(
                                            selected = isSelected,
                                            onClick = { onPlanSelect(charge.id) },
                                            colors = RadioButtonDefaults.colors(selectedColor = themedColor(light = Color(0xFF3B82F6), dark = Color(0xFF5D94EE)))
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(charge.period, color = themedColor(light = Color(0xFF60A5FA), dark = Color(0xFF043A7C)), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            if (!charge.description.isNullOrEmpty()) {
                                                Text(charge.description, color = themedColor(light = Color.Gray, dark = Color(0xFFA0A0A0)), fontSize = 12.sp)
                                            }
                                        }
                                        Text(formatPrice(charge.amount), color = themedColor(light = Color.DarkGray, dark = Color(0xFFB0B0B0)), fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                    }
                                }
                            }
                        }
                    }

                    if (selectedPlan != null) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(themedColor(light = Color(0xFF384C5C), dark = Color(0xFFA0B5C5)), RoundedCornerShape(12.dp))
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(stringResource(R.string.str_e4276c68), fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = themedColor(light = Color.LightGray, dark = Color(0xFF303030)))
                                Text(formatPrice(selectedPlan.amount), fontSize = 14.sp, color = themedColor(light = Color.LightGray, dark = Color(0xFF303030)))
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(stringResource(R.string.str_ee6d1b92), fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = themedColor(light = Color.LightGray, dark = Color(0xFF303030)))
                                Text(formatPrice(selectedPlan.amount), fontSize = 14.sp, color = themedColor(light = Color.White, dark = Color.White))
                            }

                            // Gateway Selection
                            Text(stringResource(R.string.str_0e1684cc), fontSize = 14.sp, color = themedColor(light = Color.LightGray, dark = Color(0xFF303030)), modifier = Modifier.padding(bottom = 8.dp))
                            
                            val isZarinpal = state.selectedGateway == "zarinpal"
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(themedColor(light = Color.White, dark = Color(0xFF27343F)), RoundedCornerShape(8.dp))
                                    .border(if (isZarinpal) 2.dp else 1.dp, if (isZarinpal) themedColor(light = Color(0xFF93C5FD), dark = Color(0xFF023C7D)) else Color.Transparent, RoundedCornerShape(8.dp))
                                    .clickable { onGatewaySelect("zarinpal") }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isZarinpal,
                                    onClick = { onGatewaySelect("zarinpal") },
                                    colors = RadioButtonDefaults.colors(selectedColor = themedColor(light = Color(0xFF3B82F6), dark = Color(0xFF5D94EE)))
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(stringResource(R.string.str_fe09ff34), color = themedColor(light = Color(0xFF3B82F6), dark = Color(0xFF5D94EE)), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatPrice(amount: String): String {
    val number = amount.toLongOrNull() ?: 0L
    return "%,d ریال".format(number)
}
