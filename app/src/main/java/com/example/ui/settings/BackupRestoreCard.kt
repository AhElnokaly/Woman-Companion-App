package com.example.ui.settings

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SoftTheme
import com.example.util.BackupManager
import com.example.viewmodel.WomanCompanionViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun BackupRestoreCard(
    viewModel: WomanCompanionViewModel
) {
    val context = LocalContext.current
    var pendingImportFile by remember { mutableStateOf<File?>(null) }
    var importErrorMessage by remember { mutableStateOf<String?>(null) }
    var showConfirmImportDialog by remember { mutableStateOf(false) }

    var showExportPasswordDialog by remember { mutableStateOf(false) }
    var exportPasswordInput by remember { mutableStateOf("") }
    var pendingExportPassword by remember { mutableStateOf("") }

    var showImportPasswordDialog by remember { mutableStateOf(false) }
    var pendingImportUri by remember { mutableStateOf<Uri?>(null) }
    var importPasswordInput by remember { mutableStateOf("") }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri ->
        if (uri != null && pendingExportPassword.isNotBlank()) {
            val success = BackupManager.exportEncryptedBackup(context, uri, pendingExportPassword)
            pendingExportPassword = ""
            if (success) {
                viewModel.markBackupExported()
                Toast.makeText(context, "تم تصدير النسخة الاحتياطية المشفرة بنجاح! 🔒📦", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(context, "فشل تصدير النسخة الاحتياطية.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            pendingImportUri = uri
            importPasswordInput = ""
            showImportPasswordDialog = true
        }
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth().testTag("backup_restore_card")
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("تصدير واستعادة النسخ الاحتياطية المشفرة 🔒📦", fontWeight = FontWeight.Bold, color = SoftTheme.SoftPink)
            Text("بياناتك مجهزة بنسخة احتياطية مشفرة بكلمة سر من اختياركِ. يمكنك حفظها على Google Drive أو وحدة التخزين واعادة استعادتها بكامل بياناتها في أي وقت وعلى أي جهاز آخر.", style = MaterialTheme.typography.bodySmall, color = SoftTheme.SoftGray)

            importErrorMessage?.let { errorMsg ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color(0xFF822727)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = errorMsg,
                        color = androidx.compose.ui.graphics.Color.White,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        exportPasswordInput = ""
                        showExportPasswordDialog = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.SoftPink),
                    modifier = Modifier.weight(1f).testTag("export_backup_btn")
                ) {
                    Text("تصدير مشفر 🔒📤", fontSize = 13.sp)
                }

                OutlinedButton(
                    onClick = {
                        importLauncher.launch(arrayOf("*/*", "application/octet-stream"))
                    },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = SoftTheme.MintTeal),
                    border = BorderStroke(1.dp, SoftTheme.MintTeal),
                    modifier = Modifier.weight(1f).testTag("import_backup_btn")
                ) {
                    Text("استعادة مشفرة 📥", fontSize = 13.sp)
                }
            }
        }
    }

    if (showExportPasswordDialog) {
        AlertDialog(
            onDismissRequest = { showExportPasswordDialog = false },
            title = { Text("🔒 تعيين كلمة سر للنسخة الاحتياطية", fontWeight = FontWeight.Bold, color = SoftTheme.SoftPink) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "أدخلي كلمة سر لحماية نسختك الاحتياطية. ستُطلب منكِ هذه الكلمة عند استعادة البيانات على هذا الجهاز أو أي جهاز آخر.",
                        style = MaterialTheme.typography.bodySmall,
                        color = SoftTheme.TextWhite
                    )
                    OutlinedTextField(
                        value = exportPasswordInput,
                        onValueChange = { exportPasswordInput = it },
                        label = { Text("كلمة سر النسخة الاحتياطية 🔑") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SoftTheme.SoftPink,
                            unfocusedBorderColor = SoftTheme.SoftGray,
                            focusedLabelColor = SoftTheme.SoftPink
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("export_password_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (exportPasswordInput.isBlank()) {
                            Toast.makeText(context, "يرجى إدخال كلمة سر لحماية النسخة الاحتياطية", Toast.LENGTH_SHORT).show()
                        } else {
                            pendingExportPassword = exportPasswordInput
                            showExportPasswordDialog = false
                            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                            exportLauncher.launch("woman_companion_backup_$timeStamp.db")
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.SoftPink)
                ) {
                    Text("تأكيد وتحديد المكان 📤", color = androidx.compose.ui.graphics.Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showExportPasswordDialog = false }) {
                    Text("إلغاء", color = SoftTheme.SoftGray)
                }
            },
            containerColor = SoftTheme.DeepSlate
        )
    }

    if (showImportPasswordDialog) {
        AlertDialog(
            onDismissRequest = { showImportPasswordDialog = false },
            title = { Text("🔑 أدخلي كلمة سر النسخة الاحتياطية", fontWeight = FontWeight.Bold, color = SoftTheme.MintTeal) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "يرجى إدخال كلمة السر التي قمتِ بتحديدها عند تصدير النسخة الاحتياطية لفك تشفيرها واستعادتها.",
                        style = MaterialTheme.typography.bodySmall,
                        color = SoftTheme.TextWhite
                    )
                    OutlinedTextField(
                        value = importPasswordInput,
                        onValueChange = { importPasswordInput = it },
                        label = { Text("كلمة سر النسخة 🔑") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SoftTheme.MintTeal,
                            unfocusedBorderColor = SoftTheme.SoftGray,
                            focusedLabelColor = SoftTheme.MintTeal
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("import_password_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val uri = pendingImportUri
                        showImportPasswordDialog = false
                        if (uri != null && importPasswordInput.isNotBlank()) {
                            val (isValid, tempFile) = BackupManager.validateBackupFile(context, uri, importPasswordInput)
                            if (isValid && tempFile != null) {
                                pendingImportFile = tempFile
                                importErrorMessage = null
                                showConfirmImportDialog = true
                            } else {
                                importErrorMessage = "عفواً، كلمة السر غير صحيحة أو ملف النسخة غير صالح."
                                Toast.makeText(context, "فشل فك تشفير النسخة الاحتياطية.", Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.MintTeal)
                ) {
                    Text("فك التشفير والتحقق 🔓", color = androidx.compose.ui.graphics.Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportPasswordDialog = false }) {
                    Text("إلغاء", color = SoftTheme.SoftGray)
                }
            },
            containerColor = SoftTheme.DeepSlate
        )
    }

    if (showConfirmImportDialog && pendingImportFile != null) {
        AlertDialog(
            onDismissRequest = {
                showConfirmImportDialog = false
                pendingImportFile?.delete()
                pendingImportFile = null
            },
            title = { Text("📥 تأكيد استعادة النسخة الاحتياطية", fontWeight = FontWeight.Bold, color = SoftTheme.MintTeal) },
            text = {
                Text(
                    "هيتم استبدال البيانات الحالية بالكامل، متأكدة؟\n\nتأكدي من حفظ أية بيانات هامة حالية قبل الاستبدال.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SoftTheme.TextWhite
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val file = pendingImportFile
                        showConfirmImportDialog = false
                        if (file != null) {
                            val success = BackupManager.restoreBackupFile(context, file)
                            if (success) {
                                Toast.makeText(context, "تمت استعادة البيانات بنجاح! يتم إعادة تحميل التطبيق...", Toast.LENGTH_LONG).show()
                                (context as? android.app.Activity)?.recreate()
                            } else {
                                Toast.makeText(context, "حدث خطأ أثناء استعادة الملف.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.MintTeal)
                ) {
                    Text("تأكيد الاستبدال", color = androidx.compose.ui.graphics.Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showConfirmImportDialog = false
                    pendingImportFile?.delete()
                    pendingImportFile = null
                }) {
                    Text("إلغاء", color = SoftTheme.SoftGray)
                }
            },
            containerColor = SoftTheme.DeepSlate
        )
    }
}
