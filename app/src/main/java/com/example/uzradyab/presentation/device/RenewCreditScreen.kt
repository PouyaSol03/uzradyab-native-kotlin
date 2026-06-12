package com.example.uzradyab.presentation.device

import android.content.Intent
import android.net.Uri
import android.widget.Toast
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
import com.example.uzradyab.presentation.map.AppTopToolbar
import com.example.uzradyab.presentation.map.BackButton

@Composable
fun RenewCreditRoute(
    onBackClick: () -> Unit,
    viewModel: RenewCreditViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    LaunchedEffect(viewModel.errorMessage) {
        viewModel.errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(viewModel.paymentUrl) {
        viewModel.paymentUrl?.let { url ->
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
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
                            text = "تمدید اعتبار دستگاه",
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
                                containerColor = Color(0xFF307EF3),
                                contentColor = Color.White,
                                disabledContainerColor = Color(0xFF307EF3).copy(alpha = 0.5f),
                            ),
                        ) {
                            if (state.isProcessing) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
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
            containerColor = Color(0xFFF3F4F6)
        ) { paddingValues ->
            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF307EF3))
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
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = device.name,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.Black,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                            
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("شماره سریال دستگاه:", fontSize = 14.sp, color = Color.Gray)
                                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                    Text(device.uniqueId, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color.DarkGray)
                                }
                            }
                            
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("شماره سیمکارت دستگاه:", fontSize = 14.sp, color = Color.Gray)
                                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                    Text(device.phone ?: "—", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color.DarkGray)
                                }
                            }

                            if (state.accountCharges.isEmpty()) {
                                Text("هیچ بسته سرویسی در دسترس نیست", color = Color.Gray, modifier = Modifier.padding(vertical = 16.dp))
                            } else {
                                state.accountCharges.forEach { charge ->
                                    val isSelected = state.selectedPlanId == charge.id
                                    val borderColor = if (isSelected) Color(0xFF93C5FD) else Color(0xFFE5E7EB)
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
                                            colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF3B82F6))
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(charge.period, color = Color(0xFF60A5FA), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            if (!charge.description.isNullOrEmpty()) {
                                                Text(charge.description, color = Color.Gray, fontSize = 12.sp)
                                            }
                                        }
                                        Text(formatPrice(charge.amount), color = Color.DarkGray, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                    }
                                }
                            }
                        }
                    }

                    if (selectedPlan != null) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF384C5C), RoundedCornerShape(12.dp))
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("مبلغ شارژ اعتبار:", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.LightGray)
                                Text(formatPrice(selectedPlan.amount), fontSize = 14.sp, color = Color.LightGray)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("مبلغ قابل پرداخت:", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.LightGray)
                                Text(formatPrice(selectedPlan.amount), fontSize = 14.sp, color = Color.White)
                            }

                            // Gateway Selection
                            Text("درگاه‌های پرداخت:", fontSize = 14.sp, color = Color.LightGray, modifier = Modifier.padding(bottom = 8.dp))
                            
                            val isZarinpal = state.selectedGateway == "zarinpal"
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.White, RoundedCornerShape(8.dp))
                                    .border(if (isZarinpal) 2.dp else 1.dp, if (isZarinpal) Color(0xFF93C5FD) else Color.Transparent, RoundedCornerShape(8.dp))
                                    .clickable { onGatewaySelect("zarinpal") }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isZarinpal,
                                    onClick = { onGatewaySelect("zarinpal") },
                                    colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF3B82F6))
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("زرین‌پال", color = Color(0xFF3B82F6), fontWeight = FontWeight.Bold, fontSize = 14.sp)
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
