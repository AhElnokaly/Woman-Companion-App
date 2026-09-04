package com.example.ui.pregnancy

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SoftTheme

@Composable
fun PregnancyBabyInfoDisplayCard(
    babyGender: String?,
    babyName: String?,
    onOpenEditDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isGenderKnown = !babyGender.isNullOrEmpty()

    if (isGenderKnown) {
        // Compact space-saving version of Baby Info Card
        Card(
            modifier = modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier.padding(12.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(SoftTheme.DeepSlate),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(if (babyGender == "ولد") "👶" else if (babyGender == "بنت") "👧" else "🤰", fontSize = 18.sp)
                    }
                    Column {
                        val genderEmoji = if (babyGender == "ولد") "💙" else if (babyGender == "بنت") "💗" else "✨"
                        val genderLabel = if (babyGender == "ولد") "ولد صالح معافى" else if (babyGender == "بنت") "بنت صالحة معافاة" else "مفاجأة مباركة"
                        val nameLabel = if (!babyName.isNullOrBlank()) "الاسم المقترح: $babyName" else "لم يتم اختيار اسم بعد"

                        Text(
                            text = "$genderLabel $genderEmoji",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = SoftTheme.TextWhite
                        )
                        Text(
                            text = nameLabel,
                            style = MaterialTheme.typography.bodySmall,
                            color = SoftTheme.SoftPink
                        )
                    }
                }
                Text(
                    text = "تعديل 📝",
                    color = SoftTheme.SoftPink,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onOpenEditDialog() }
                )
            }
        }
    } else {
        // Expandable standard registration Card (visible when gender is not yet recorded)
        Card(
            modifier = modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🧸", fontSize = 22.sp)
                        Text(
                            text = "جنينكِ الغالي",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = SoftTheme.TextWhite
                        )
                    }
                    Text(
                        text = "تسجيل 📝",
                        color = SoftTheme.SoftPink,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { onOpenEditDialog() }
                    )
                }
                Text(
                    text = "لقد دخلتِ الأسبوع ١٤ من الحمل 🌸 هل عرفتِ جنس جنينكِ؟ اضغطي لتسجيله واقتراح اسمه لكي يتفاعل رفيقكِ مع جنينكِ بالاسم والتهنئة اللطيفة! 💕",
                    color = SoftTheme.SoftGray,
                    style = MaterialTheme.typography.bodySmall,
                    lineHeight = 16.sp
                )
                Button(
                    onClick = onOpenEditDialog,
                    colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.DeepSlate),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("تسجيل جنس واسم الجنين 👶🍼", color = SoftTheme.TextWhite, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
