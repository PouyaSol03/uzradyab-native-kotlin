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
import com.example.uzradyab.ui.theme.UzradyabTheme
import com.example.uzradyab.ui.theme.themedColor

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
                            color = UzradyabTheme.colors.textPrimary
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
            containerColor = themedColor(light = Color(0xFFF0F4F8), dark = Color(0xFF131F2A)), // Premium light background
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(themedColor(light = Color(0xFFF0F4F8), dark = Color(0xFF131F2A)), themedColor(light = Color(0xFFE2E8F0), dark = Color(0xFF151D28)))
                        )
                    )
                    .navigationBarsPadding(),
                contentAlignment = Alignment.Center
            ) {
                // Main Premium Card
                Column(
                    modifier = Modifier
                        .padding(top = 24.dp, start = 20.dp, end = 20.dp, bottom = 32.dp)
                        .fillMaxSize()
                        .shadow(
                            elevation = 24.dp, 
                            shape = RoundedCornerShape(28.dp), 
                            spotColor = UzradyabTheme.colors.primary.copy(alpha = 0.15f),
                            ambientColor = UzradyabTheme.colors.primary.copy(alpha = 0.05f)
                        )
                        .clip(RoundedCornerShape(28.dp))
                        .background(themedColor(light = Color.White, dark = Color(0xFF27343F)))
                ) {
                    // Header with BIG logo
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f) // Takes available space, making it responsive
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(themedColor(light = Color(0xFFF8FAFC), dark = Color(0xFF121F2B)), themedColor(light = Color(0xFFEDF2F7), dark = Color(0xFF131F2A)))
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
                            .padding(horizontal = 28.dp, vertical = 24.dp), // Reduced vertical padding
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "کاربر گرامی، در صورت نیاز، می‌توانید جهت برقراری ارتباط با پشتیبانی یوزردیاب، با شمارهٔ زیر:",
                            fontSize = 15.sp,
                            lineHeight = 28.sp,
                            fontWeight = FontWeight.Medium,
                            color = themedColor(light = Color(0xFF334155), dark = Color(0xFF9FAFC6)),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Box(
                            modifier = Modifier
                                .background(themedColor(light = Color(0xFFFEF2F2), dark = Color(0xFF390404)), RoundedCornerShape(16.dp))
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
                                color = themedColor(light = Color(0xFFDC2626), dark = Color(0xFFDD6E6E)),
                                textAlign = TextAlign.Center
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "تماس حاصل فرمایید.",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = themedColor(light = Color(0xFF334155), dark = Color(0xFF9FAFC6)),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Box(
                            modifier = Modifier
                                .background(themedColor(light = Color(0xFFF0F9FF), dark = Color(0xFF00253D)), RoundedCornerShape(16.dp))
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
                                color = UzradyabTheme.colors.primary,
                                textAlign = TextAlign.Center
                            )
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        // Primary Action Button
                        Button(
                            onClick = onBackClick,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .shadow(8.dp, RoundedCornerShape(16.dp), spotColor = UzradyabTheme.colors.primary.copy(alpha = 0.5f)),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = UzradyabTheme.colors.primary
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                text = "بازگشت به صفحه اصلی",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = themedColor(light = Color.White, dark = Color.White)
                            )
                        }
                    }
                }
            }
        }
    }
}
