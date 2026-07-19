package com.example.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.WeatherInfo

enum class WeatherState(
    val description: String,
    val iconEmoji: String,
    val startGradient: Color,
    val endGradient: Color,
    val advice: String
) {
    SUNNY(
        "مشمس وحار ☀️", "☀️",
        Color(0xFFFFE082), Color(0xFFFFB300),
        "الجو مشمس وحار اليوم يا غالية! ☀️ احرصي تماماً على شرب ما لا يقل عن ٣ لتر مياه، وارتداء ملابس خفيفة مريحة وتجنب أشعة الشمس المباشرة لحمايتكِ وحماية طفلكِ."
    ),
    CLOUDY(
        "غائم ولطيف ☁️", "☁️",
        Color(0xFFCFD8DC), Color(0xFF78909C),
        "الجو غائم ومريح اليوم يا حبيبتي ☁️. الطقس مثالي للمشي الخفيف في الهواء الطلق لمدة ١٥-٢٠ دقيقة لتنشيط دورتكِ الدموية وتحسين المزاج."
    ),
    RAINY(
        "أمطـار وغيوم 🌧️", "🌧️",
        Color(0xFF90A4AE), Color(0xFF37474F),
        "هناك تساقط للأمطار أو طقس بارد غائم اليوم 🌧️. ابقِ دافئة في المنزل، واحتسي كوباً من الينسون الدافئ أو البابونج المنعش لراحة بطنكِ."
    ),
    HUMID(
        "رطوبة مرتفعة 🌫️", "🌫️",
        Color(0xFFB2EBF2), Color(0xFF00ACC1),
        "الرطوبة مرتفعة اليوم في الجو 🌫️. ننصحكِ بشرب رشفات صغيرة من المياه باستمرار وتجنب الأماكن المزدحمة لسهولة التنفس والراحة التامة."
    )
}

@Composable
fun JouriWeatherHeader(
    weatherInfo: WeatherInfo?,
    isDarkMode: Boolean = SoftTheme.isDark
) {
    val temp = weatherInfo?.temperature ?: 25.0
    val humidity = weatherInfo?.humidity ?: 50

    // Deduce weather state based on temp and humidity
    val weatherState = when {
        temp > 32 -> WeatherState.SUNNY
        temp < 18 -> WeatherState.RAINY
        humidity > 70 -> WeatherState.HUMID
        else -> WeatherState.CLOUDY
    }

    val infiniteTransition = rememberInfiniteTransition(label = "WeatherIconTransition")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "WeatherScale"
    )

    GlassmorphicCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        isDarkMode = isDarkMode
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Title row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.Start) {
                    Text(
                        text = "الطقس الآن محلياً 🌡️",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SoftTheme.TextWhite
                    )
                    Text(
                        text = "مستوحى من طقس جوجل للرعاية اليومية",
                        fontSize = 10.sp,
                        color = SoftTheme.SoftGray
                    )
                }

                // Dynamic glowing weather badge
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .graphicsLayer(scaleX = scale, scaleY = scale)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(weatherState.startGradient, Color.Transparent)
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(weatherState.iconEmoji, fontSize = 22.sp)
                }
            }

            // Temperature & Condition Layout
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "${temp.toInt()}°م",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Black,
                        color = SoftTheme.TextWhite
                    )
                    Column {
                        Text(
                            text = weatherState.description,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = SoftTheme.SoftPink
                        )
                        Text(
                            text = "الرطوبة النسبية: $humidity%",
                            fontSize = 11.sp,
                            color = SoftTheme.SoftGray
                        )
                    }
                }
            }

            // Jouri's Weather-based Advice Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = SoftTheme.DeepSlate.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(12.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("🌸", fontSize = 14.sp)
                        Text(
                            text = "نصيحة جوري للطقس الحالي:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SoftTheme.SoftTeal
                        )
                    }
                    Text(
                        text = weatherState.advice,
                        fontSize = 11.sp,
                        color = SoftTheme.TextWhite,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}
