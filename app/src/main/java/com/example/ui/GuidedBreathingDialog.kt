package com.example.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.SoftTheme
import kotlinx.coroutines.delay

enum class BreathingPhase(val title: String, val subtitle: String, val color: Color, val durationSec: Int) {
    INHALE("شهيق عميق", "تنفسي ببطء وهدوء من الأنف...", Color(0xFF80CBC4), 4),
    HOLD_IN("حبس النفس بلطف", "استشعري الراحة والسكينة...", Color(0xFFFFAB91), 4),
    EXHALE("زفير بطيء", "أخرجي كل التوتر بهدوء من الفم...", Color(0xFFF48FB1), 4),
    HOLD_OUT("استراحة وسكون", "استرخي تماماً قبل الدورة التالية...", Color(0xFFCE93D8), 4)
}

@Composable
fun GuidedBreathingDialog(
    onDismiss: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    var isRunning by remember { mutableStateOf(true) }
    var currentPhaseIndex by remember { mutableIntStateOf(0) }
    var phaseSecondsRemaining by remember { mutableIntStateOf(4) }
    var completedCycles by remember { mutableIntStateOf(0) }

    val phases = BreathingPhase.values()
    val currentPhase = phases[currentPhaseIndex]

    // Breathing loop timer
    LaunchedEffect(isRunning, currentPhaseIndex) {
        if (isRunning) {
            phaseSecondsRemaining = currentPhase.durationSec
            while (isRunning && phaseSecondsRemaining > 0) {
                delay(1000L)
                phaseSecondsRemaining--
            }
            if (isRunning && phaseSecondsRemaining == 0) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                val nextIndex = (currentPhaseIndex + 1) % phases.size
                if (nextIndex == 0) {
                    completedCycles++
                }
                currentPhaseIndex = nextIndex
            }
        }
    }

    // Animation scale for the glowing breathing circle
    val animatedScale by animateFloatAsState(
        targetValue = when (currentPhase) {
            BreathingPhase.INHALE -> 1.35f
            BreathingPhase.HOLD_IN -> 1.35f
            BreathingPhase.EXHALE -> 0.85f
            BreathingPhase.HOLD_OUT -> 0.85f
        },
        animationSpec = tween(durationMillis = 4000, easing = FastOutSlowInEasing),
        label = "circle_scale"
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("guided_breathing_dialog"),
            colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
            shape = RoundedCornerShape(28.dp),
            border = BorderStroke(1.dp, SoftTheme.MintTeal.copy(alpha = 0.4f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Spa,
                            contentDescription = null,
                            tint = SoftTheme.MintTeal,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "تنفس الاسترخاء والسكينة 🌸",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = SoftTheme.TextWhite
                        )
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "إغلاق",
                            tint = SoftTheme.SoftGray
                        )
                    }
                }

                Text(
                    text = "دورة تنفس مربعة (Box Breathing) معتمدة لتهدئة الجهاز العصبي وتخفيف القلق وتحسين النوم.",
                    style = MaterialTheme.typography.bodySmall,
                    color = SoftTheme.SoftGray,
                    textAlign = TextAlign.Center,
                    fontSize = 11.sp
                )

                // Breathing Visual Circle Canvas
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Outer pulsating wave
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .scale(animatedScale)
                    ) {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    currentPhase.color.copy(alpha = 0.45f),
                                    currentPhase.color.copy(alpha = 0.05f)
                                )
                            )
                        )
                        drawCircle(
                            color = currentPhase.color.copy(alpha = 0.6f),
                            style = Stroke(width = 3.dp.toPx())
                        )
                    }

                    // Inner Center Content
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = currentPhase.title,
                            fontWeight = FontWeight.Bold,
                            color = SoftTheme.TextWhite,
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${phaseSecondsRemaining}ث",
                            fontWeight = FontWeight.ExtraBold,
                            color = currentPhase.color,
                            fontSize = 24.sp
                        )
                    }
                }

                // Phase descriptive hint
                Surface(
                    color = SoftTheme.DeepSlate,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = currentPhase.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = SoftTheme.TextWhite,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp),
                        fontSize = 12.sp
                    )
                }

                // Stats & Controls Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "الدورات المكتملة: $completedCycles 🌿",
                        style = MaterialTheme.typography.bodySmall,
                        color = SoftTheme.MintTeal,
                        fontWeight = FontWeight.Bold
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(
                            onClick = {
                                currentPhaseIndex = 0
                                phaseSecondsRemaining = 4
                                completedCycles = 0
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = "إعادة البدء",
                                tint = SoftTheme.SoftGray
                            )
                        }

                        Button(
                            onClick = { isRunning = !isRunning },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isRunning) SoftTheme.DeepSlate else SoftTheme.SoftPink
                            ),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = if (isRunning) Icons.Default.Stop else Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = SoftTheme.TextWhite,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isRunning) "إيقاف مؤقت" else "متابعة",
                                color = SoftTheme.TextWhite,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
