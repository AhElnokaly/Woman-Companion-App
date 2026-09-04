package com.example.ui.onboarding

import android.app.DatePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.GlassmorphicCard
import com.example.ui.SoftTheme
import com.example.ui.formatGregorianDate
import java.util.*

@Composable
fun StepPersonalInfo(
    name: String,
    onNameChange: (String) -> Unit,
    nickname: String,
    onNicknameChange: (String) -> Unit,
    birthDateMs: Long?,
    onBirthDateChange: (Long?) -> Unit
) {
    val context = LocalContext.current

    GlassmorphicCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF2196F3).copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("📝", fontSize = 22.sp)
                }
                Column {
                    Text(
                        text = "الترحيب بكِ والاسم المحبب",
                        fontWeight = FontWeight.Bold,
                        color = SoftTheme.TextWhite,
                        style = MaterialTheme.typography.titleMedium,
                        fontSize = 18.sp
                    )
                    Text(
                        text = "دعي جوري تتعرف عليكِ لتخصيص كامل للغة الخطاب",
                        color = SoftTheme.SoftGray,
                        fontSize = 11.sp
                    )
                }
            }

            Text(
                text = "سلامتكِ يا غالية تهمني جداً، أنا صديقتكِ جوري وأريد أن أناديكِ دائماً بألطف الأسماء وأقربها لقلبكِ لتقديم الدعم الدافئ.",
                color = SoftTheme.SoftGray,
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 22.sp
            )

            // Showcase Hero Visual Card for Jouri
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = SoftTheme.DeepSlate.copy(alpha = 0.6f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Image(
                        painter = painterResource(id = R.drawable.jouri_showcase_1783592034174),
                        contentDescription = "جوري رفيقتكِ الذكية",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.75f)
                                    )
                                )
                            )
                    )
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("🌸", fontSize = 20.sp)
                        Column {
                            Text(
                                text = "مرحباً بكِ، أنا جوري 🌸",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "رفيقتكِ الذكية لمتابعة صحتكِ ونمط حياتكِ بأمان وخصوصية",
                                color = SoftTheme.LightPink,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            HorizontalDivider(color = Color(0xFF2196F3).copy(alpha = 0.25f), thickness = 1.dp)

            // Full Name Input
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "ما هو اسمكِ الكريم؟",
                    color = SoftTheme.TextWhite,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = onNameChange,
                    placeholder = { Text("أدخلي اسمكِ هنا...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("onboarding_name_input"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = SoftTheme.DeepSlate.copy(alpha = 0.5f),
                        unfocusedContainerColor = SoftTheme.DeepSlate.copy(alpha = 0.3f),
                        focusedBorderColor = Color(0xFF2196F3),
                        unfocusedBorderColor = SoftTheme.SoftGray.copy(alpha = 0.3f),
                        focusedTextColor = SoftTheme.TextWhite,
                        unfocusedTextColor = SoftTheme.TextWhite,
                        focusedPlaceholderColor = SoftTheme.SoftGray.copy(alpha = 0.7f),
                        unfocusedPlaceholderColor = SoftTheme.SoftGray.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(14.dp)
                )
            }

            // Nickname Input
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "اسم الدلع (كيف تحبين أن أناديكِ؟) 💕",
                    color = SoftTheme.TextWhite,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
                OutlinedTextField(
                    value = nickname,
                    onValueChange = onNicknameChange,
                    placeholder = { Text("مثال: رورو، لولو، ميمي...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("onboarding_nickname_input"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = SoftTheme.DeepSlate.copy(alpha = 0.5f),
                        unfocusedContainerColor = SoftTheme.DeepSlate.copy(alpha = 0.3f),
                        focusedBorderColor = Color(0xFF2196F3),
                        unfocusedBorderColor = SoftTheme.SoftGray.copy(alpha = 0.3f),
                        focusedTextColor = SoftTheme.TextWhite,
                        unfocusedTextColor = SoftTheme.TextWhite,
                        focusedPlaceholderColor = SoftTheme.SoftGray.copy(alpha = 0.7f),
                        unfocusedPlaceholderColor = SoftTheme.SoftGray.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(14.dp)
                )
            }

            // Birth Date Picker
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "تاريخ ميلادكِ المبارك:",
                    color = SoftTheme.TextWhite,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
                Button(
                    onClick = {
                        val calendar = Calendar.getInstance().apply {
                            if (birthDateMs != null) timeInMillis = birthDateMs
                        }
                        val dateDialog = DatePickerDialog(
                            context,
                            { _, y, m, d ->
                                val cal = Calendar.getInstance().apply {
                                    set(Calendar.YEAR, y)
                                    set(Calendar.MONTH, m)
                                    set(Calendar.DAY_OF_MONTH, d)
                                    set(Calendar.HOUR_OF_DAY, 0)
                                    set(Calendar.MINUTE, 0)
                                    set(Calendar.SECOND, 0)
                                    set(Calendar.MILLISECOND, 0)
                                }
                                onBirthDateChange(cal.timeInMillis)
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                        )
                        dateDialog.datePicker.maxDate = System.currentTimeMillis()
                        dateDialog.show()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("onboarding_dob_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SoftTheme.DeepSlate.copy(alpha = 0.6f),
                        contentColor = SoftTheme.TextWhite
                    ),
                    border = BorderStroke(1.dp, Color(0xFF2196F3).copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text("🗓️", fontSize = 18.sp)
                            Text(
                                text = if (birthDateMs != null) formatGregorianDate(birthDateMs) else "انقري لتحديد تاريخ ميلادكِ بدقة",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Icon(Icons.Default.Edit, contentDescription = null, tint = Color(0xFF2196F3), modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}
