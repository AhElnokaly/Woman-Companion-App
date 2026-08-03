package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.util.formatArabicDays
import com.example.viewmodel.WomanCompanionViewModel

// --- Qada Fast Sub-screen ---
@Composable
fun QadaSubScreen(viewModel: WomanCompanionViewModel) {
    val qadaList by viewModel.qadaFastsState.collectAsStateWithLifecycle()
    val currentPhase = viewModel.getCurrentCyclePhase()
    val periodLogs by viewModel.periodLogsState.collectAsStateWithLifecycle()
    val pregState by viewModel.pregnancyState.collectAsStateWithLifecycle()

    var showAddQadaDialog by remember { mutableStateOf(false) }
    var yearInput by remember { mutableStateOf("") }
    var missedInput by remember { mutableStateOf("") }

    var completedPrayers by remember { mutableStateOf(setOf<String>()) }

    // Adaptive logic:
    val userPhase = pregState?.userPhase ?: "period"
    val isOnPeriod = currentPhase.phaseName == "Menstruation" && pregState == null
    val isInPostpartum = userPhase == "postpartum"
    val isExempt = isOnPeriod || isInPostpartum

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("مساعد العبادات والصلوات الذكي 🌙", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = SoftTheme.TextWhite)
        Text(
            "يتكيف تلقائياً مع فترات دورتكِ الشهرية ونفاسكِ لضمان حماية سجلاتك الدينية بدقة.",
            color = SoftTheme.SoftGray,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center
        )

        if (isExempt) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, SoftTheme.GoldFasting.copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("🌸 رخصة شرعية وعذر رحيم", fontWeight = FontWeight.Bold, color = SoftTheme.GoldFasting, style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = if (isInPostpartum) {
                            "أنتِ الآن في فترة النفاس المباركة (رخصة شرعية من رب العالمين) 🥰 ارتاحي واعتني بنفسك وبطفلكِ، واذكري الله واستغفري. لا صلاة ولا صيام عليكِ الآن."
                        } else {
                            "أنتِ في رخصة شرعية رقيقة بسبب العذر الشرعي (الحيض) 🥰 ارتاحي واحتسبي الأجر في الاستغفار والذكر والعبادات القلبية. لا صلاة عليكِ ولا صيام."
                        },
                        textAlign = TextAlign.Center,
                        color = SoftTheme.TextWhite,
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 22.sp
                    )

                    if (!isInPostpartum) {
                        Button(
                            onClick = {
                                val currentHijriYear = 1447 // Current Hijri Year
                                val existing = qadaList.find { it.yearHijri == currentHijriYear }
                                if (existing != null) {
                                    viewModel.addQadaFast(currentHijriYear, existing.missedDays + 1, existing.completedDays)
                                } else {
                                    viewModel.addQadaFast(currentHijriYear, 1, 0)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.GoldFasting),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("تسجيل يوم فطر تلقائي لقضائه لاحقاً 📅", color = SoftTheme.DeepSlate, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("🕌 جدول صلواتكِ لليوم (طهر ونشاط)", fontWeight = FontWeight.Bold, color = SoftTheme.MintTeal, style = MaterialTheme.typography.titleMedium)
                    Text("حافظي على صلواتكِ الخمس اليومية وتابعي التزامكِ الروحي الجميل:", color = SoftTheme.SoftGray, style = MaterialTheme.typography.bodySmall)

                    val prayers = listOf("الفجر", "الظهر", "العصر", "المغرب", "العشاء")
                    prayers.forEach { prayer ->
                        val isChecked = completedPrayers.contains(prayer)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    completedPrayers = if (isChecked) {
                                        completedPrayers - prayer
                                    } else {
                                        completedPrayers + prayer
                                    }
                                }
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(
                                    imageVector = if (isChecked) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                    contentDescription = null,
                                    tint = if (isChecked) SoftTheme.MintTeal else SoftTheme.SoftGray
                                )
                                Text(text = prayer, color = SoftTheme.TextWhite, fontWeight = FontWeight.Bold)
                            }
                            Text(
                                text = if (isChecked) "مكتملة ✨" else "لم تُؤدَّ بعد",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isChecked) SoftTheme.MintTeal else SoftTheme.SoftGray
                            )
                        }
                    }
                }
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = SoftTheme.CardSlate)

        Text("تتبع قضاء أيام صيام رمضان 🌙", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = SoftTheme.TextWhite)
        Text(
            "سجلي الأيام الفائتة بسبب الحيض أو رخصة الفطر في الحمل في سنوات رمضان المختلفة وتابعي تقدمكِ في القضاء بيسر وسهولة.",
            color = SoftTheme.SoftGray,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center
        )

        Button(
            onClick = { showAddQadaDialog = true },
            colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.SoftPink),
            modifier = Modifier.fillMaxWidth().testTag("add_qada_btn")
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("إضافة سنة جديدة")
        }

        if (qadaList.isEmpty()) {
            Text("لا توجد سجلات قضاء بعد.", color = SoftTheme.SoftGray)
        } else {
            qadaList.forEach { fast ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("رمضان هجري: ${fast.yearHijri}", fontWeight = FontWeight.Bold, color = SoftTheme.TextWhite)
                            IconButton(onClick = { viewModel.deleteQadaFast(fast) }) {
                                Icon(Icons.Default.Delete, contentDescription = "حذف", tint = SoftTheme.RedDanger)
                            }
                        }

                        LinearProgressIndicator(
                            progress = { fast.completedDays.toFloat() / fast.missedDays.toFloat().coerceAtLeast(1f) },
                            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                            color = SoftTheme.GoldFasting,
                            trackColor = SoftTheme.DeepSlate
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "المكتمل: ${formatArabicDays(fast.completedDays)} من أصل ${formatArabicDays(fast.missedDays)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = SoftTheme.SoftGray
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = { viewModel.updateQadaFastProgress(fast, increment = false) },
                                    colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.DeepSlate),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp)
                                ) {
                                    Text("-", color = SoftTheme.TextWhite, fontSize = 16.sp)
                                }
                                Button(
                                    onClick = { viewModel.updateQadaFastProgress(fast, increment = true) },
                                    colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.GoldFasting),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp)
                                ) {
                                    Text("+", color = SoftTheme.DeepSlate, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(80.dp))
    }

    if (showAddQadaDialog) {
        Dialog(onDismissRequest = { showAddQadaDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("إضافة قضاء صيام 🌙", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = SoftTheme.SoftPink)

                    OutlinedTextField(
                        value = yearInput,
                        onValueChange = { yearInput = it },
                        label = { Text("السنة الهجرية (مثال: ١٤٤٧)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = missedInput,
                        onValueChange = { missedInput = it },
                        label = { Text("عدد الأيام الفائتة") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        TextButton(onClick = { showAddQadaDialog = false }, modifier = Modifier.weight(1f)) {
                            Text("إلغاء", color = SoftTheme.SoftGray)
                        }
                        Button(
                            onClick = {
                                val year = yearInput.toIntOrNull() ?: 1447
                                val missed = missedInput.toIntOrNull() ?: 7
                                viewModel.addQadaFast(year, missed, 0)
                                showAddQadaDialog = false
                                yearInput = ""
                                missedInput = ""
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.SoftPink),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("حفظ")
                        }
                    }
                }
            }
        }
    }
}
