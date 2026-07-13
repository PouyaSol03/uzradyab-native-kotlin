package com.example.uzradyab.presentation.support

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.uzradyab.R
import com.example.uzradyab.presentation.map.AppTopToolbar
import com.example.uzradyab.presentation.map.BackButton
import com.example.uzradyab.presentation.map.MenuGridButton
import com.example.uzradyab.ui.theme.AppBlue
import com.example.uzradyab.ui.theme.AppTextPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactSupportScreen(
    onBackClick: () -> Unit,
    onMenuClick: () -> Unit
) {
    val context = LocalContext.current

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            topBar = {
                AppTopToolbar(
                    startContent = {
                        BackButton(onClick = onBackClick)
                    },
                    centerContent = {
                        Text(
                            text = "تماس با ما",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = AppTextPrimary
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
            containerColor = Color(0xFFF0F4F8), // Premium light background
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFFF0F4F8), Color(0xFFE2E8F0))
                        )
                    )
                    .verticalScroll(rememberScrollState()),
                contentAlignment = Alignment.TopCenter
            ) {
                // Main Premium Card
                Column(
                    modifier = Modifier
                        .padding(top = 24.dp, start = 20.dp, end = 20.dp, bottom = 32.dp)
                        .fillMaxWidth()
                        .shadow(
                            elevation = 24.dp, 
                            shape = RoundedCornerShape(28.dp), 
                            spotColor = AppBlue.copy(alpha = 0.15f),
                            ambientColor = AppBlue.copy(alpha = 0.05f)
                        )
                        .clip(RoundedCornerShape(28.dp))
                        .background(Color.White)
                ) {
                    // Header with BIG logo
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp) // Taller header for the large logo
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(Color(0xFFF8FAFC), Color(0xFFEDF2F7))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.exir_final_logo_blue),
                            contentDescription = "Exir Logo",
                            modifier = Modifier
                                .fillMaxSize(0.7f)
                                .padding(16.dp)
                        )
                    }

                    // Content Area
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 28.dp, vertical = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "کاربر گرامی، در صورت نیاز، می‌توانید جهت برقراری ارتباط با پشتیبانی یوزردیاب، با شمارهٔ زیر:",
                            fontSize = 15.sp,
                            lineHeight = 28.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF334155),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Box(
                            modifier = Modifier
                                .background(Color(0xFFFEF2F2), RoundedCornerShape(16.dp))
                                .padding(horizontal = 24.dp, vertical = 12.dp)
                                .clickable {
                                    val intent = Intent(Intent.ACTION_DIAL).apply {
                                        data = Uri.parse("tel:05191001340")
                                    }
                                    context.startActivity(intent)
                                }
                        ) {
                            Text(
                                text = "۰۵۱۹۱۰۰۱۳۴۰",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFFDC2626),
                                textAlign = TextAlign.Center
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = "تماس حاصل فرمایید.",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF334155),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        Spacer(modifier = Modifier.height(32.dp))
                        
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFF0F9FF), RoundedCornerShape(16.dp))
                                .padding(horizontal = 20.dp, vertical = 10.dp)
                                .clickable {
                                    val intent = Intent(Intent.ACTION_VIEW).apply {
                                        data = Uri.parse("https://www.exirfirm.com")
                                    }
                                    context.startActivity(intent)
                                }
                        ) {
                            Text(
                                text = "www.exirfirm.com",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = AppBlue,
                                textAlign = TextAlign.Center
                            )
                        }

                        Spacer(modifier = Modifier.height(40.dp))

                        // Primary Action Button
                        Button(
                            onClick = onBackClick,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .shadow(8.dp, RoundedCornerShape(16.dp), spotColor = AppBlue.copy(alpha = 0.5f)),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AppBlue
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                text = "بازگشت به صفحه اصلی",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}
