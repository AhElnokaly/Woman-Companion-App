package com.example.ui.dashboard

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocalCafe
import androidx.compose.material.icons.outlined.Thermostat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.SoftTheme

/**
 * شريط التنقل السفلي المعتمد ذو التابات الـ 5 مع التحول الديناميكي
 */
@Composable
fun CurvedFloatingBottomBar(
    currentPage: Int,
    isPregnant: Boolean = false,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Surface(
            shape = RoundedCornerShape(26.dp),
            color = SoftTheme.CardBg,
            tonalElevation = 6.dp,
            shadowElevation = 8.dp,
            border = androidx.compose.foundation.BorderStroke(1.dp, SoftTheme.CardBorder),
            modifier = Modifier
                .fillMaxWidth()
                .height(66.dp)
                .testTag("floating_bottom_bar")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Tab 0: الرئيسية
                FloatingBottomBarItem(
                    title = "الرئيسية",
                    selectedIcon = Icons.Filled.Home,
                    unselectedIcon = Icons.Outlined.Home,
                    isSelected = currentPage == 0,
                    onClick = { onTabSelected(0) },
                    modifier = Modifier.weight(1f)
                )

                // Tab 1: الدورة / الحمل (ديناميكي)
                FloatingBottomBarItem(
                    title = if (isPregnant) "الحمل" else "الدورة",
                    selectedIcon = if (isPregnant) Icons.Filled.Favorite else Icons.Filled.CalendarMonth,
                    unselectedIcon = if (isPregnant) Icons.Outlined.FavoriteBorder else Icons.Outlined.CalendarMonth,
                    isSelected = currentPage == 1,
                    onClick = { onTabSelected(1) },
                    modifier = Modifier.weight(1f)
                )

                // Tab 2: الغذاء
                FloatingBottomBarItem(
                    title = "الغذاء",
                    selectedIcon = Icons.Filled.LocalCafe,
                    unselectedIcon = Icons.Outlined.LocalCafe,
                    isSelected = currentPage == 2,
                    onClick = { onTabSelected(2) },
                    modifier = Modifier.weight(1f)
                )

                // Tab 3: الأعراض
                FloatingBottomBarItem(
                    title = "الأعراض",
                    selectedIcon = Icons.Filled.Thermostat,
                    unselectedIcon = Icons.Outlined.Thermostat,
                    isSelected = currentPage == 3,
                    onClick = { onTabSelected(3) },
                    modifier = Modifier.weight(1f)
                )

                // Tab 4: الأدوات
                FloatingBottomBarItem(
                    title = "الأدوات",
                    selectedIcon = Icons.Filled.Dashboard,
                    unselectedIcon = Icons.Outlined.Dashboard,
                    isSelected = currentPage == 4,
                    onClick = { onTabSelected(4) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun FloatingBottomBarItem(
    title: String,
    selectedIcon: ImageVector,
    unselectedIcon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val iconColor by animateColorAsState(
        targetValue = if (isSelected) SoftTheme.EmeraldPrimary else SoftTheme.TextSecondaryMuted,
        label = "iconColor"
    )

    Column(
        modifier = modifier
            .fillMaxHeight()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = if (isSelected) selectedIcon else unselectedIcon,
            contentDescription = title,
            tint = iconColor,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = iconColor
            )
        )
    }
}
