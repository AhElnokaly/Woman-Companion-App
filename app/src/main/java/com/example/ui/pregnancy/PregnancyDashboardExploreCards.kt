package com.example.ui.pregnancy

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.*
import com.example.viewmodel.WomanCompanionViewModel

/**
 * كاروسيل أفقي ذكي يجمع البطاقات الإرشادية والاستكشافية لتوفير المساحة البصرية
 * وتسهيل التصفح دون الحاجة لتمرير رأسي طويل وممل.
 */
@Composable
fun JouriExploreCardsCarousel(
    viewModel: WomanCompanionViewModel,
    onOpenJouriChat: () -> Unit,
    onNavigateToTab: (Int) -> Unit
) {
    // 4 بطاقات تفاعلية:
    // 0: نصائح جوري الذكية والطقس
    // 1: كاشف خرافات الحمل الشائعة
    // 2: البحث في الأغذية والسعرات المصرية
    // 3: تسجيل الوحام السريع
    val pagerState = rememberPagerState(pageCount = { 4 })

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("explore_cards_carousel"),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // شريط العناوين المصغرة والتحكم السريع
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "استكشاف جوري الذكي 💡",
                    fontWeight = FontWeight.Bold,
                    color = SoftTheme.TextWhite,
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = 15.sp
                )
                val badgeTitle = when (pagerState.currentPage) {
                    0 -> "نصائح اليوم 🌸"
                    1 -> "كاشف الخرافات 🔍"
                    2 -> "دليل التغذية 🥗"
                    else -> "سجل الوحام 🍓"
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(SoftTheme.PrimaryPink.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = badgeTitle,
                        color = SoftTheme.PrimaryPink,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // مؤشر الصفحات (Dots Indicator)
            Row(
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(4) { index ->
                    val isSelected = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .size(width = if (isSelected) 18.dp else 6.dp, height = 6.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) SoftTheme.PrimaryPink else SoftTheme.CardBorder.copy(alpha = 0.6f)
                            )
                    )
                }
            }
        }

        // الكاروسيل الأفقي
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth(),
            pageSpacing = 12.dp
        ) { page ->
            when (page) {
                0 -> {
                    JouriWellnessNotificationCard(
                        viewModel = viewModel,
                        onOpenJouriChat = onOpenJouriChat
                    )
                }
                1 -> {
                    PregnancyMythBusterCard(
                        viewModel = viewModel,
                        onNavigateToFoodSafety = { onNavigateToTab(2) },
                        onOpenJouriChat = onOpenJouriChat
                    )
                }
                2 -> {
                    EgyptianFoodSearchWidget(
                        viewModel = viewModel,
                        onNavigateToNutrition = { onNavigateToTab(2) }
                    )
                }
                3 -> {
                    QuickCravingLogCard(
                        viewModel = viewModel,
                        onNavigateToCraving = { onNavigateToTab(2) }
                    )
                }
            }
        }
    }
}
