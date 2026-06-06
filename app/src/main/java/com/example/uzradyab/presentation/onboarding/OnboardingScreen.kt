package com.example.uzradyab.presentation.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onOnboardingFinished: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val pagerState = rememberPagerState(pageCount = { 3 })
    val coroutineScope = rememberCoroutineScope()

    val pages = listOf(
        OnboardingPageData(
            title = "موقعیت یابی دقیق",
            description = "با استفاده از فناوری GPS، می‌توان به صورت لحظه به لحظه از وضعیت خودرو مطلع بود.",
            pageType = OnboardingPageType.GPS
        ),
        OnboardingPageData(
            title = "کنترل رویدادها و هشدارها",
            description = "در صورت حرکت غیرمجاز یا خروج خودرو از محدوده تعریف شده، بلافاصله برای کاربر هشدار ارسال می‌شود.",
            pageType = OnboardingPageType.ALERTS
        ),
        OnboardingPageData(
            title = "گزارش‌های جامع",
            description = "گزارش‌های دقیقی از وضعیت خودرو در زمان‌های مختلف و همچنین جزئیات مسیرهای پیموده شده و امکان مرور مجدد آن‌ها.",
            pageType = OnboardingPageType.REPORTS
        )
    )

    val handleFinish = {
        viewModel.completeOnboarding()
        onOnboardingFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Horizontal Pager for onboarding screens
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            OnboardingPageContent(
                pageData = pages[page],
                modifier = Modifier.fillMaxSize()
            )
        }

        // Cancel (×) Circle Button at Top-Right (Start)
        IconButton(
            onClick = handleFinish,
            modifier = Modifier
                .statusBarsPadding()
                .padding(top = 16.dp, end = 16.dp)
                .size(40.dp)
                .background(Color(0xFFEFF3F5), shape = CircleShape)
                .align(Alignment.TopEnd)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "بستن",
                tint = Color(0xFF384C5C),
                modifier = Modifier.size(20.dp)
            )
        }

        // Bottom Navigation Bar (indicators + prev/next buttons)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Page Indicators (aligned to the left/start as in Figma layout)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(3) { index ->
                        val isActive = pagerState.currentPage == index
                        Box(
                            modifier = Modifier
                                .height(8.dp)
                                .width(if (isActive) 24.dp else 8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isActive) Color(0xFF384C5C) else Color(0xFF97ADBF))
                        )
                    }
                }

                // Prev / Next Action buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Previous Button (visible on page 2 & 3)
                    AnimatedVisibility(
                        visible = pagerState.currentPage > 0,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        OutlinedButton(
                            onClick = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage - 1)
                                }
                            },
                            shape = RoundedCornerShape(8.dp),
                            border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFF384C5C)
                            ),
                            modifier = Modifier.height(48.dp)
                        ) {
                            Text(
                                text = "قبلی",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Next / Enter Button
                    Button(
                        onClick = {
                            if (pagerState.currentPage < 2) {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                }
                            } else {
                                handleFinish()
                            }
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF307EF3),
                            contentColor = Color.White
                        ),
                        modifier = Modifier.height(48.dp)
                    ) {
                        Text(
                            text = if (pagerState.currentPage == 2) "ورود" else "بعدی",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

enum class OnboardingPageType {
    GPS, ALERTS, REPORTS
}

data class OnboardingPageData(
    val title: String,
    val description: String,
    val pageType: OnboardingPageType
)

@Composable
fun OnboardingPageContent(
    pageData: OnboardingPageData,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(0.15f))

        // Visual Illustration box
        Box(
            modifier = Modifier
                .size(280.dp)
                .clip(CircleShape)
                .background(Color(0xFFEFF3F5)),
            contentAlignment = Alignment.Center
        ) {
            // Draw custom premium vector illustration based on page type
            when (pageData.pageType) {
                OnboardingPageType.GPS -> GPSIllustration()
                OnboardingPageType.ALERTS -> AlertsIllustration()
                OnboardingPageType.REPORTS -> ReportsIllustration()
            }
        }

        Spacer(modifier = Modifier.weight(0.12f))

        // Text Content
        Text(
            text = pageData.title,
            color = Color(0xFF333638),
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = pageData.description,
            color = Color(0xFF6A8BA5),
            fontSize = 16.sp,
            fontWeight = FontWeight.Light,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        )

        Spacer(modifier = Modifier.weight(0.35f))
    }
}

@Composable
fun GPSIllustration() {
    Canvas(modifier = Modifier.size(160.dp)) {
        val centerOffset = Offset(size.width / 2, size.height / 2)
        
        // Draw concentric radar/pulsing circles (Figma primary light/blue colors)
        drawCircle(
            color = Color(0xFFCEE1FD),
            radius = 70.dp.toPx()
        )
        drawCircle(
            color = Color(0xFF6BA3F6),
            radius = 50.dp.toPx(),
            style = Stroke(width = 2.dp.toPx())
        )
        drawCircle(
            color = Color(0xFF307EF3),
            radius = 30.dp.toPx(),
            style = Stroke(width = 1.5.dp.toPx())
        )

        // Draw Location Pin
        val pinPath = Path().apply {
            val pinWidth = 28.dp.toPx()
            val pinHeight = 42.dp.toPx()
            val topCenter = Offset(centerOffset.x, centerOffset.y - pinHeight / 2)
            
            moveTo(topCenter.x, topCenter.y)
            cubicTo(
                topCenter.x + pinWidth / 2, topCenter.y,
                topCenter.x + pinWidth / 2, topCenter.y + pinHeight * 0.5f,
                topCenter.x, topCenter.y + pinHeight
            )
            cubicTo(
                topCenter.x - pinWidth / 2, topCenter.y + pinHeight * 0.5f,
                topCenter.x - pinWidth / 2, topCenter.y,
                topCenter.x, topCenter.y
            )
            close()
        }
        drawPath(
            path = pinPath,
            color = Color(0xFFA12887)
        )

        // Draw pin center hole
        drawCircle(
            color = Color.White,
            radius = 6.dp.toPx(),
            center = Offset(centerOffset.x, centerOffset.y - 8.dp.toPx())
        )
    }
}

