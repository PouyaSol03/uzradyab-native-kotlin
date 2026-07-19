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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.Image
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
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
import com.example.uzradyab.R
import androidx.compose.ui.res.stringResource
import com.example.uzradyab.ui.theme.themedColor

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
            title = stringResource(R.string.str_66697949),
            description = stringResource(R.string.str_b9c4b88d),
            pageType = OnboardingPageType.GPS
        ),
        OnboardingPageData(
            title = stringResource(R.string.str_af2f9933),
            description = stringResource(R.string.str_bf19d98c),
            pageType = OnboardingPageType.ALERTS
        ),
        OnboardingPageData(
            title = stringResource(R.string.str_a5ba150c),
            description = stringResource(R.string.str_f28033c8),
            pageType = OnboardingPageType.REPORTS
        )
    )

    val handleFinish = {
        viewModel.completeOnboarding()
        onOnboardingFinished()
    }

    // Force LTR layout direction for the onboarding flow to swipe left-to-right (like English apps)
    // and correctly place indicators on the left and the next button on the right.
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(themedColor(light = Color.White, dark = Color(0xFF27343F)))
        ) {
            // Horizontal Pager for onboarding screens (scrolling LTR)
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                OnboardingPageContent(
                    pageData = pages[page],
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Cancel (×) Circle Button at Top-Right
            IconButton(
                onClick = handleFinish,
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(top = 16.dp, end = 16.dp)
                    .size(40.dp)
                    .background(themedColor(light = Color(0xFFEFF3F5), dark = Color(0xFF182126)), shape = CircleShape)
                    .align(Alignment.TopEnd)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(R.string.str_fa882772),
                    tint = themedColor(light = Color(0xFF384C5C), dark = Color(0xFFA0B5C5)),
                    modifier = Modifier.size(20.dp)
                )
            }

            // Bottom Navigation Bar (indicators on the left + next button on the right)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 36.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Page Indicators (aligned to the left in LTR)
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
                                    .background(if (isActive) themedColor(light = Color(0xFF384C5C), dark = Color(0xFFA0B5C5)) else themedColor(light = Color(0xFF97ADBF), dark = Color(0xFF31414F)))
                            )
                        }
                    }

                    // Next / Enter Button (aligned to the right in LTR)
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
                            containerColor = themedColor(light = Color(0xFF307EF3), dark = Color(0xFF5F98EC)),
                            contentColor = themedColor(light = Color.White, dark = Color.White)
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

        // Visual Illustration box (takes up ~50% of the screen height)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.5f),
            contentAlignment = Alignment.Center
        ) {
            when (pageData.pageType) {
                OnboardingPageType.GPS -> {
                    Image(
                        painter = painterResource(id = com.example.uzradyab.R.drawable.ic_onboarding_illustration),
                        contentDescription = "Illustration",
                        modifier = Modifier.fillMaxSize()
                    )
                }
                OnboardingPageType.ALERTS -> {
                    Image(
                        painter = painterResource(id = com.example.uzradyab.R.drawable.ic_onboarding_illustration_2),
                        contentDescription = "Alerts Illustration",
                        modifier = Modifier.fillMaxSize()
                    )
                }
                OnboardingPageType.REPORTS -> {
                    Image(
                        painter = painterResource(id = com.example.uzradyab.R.drawable.ic_onboarding_illustration_3),
                        contentDescription = "Reports Illustration",
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Text Content
        Text(
            text = pageData.title,
            color = themedColor(light = Color(0xFF333638), dark = Color(0xFFAFB3B6)),
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = pageData.description,
            color = themedColor(light = Color(0xFF6A8BA5), dark = Color(0xFF99A7B3)),
            fontSize = 16.sp,
            fontWeight = FontWeight.Light,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        )

        Spacer(modifier = Modifier.weight(1f))
    }
}


