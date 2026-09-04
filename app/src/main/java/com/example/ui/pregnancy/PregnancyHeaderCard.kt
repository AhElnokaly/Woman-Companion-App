package com.example.ui.pregnancy

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.WeatherInfo
import com.example.ui.theme.SoftTheme
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.delay

@Composable
fun PregnancyHeaderCard(
    companionName: String,
    isNetworkAvailable: Boolean,
    weatherState: WeatherInfo?,
    onToggleDarkMode: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greeting = when {
        hour in 5..11 -> "صباح الورد والرضا يا غالية ☀️"
        hour in 12..17 -> "أهلاً بكِ يا صديقتي ✨"
        else -> "مساء الهدوء والسكينة يا غالية 🌙"
    }

    var currentTimeString by remember {
        mutableStateOf(
            SimpleDateFormat("hh:mm a", Locale.forLanguageTag("ar")).format(Calendar.getInstance().time)
        )
    }
    var currentDateString by remember {
        mutableStateOf(
            SimpleDateFormat("EEEE، d MMMM", Locale.forLanguageTag("ar")).format(Calendar.getInstance().time)
        )
    }

    LaunchedEffect(Unit) {
        val timeSdf = SimpleDateFormat("hh:mm a", Locale.forLanguageTag("ar"))
        val dateSdf = SimpleDateFormat("EEEE، d MMMM", Locale.forLanguageTag("ar"))
        while (true) {
            val cal = Calendar.getInstance()
            currentTimeString = timeSdf.format(cal.time)
            currentDateString = dateSdf.format(cal.time)
            delay(1000)
        }
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = greeting,
                        style = MaterialTheme.typography.titleMedium,
                        color = SoftTheme.SoftPink,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "جوري 🌸",
                            style = MaterialTheme.typography.headlineSmall,
                            color = SoftTheme.TextWhite,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isNetworkAvailable) SoftTheme.MintTeal.copy(alpha = 0.15f) else SoftTheme.RedDanger.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(if (isNetworkAvailable) SoftTheme.MintTeal else SoftTheme.RedDanger, CircleShape)
                                )
                                Text(
                                    text = if (isNetworkAvailable) "متصل" else "أوفلاين",
                                    color = if (isNetworkAvailable) SoftTheme.MintTeal else SoftTheme.RedDanger,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = onToggleDarkMode,
                        modifier = Modifier.background(SoftTheme.DeepSlate, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DarkMode,
                            contentDescription = "تبديل المظهر",
                            tint = SoftTheme.SoftPink
                        )
                    }
                    IconButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier.background(SoftTheme.DeepSlate, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "الإعدادات",
                            tint = SoftTheme.SoftGray
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "صديقتكِ الوفية $companionName تسهر على راحتكِ الروحية والصحية وتدعمكِ في كل خطوة ومرحلة 💖",
                style = MaterialTheme.typography.bodySmall,
                color = SoftTheme.SoftGray,
                lineHeight = 16.sp
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = SoftTheme.DeepSlate,
                thickness = 1.dp
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Clock & Date (Right side)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(SoftTheme.SoftPink.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🕒", fontSize = 18.sp)
                    }
                    Column {
                        Text(
                            text = currentTimeString,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = SoftTheme.TextWhite
                        )
                        Text(
                            text = currentDateString,
                            fontSize = 10.sp,
                            color = SoftTheme.SoftGray
                        )
                    }
                }

                // Weather Panel (Left side)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = if (weatherState != null) "${weatherState.temperature.toInt()}°م" else "--°م",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = SoftTheme.MintTeal
                        )
                        Text(
                            text = weatherState?.description ?: "جاري الجلب...",
                            fontSize = 10.sp,
                            color = SoftTheme.SoftGray
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(SoftTheme.MintTeal.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        val temp = weatherState?.temperature ?: 25.0
                        val emoji = when {
                            temp > 32 -> "☀️"
                            temp < 18 -> "🌧️"
                            else -> "🍃"
                        }
                        Text(emoji, fontSize = 18.sp)
                    }
                }
            }
        }
    }
}
