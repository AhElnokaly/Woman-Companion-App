package com.example.ui.pregnancy

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.SoftTheme

@Composable
fun PregnancySpiritualCard(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate.copy(alpha = 0.8f)),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "الجانب الروحي والسكينة 📖✨",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = SoftTheme.SoftPink,
                textAlign = TextAlign.Center
            )
            Text(
                text = "﴿رَبِّ هَبْ لِي مِن لَّدُنكَ ذُرِّيَّةً طَيِّبَةً ۖ إِنَّكَ سَمِيعُ الدُّعَاءِ﴾",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = SoftTheme.TextWhite,
                textAlign = TextAlign.Center,
                lineHeight = 26.sp
            )
            Text(
                text = "طمأنينة قلبكِ وراحتكِ النفسية تنعكس حباً وسلاماً على صحتكِ ونمو طفلكِ. استودعي نفسكِ وجنينكِ الله الخالق العليم كل يوم.",
                style = MaterialTheme.typography.bodySmall,
                color = SoftTheme.SoftGray,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )
        }
    }
}
