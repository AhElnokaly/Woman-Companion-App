package com.example.ui.meds

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.SoftTheme

@Composable
fun MedAdvancedOptionsSection(
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    prescribedDoctor: String,
    onPrescribedDoctorChange: (String) -> Unit,
    medNotes: String,
    onMedNotesChange: (String) -> Unit,
    stockCount: String,
    onStockCountChange: (String) -> Unit,
    courseDurationDays: String,
    onCourseDurationDaysChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Accordion Header button
        Surface(
            color = SoftTheme.CardSlate,
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, if (isExpanded) SoftTheme.SoftPink.copy(alpha = 0.5f) else SoftTheme.SoftGray.copy(alpha = 0.2f)),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggleExpand() }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("⚙️", fontSize = 16.sp)
                    Text(
                        text = "خيارات إضافية ومتقدمة (اختياري)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = SoftTheme.TextWhite
                    )
                }

                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = SoftTheme.SoftPink
                )
            }
        }

        // Accordion Content
        AnimatedVisibility(visible = isExpanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(SoftTheme.CardSlate.copy(alpha = 0.7f))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Stock / Count & Duration
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = stockCount,
                        onValueChange = onStockCountChange,
                        label = { Text("المخزون بالعلبة (قرص/مل)", fontSize = 10.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SoftTheme.MintTeal,
                            unfocusedBorderColor = SoftTheme.DeepSlate,
                            focusedTextColor = SoftTheme.TextWhite,
                            unfocusedTextColor = SoftTheme.TextWhite
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = courseDurationDays,
                        onValueChange = onCourseDurationDaysChange,
                        label = { Text("مدة الكورس (أيام)", fontSize = 10.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        placeholder = { Text("مستمر إذا ترك فارغاً", fontSize = 9.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SoftTheme.MintTeal,
                            unfocusedBorderColor = SoftTheme.DeepSlate,
                            focusedTextColor = SoftTheme.TextWhite,
                            unfocusedTextColor = SoftTheme.TextWhite
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    )
                }

                // Prescribed By Doctor
                OutlinedTextField(
                    value = prescribedDoctor,
                    onValueChange = onPrescribedDoctorChange,
                    label = { Text("الطبيبة المعالجة / المستشفى (اختياري)", fontSize = 11.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SoftTheme.SoftPink,
                        unfocusedBorderColor = SoftTheme.DeepSlate,
                        focusedTextColor = SoftTheme.TextWhite,
                        unfocusedTextColor = SoftTheme.TextWhite
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // Notes / Instructions
                OutlinedTextField(
                    value = medNotes,
                    onValueChange = onMedNotesChange,
                    label = { Text("تعليمات وملاحظات خاصة", fontSize = 11.sp) },
                    placeholder = { Text("مثال: شرب كوب ماء كبير، تجنب الكافيين بعدها لساعة...", fontSize = 10.sp) },
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SoftTheme.SoftPink,
                        unfocusedBorderColor = SoftTheme.DeepSlate,
                        focusedTextColor = SoftTheme.TextWhite,
                        unfocusedTextColor = SoftTheme.TextWhite
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
