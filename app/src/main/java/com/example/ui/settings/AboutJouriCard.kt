package com.example.ui.settings

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.SoftTheme

@Composable
fun AboutJouriCard() {
    Card(
        colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Card(
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    androidx.compose.foundation.Image(
                        painter = painterResource(id = R.drawable.app_showcase_split_1783590277277),
                        contentDescription = "استعراض تطبيق جوري",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .border(1.5.dp, SoftTheme.SoftPink, CircleShape)
                ) {
                    androidx.compose.foundation.Image(
                        painter = painterResource(id = R.drawable.jouri_showcase_1783592034174),
                        contentDescription = "جوري",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Column {
                    Text(
                        "تطبيق جوري 🌸",
                        fontWeight = FontWeight.Bold,
                        color = SoftTheme.TextWhite,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        "رفيقتكِ الذكية لصحة وحياة مطمئنة",
                        color = SoftTheme.SoftPink,
                        fontSize = 12.sp
                    )
                }
            }

            Text(
                "تطبيق جوري مصمم خصيصاً لخصوصيتكِ التامة وراحتكِ. يعمل محلياً بنسبة 100% دون خوادم وسيطة، مع إمكانية التبديل السلس بين طور تتبع الدورة والخصوبة وطور الحمل المبارك.",
                style = MaterialTheme.typography.bodySmall,
                color = SoftTheme.SoftGray,
                lineHeight = 18.sp
            )
        }
    }
}
