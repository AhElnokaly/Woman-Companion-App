package com.example.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.viewmodel.WomanCompanionViewModel

@Composable
fun AppointmentsSubScreen(viewModel: WomanCompanionViewModel) {
    val appointments by viewModel.appointmentsState.collectAsStateWithLifecycle()

    var showAddApptDialog by remember { mutableStateOf(false) }
    var titleInput by remember { mutableStateOf("") }
    var doctorInput by remember { mutableStateOf("") }
    var notesInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("جدول زيارات ومواعيد الدكتورة 🏥", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = SoftTheme.TextWhite)

        Button(
            onClick = { showAddApptDialog = true },
            colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.SoftPink),
            modifier = Modifier.fillMaxWidth().testTag("add_appt_btn")
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("إضافة موعد كشف جديد")
        }

        if (appointments.isEmpty()) {
            Text("لا توجد مواعيد مسجلة بعد.", color = SoftTheme.SoftGray)
        } else {
            appointments.forEach { appt ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = appt.title,
                                fontWeight = FontWeight.Bold,
                                color = if (appt.completed) SoftTheme.SoftGray else SoftTheme.TextWhite
                            )
                            appt.doctorName?.let {
                                Text("مع: د. $it", style = MaterialTheme.typography.bodySmall, color = SoftTheme.MintTeal)
                            }
                            appt.notes?.let {
                                Text("تفاصيل: $it", style = MaterialTheme.typography.bodySmall, color = SoftTheme.SoftGray)
                            }
                            Text("التاريخ: ${formatGregorianDate(appt.dateTime)}", style = MaterialTheme.typography.bodySmall, color = SoftTheme.SoftPink)
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Checkbox(
                                checked = appt.completed,
                                onCheckedChange = { viewModel.toggleAppointmentCompleted(appt) },
                                colors = CheckboxDefaults.colors(checkedColor = SoftTheme.MintTeal)
                            )
                            IconButton(onClick = { viewModel.deleteAppointment(appt) }) {
                                Icon(Icons.Default.Delete, contentDescription = "حذف", tint = SoftTheme.RedDanger)
                            }
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(80.dp))
    }

    if (showAddApptDialog) {
        Dialog(onDismissRequest = { showAddApptDialog = false }) {
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
                    Text("إضافة موعد طبي 🏥", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = SoftTheme.SoftPink)

                    OutlinedTextField(
                        value = titleInput,
                        onValueChange = { titleInput = it },
                        label = { Text("عنوان الموعد (مثال: سونار الثلث الثاني)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = doctorInput,
                        onValueChange = { doctorInput = it },
                        label = { Text("اسم الطبيبة") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = notesInput,
                        onValueChange = { notesInput = it },
                        label = { Text("ملاحظات الكشف أو التحاليل المطلوبة") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        TextButton(onClick = { showAddApptDialog = false }, modifier = Modifier.weight(1f)) {
                            Text("إلغاء", color = SoftTheme.SoftGray)
                        }
                        Button(
                            onClick = {
                                viewModel.addAppointment(
                                    title = titleInput,
                                    dateTime = System.currentTimeMillis() + 3L * 24 * 60 * 60 * 1000,
                                    doctor = doctorInput,
                                    notes = notesInput
                                )
                                showAddApptDialog = false
                                titleInput = ""
                                doctorInput = ""
                                notesInput = ""
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
