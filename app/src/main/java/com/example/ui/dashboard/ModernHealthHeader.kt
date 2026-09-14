package com.example.ui.dashboard

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.SoftTheme

/**
 * الترويسة العلوية الأنيقة والعصرية (Modern Health Top App Bar)
 * مطابقة بدقة للصورة المرفقة:
 * - على اليمين (RTL Start): صورة الأفاتار الدائرية + مرحباً أحمد 🌿 + كيف صحتك اليوم؟
 * - على اليسار (RTL End): شعار LifeCompanion Health + أيقونة الإشعارات الدائرية بنقطة حمراء
 */
@Composable
fun ModernHealthHeader(
    userName: String?,
    onAvatarClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    onToggleTheme: () -> Unit = {},
    hasUnreadNotifications: Boolean = true,
    modifier: Modifier = Modifier
) {
    val displayName = if (!userName.isNullOrBlank()) userName else "جميلة"

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // --- Start Side (Right in Arabic RTL): User Avatar & Greeting ---
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .border(2.dp, SoftTheme.MintAccentBorder, CircleShape)
                    .clickable(onClick = onAvatarClick)
                    .testTag("header_avatar_image")
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_user_avatar),
                    contentDescription = "صورة المستخدم",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "مرحباً $displayName",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = SoftTheme.TextPrimary
                        )
                    )
                    Text(text = "🌿", fontSize = 14.sp)
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "كيف صحتك اليوم؟",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = SoftTheme.TextSecondaryMuted
                    )
                )
            }
        }

        // --- End Side (Left in Arabic RTL): LifeCompanion Brand & Notification Icon ---
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Column(horizontalAlignment = Alignment.End) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text = "LifeCompanion",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = SoftTheme.EmeraldPrimary
                        )
                    )
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        tint = SoftTheme.EmeraldPrimary,
                        modifier = Modifier.size(13.dp)
                    )
                }
                Text(
                    text = "Health",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 10.sp,
                        color = SoftTheme.TealDark
                    )
                )
            }

            // Theme Mode Toggle Button (Dark / Light)
            Surface(
                onClick = onToggleTheme,
                shape = CircleShape,
                color = SoftTheme.CardBg,
                border = androidx.compose.foundation.BorderStroke(1.dp, SoftTheme.CardBorder),
                shadowElevation = 2.dp,
                modifier = Modifier
                    .size(40.dp)
                    .testTag("top_theme_toggle_button")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (SoftTheme.isDark) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                        contentDescription = if (SoftTheme.isDark) "تفعيل المظهر المضيء" else "تفعيل المظهر الداكن",
                        tint = if (SoftTheme.isDark) Color(0xFFFFD54F) else SoftTheme.TealDark,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Box {
                Surface(
                    onClick = onNotificationsClick,
                    shape = CircleShape,
                    color = SoftTheme.CardBg,
                    border = androidx.compose.foundation.BorderStroke(1.dp, SoftTheme.CardBorder),
                    shadowElevation = 2.dp,
                    modifier = Modifier
                        .size(40.dp)
                        .testTag("top_notifications_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Outlined.Notifications,
                            contentDescription = "التنبيهات",
                            tint = SoftTheme.TextSecondaryMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                if (hasUnreadNotifications) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(Color(0xFFE91E63), CircleShape)
                            .align(Alignment.TopEnd)
                            .offset(x = (-3).dp, y = 3.dp)
                    )
                }
            }
        }
    }
}
