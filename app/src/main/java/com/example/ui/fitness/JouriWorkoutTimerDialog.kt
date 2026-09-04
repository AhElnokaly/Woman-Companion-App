package com.example.ui.fitness

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.SoftTheme
import kotlinx.coroutines.delay

@Composable
fun JouriWorkoutTimerDialog(
    exercise: JouriExercise,
    onDismiss: () -> Unit,
    onWorkoutCompleted: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    var timeLeft by remember { mutableStateOf(exercise.durationSeconds) }
    var isRunning by remember { mutableStateOf(true) }
    var isCompleted by remember { mutableStateOf(false) }

    // Breathing cues state
    var breathingCue by remember { mutableStateOf("شهيق عميق ولطيف 🌸") }

    LaunchedEffect(isRunning, timeLeft) {
        if (isRunning && timeLeft > 0) {
            delay(1000)
            timeLeft -= 1

            // Dynamic change of breathing cues every 5 seconds to guide mothers perfectly
            val newCueIndex = (timeLeft / 5) % 3
            val newCue = when (newCueIndex) {
                0 -> "شهيق عميق وهادئ من الأنف 🌸"
                1 -> "حبس النفس بلطف للحظة... 🍃"
                else -> "زفير طويل وبطيء مريح من الفم 💕"
            }
            if (newCue != breathingCue) {
                breathingCue = newCue
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            }
        } else if (timeLeft == 0) {
            isCompleted = true
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    Dialog(onDismissRequest = { }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1B222E)),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                if (!isCompleted) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "جلسة تمرين نشطة 🧘‍♀️",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF38B2AC),
                            fontSize = 14.sp
                        )
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(28.dp)
                                .background(Color(0xFF141921), CircleShape)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "إلغاء",
                                tint = Color(0xFF38B2AC),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Text(
                        text = exercise.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = SoftTheme.TextWhite,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Circular Countdown Timer Display
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(160.dp)
                    ) {
                        val progress = timeLeft.toFloat() / exercise.durationSeconds.toFloat()
                        CircularProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.fillMaxSize(),
                            color = Color(0xFF38B2AC),
                            strokeWidth = 8.dp,
                            trackColor = Color(0xFF141921),
                            strokeCap = StrokeCap.Round,
                        )

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = String.format("%02d:%02d", timeLeft / 60, timeLeft % 60),
                                fontSize = 34.sp,
                                fontWeight = FontWeight.Black,
                                color = SoftTheme.TextWhite
                            )
                            Text(
                                text = "متبقي",
                                fontSize = 11.sp,
                                color = Color(0xFF8F9CAE)
                            )
                        }
                    }

                    // Breathing guide visual card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF141921)),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "مساعد التنفس المتناغم من جوري:",
                                fontSize = 10.sp,
                                color = Color(0xFF38B2AC),
                                fontWeight = FontWeight.Bold
                            )
                            AnimatedContent(
                                targetState = breathingCue,
                                transitionSpec = {
                                    slideInVertically { height -> height } + fadeIn() togetherWith
                                            slideOutVertically { height -> -height } + fadeOut()
                                },
                                label = "breathing"
                            ) { text ->
                                Text(
                                    text = text,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = SoftTheme.TextWhite,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Controls row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { isRunning = !isRunning },
                            modifier = Modifier
                                .size(56.dp)
                                .background(Color(0xFF38B2AC), CircleShape)
                        ) {
                            Icon(
                                imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isRunning) "إيقاف مؤقت" else "استئناف",
                                tint = Color(0xFF141921),
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(20.dp))

                        Button(
                            onClick = { isCompleted = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2D3748)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                "إنهاء الجلسة 🏆",
                                color = SoftTheme.TextWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                } else {
                    // Celebration Screen
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(Color(0xFF38B2AC).copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🎉", fontSize = 42.sp)
                    }

                    Text(
                        text = "عمل رائع ومثالي يا غالية! 🥳🌸",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF38B2AC),
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "لقد أكملتِ تمرين \"${exercise.name}\" بنجاح واقتدار. جوري فخورة بكِ وبعنايتكِ الفائقة بصحتكِ وسلامتكِ اليوم! 💕",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SoftTheme.TextWhite,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = onWorkoutCompleted,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38B2AC)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("workout_done_button"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(
                            text = "حفظ وإضافة الجلسة لسجلي اليومي 📝",
                            color = Color(0xFF141921),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}