@Composable
fun AlertsIllustration() {
    Canvas(modifier = Modifier.size(160.dp)) {
        val centerOffset = Offset(size.width / 2, size.height / 2)
        
        // Background decorative shapes
        drawCircle(
            color = Color(0xFFCEE1FD),
            radius = 70.dp.toPx()
        )
        
        // Ringing Shield/Bell concept (Figma secondary/accent colors)
        val shieldPath = Path().apply {
            val w = 50.dp.toPx()
            val h = 60.dp.toPx()
            moveTo(centerOffset.x, centerOffset.y - h / 2)
            quadraticBezierTo(
                centerOffset.x + w / 2, centerOffset.y - h / 2,
                centerOffset.x + w / 2, centerOffset.y
            )
            quadraticBezierTo(
                centerOffset.x + w / 2, centerOffset.y + h / 2,
                centerOffset.x, centerOffset.y + h / 2
            )
            quadraticBezierTo(
                centerOffset.x - w / 2, centerOffset.y + h / 2,
                centerOffset.x - w / 2, centerOffset.y
            )
            quadraticBezierTo(
                centerOffset.x - w / 2, centerOffset.y - h / 2,
                centerOffset.x, centerOffset.y - h / 2
            )
            close()
        }
        drawPath(
            path = shieldPath,
            color = Color(0xFF6BA3F6)
        )

        // Exclamation Mark inside shield
        val lineStroke = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round)
        drawLine(
            color = Color.White,
            start = Offset(centerOffset.x, centerOffset.y - 15.dp.toPx()),
            end = Offset(centerOffset.x, centerOffset.y + 5.dp.toPx()),
            strokeWidth = lineStroke.width,
            cap = lineStroke.cap
        )
        drawCircle(
            color = Color.White,
            radius = 3.5.dp.toPx(),
            center = Offset(centerOffset.x, centerOffset.y + 15.dp.toPx())
        )

        // Alarm waves on the sides
        val leftWave = Path().apply {
            moveTo(centerOffset.x - 40.dp.toPx(), centerOffset.y - 20.dp.toPx())
            quadraticBezierTo(
                centerOffset.x - 55.dp.toPx(), centerOffset.y,
                centerOffset.x - 40.dp.toPx(), centerOffset.y + 20.dp.toPx()
            )
        }
        val rightWave = Path().apply {
            moveTo(centerOffset.x + 40.dp.toPx(), centerOffset.y - 20.dp.toPx())
            quadraticBezierTo(
                centerOffset.x + 55.dp.toPx(), centerOffset.y,
                centerOffset.x + 40.dp.toPx(), centerOffset.y + 20.dp.toPx()
            )
        }
        
        drawPath(
            path = leftWave,
            color = Color(0xFFA12887),
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )
        drawPath(
            path = rightWave,
            color = Color(0xFFA12887),
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}

@Composable
fun ReportsIllustration() {
    Canvas(modifier = Modifier.size(160.dp)) {
        val centerOffset = Offset(size.width / 2, size.height / 2)
        
        // Concentric background
        drawCircle(
            color = Color(0xFFCEE1FD),
            radius = 70.dp.toPx()
        )

        // Document background
        val docW = 60.dp.toPx()
        val docH = 80.dp.toPx()
        drawRoundRect(
            color = Color.White,
            topLeft = Offset(centerOffset.x - docW / 2, centerOffset.y - docH / 2),
            size = Size(docW, docH),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx())
        )

        // Document border (purple/pink)
        drawRoundRect(
            color = Color(0xFFA12887),
            topLeft = Offset(centerOffset.x - docW / 2, centerOffset.y - docH / 2),
            size = Size(docW, docH),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx()),
            style = Stroke(width = 2.dp.toPx())
        )

        // Chart line inside document
        val chartPath = Path().apply {
            moveTo(centerOffset.x - 20.dp.toPx(), centerOffset.y + 20.dp.toPx())
            lineTo(centerOffset.x - 10.dp.toPx(), centerOffset.y)
            lineTo(centerOffset.x, centerOffset.y + 10.dp.toPx())
            lineTo(centerOffset.x + 10.dp.toPx(), centerOffset.y - 15.dp.toPx())
            lineTo(centerOffset.x + 20.dp.toPx(), centerOffset.y - 5.dp.toPx())
        }
        drawPath(
            path = chartPath,
            color = Color(0xFF307EF3),
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )

        // Highlight dots on the chart peaks
        drawCircle(
            color = Color(0xFF384C5C),
            radius = 3.5.dp.toPx(),
            center = Offset(centerOffset.x - 10.dp.toPx(), centerOffset.y)
        )
        drawCircle(
            color = Color(0xFF384C5C),
            radius = 3.5.dp.toPx(),
            center = Offset(centerOffset.x + 10.dp.toPx(), centerOffset.y - 15.dp.toPx())
        )
    }
}
