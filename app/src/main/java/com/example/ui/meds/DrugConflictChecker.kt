package com.example.ui.meds

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.SoftTheme

/**
 * فاحص التداخلات الدوائية والغذائية التثقيفي والوقائي (Drug-Nutrient Conflict Warning)
 * يحذر من الجمع بين الحديد والكالسيوم أو منتجات الألبان والشاي المغلي
 */
@Composable
fun DrugNutrientConflictBanner(
    isIronTakenOrScheduled: Boolean,
    isCalciumTakenOrScheduled: Boolean,
    modifier: Modifier = Modifier
) {
    // Show alert when both Iron and Calcium are active/checked together
    val hasConflict = isIronTakenOrScheduled && isCalciumTakenOrScheduled

    AnimatedVisibility(
        visible = hasConflict,
        modifier = modifier
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("drug_nutrient_conflict_card"),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF332014)),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color(0xFFFF9800).copy(alpha = 0.6f))
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("⚠️", fontSize = 20.sp)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "تنبيه وقائي: افصلي بين مكمل الحديد والكالسيوم 💊⚡",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFB74D),
                        fontSize = 13.sp
                    )
                    Text(
                        text = "يا غالية، تناول الحديد والكالسيوم (أو الحليب ومشتقاته) في نفس الوقت يبطل امتصاص الحديد تماماً. يرجى الفصل بينهما بساعتين على الأقل، وتجنب الشاي بعد حبة الحديد بساعة لضمان وصول التغذية لصغيركِ وحمايتكِ من الأنيميا ✨.",
                        color = SoftTheme.TextWhite,
                        fontSize = 11.sp,
                        lineHeight = 17.sp
                    )
                }
            }
        }
    }
}
