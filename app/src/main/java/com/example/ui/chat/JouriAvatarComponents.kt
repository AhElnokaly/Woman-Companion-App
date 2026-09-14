package com.example.ui.chat

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.SoftTheme

enum class JouriExpressionState(val emoji: String, val label: String) {
    WARM_HAPPY("🌸", "سعيدة ودافئة"),
    ATTENTIVE_LISTENING("✍️🌸", "تستمع بإنصات"),
    REASSURING("🤗🌸", "طمأنينة ورعاية"),
    CELEBRATORY("🎉🌸", "محتفلة وفخورة")
}

@Composable
fun JouriAvatar(
    expression: JouriExpressionState = JouriExpressionState.WARM_HAPPY,
    size: Dp = 40.dp,
    modifier: Modifier = Modifier
) {
    val borderColor = when (expression) {
        JouriExpressionState.CELEBRATORY -> SoftTheme.PregnancyPurple
        JouriExpressionState.REASSURING -> SoftTheme.MintTeal
        JouriExpressionState.ATTENTIVE_LISTENING -> SoftTheme.LightPink
        JouriExpressionState.WARM_HAPPY -> SoftTheme.SoftPink
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .border(1.5.dp, borderColor, CircleShape)
            .testTag("jouri_avatar_${expression.name.lowercase()}"),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = com.example.R.drawable.jouri_showcase_1783592034174),
            contentDescription = "جوري",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun JouriChatSkeletonResponse(
    companionName: String = "جوري",
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "skeleton_shimmer")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "skeleton_alpha"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .testTag("jouri_chat_skeleton_response"),
        contentAlignment = Alignment.CenterStart
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = SoftTheme.DeepSlate),
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = 0.dp,
                bottomEnd = 16.dp
            ),
            modifier = Modifier.widthIn(max = 240.dp)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    JouriAvatar(expression = JouriExpressionState.ATTENTIVE_LISTENING, size = 20.dp)
                    Text(
                        text = "$companionName تفكر وتصيغ لكِ إجابة دافئة...",
                        style = MaterialTheme.typography.labelSmall,
                        color = SoftTheme.SoftPink
                    )
                }
                Box(
                    modifier = Modifier
                        .height(10.dp)
                        .fillMaxWidth(0.9f)
                        .background(SoftTheme.SoftGray.copy(alpha = alpha), RoundedCornerShape(6.dp))
                )
                Box(
                    modifier = Modifier
                        .height(10.dp)
                        .fillMaxWidth(0.7f)
                        .background(SoftTheme.SoftGray.copy(alpha = alpha), RoundedCornerShape(6.dp))
                )
                Box(
                    modifier = Modifier
                        .height(10.dp)
                        .fillMaxWidth(0.5f)
                        .background(SoftTheme.SoftGray.copy(alpha = alpha), RoundedCornerShape(6.dp))
                )
            }
        }
    }
}
