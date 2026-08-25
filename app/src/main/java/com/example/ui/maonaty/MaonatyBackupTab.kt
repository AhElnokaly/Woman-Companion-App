package com.example.ui.maonaty

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.SoftTheme
import com.example.viewmodel.*

@Composable
fun BackupTab(
    viewModel: WomanCompanionViewModel,
    clipboardManager: ClipboardManager,
    context: Context
) {
    var backupText by remember { mutableStateOf("") }
    var restoreText by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        backupText = viewModel.exportMaonatyBackup()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("تصدير النسخة الاحتياطية لمؤونتي 🗄️", fontWeight = FontWeight.Bold, color = SoftTheme.TextWhite, fontSize = 14.sp)
                Text(
                    "انسخي الكود البرمجي أدناه واحتفظي به في أي ملف نصي أو جدول Excel/Sheets لاسترجاعه في أي وقت لاحق بالكامل:",
                    fontSize = 11.sp,
                    color = SoftTheme.SoftGray
                )

                OutlinedTextField(
                    value = backupText,
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SoftTheme.SoftPink,
                        unfocusedBorderColor = SoftTheme.SoftGray,
                        focusedTextColor = SoftTheme.SoftGray,
                        unfocusedTextColor = SoftTheme.SoftGray
                    ),
                    shape = RoundedCornerShape(10.dp),
                    textStyle = TextStyle(fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                )

                Button(
                    onClick = {
                        if (backupText.isNotEmpty()) {
                            clipboardManager.setText(AnnotatedString(backupText))
                            ScaffoldMessengerHelper.showToast(context, "تم نسخ كود النسخة الاحتياطية للحافظة بنجاح! 📋")
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.SoftPink),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("نسخ كود النسخ الاحتياطي 📋", fontWeight = FontWeight.Bold)
                }
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("استرجاع كود النسخ الاحتياطي 📥", fontWeight = FontWeight.Bold, color = SoftTheme.TextWhite, fontSize = 14.sp)
                Text(
                    "ألصقي الكود الاحتياطي الخاص بك هنا لاسترداد مخزون المطبخ والمشتريات والمهام المنزلية كاملة دفعة واحدة:",
                    fontSize = 11.sp,
                    color = SoftTheme.SoftGray
                )

                OutlinedTextField(
                    value = restoreText,
                    onValueChange = { restoreText = it },
                    placeholder = { Text("ألصقي الكود البرمجي النصي هنا...", color = SoftTheme.SoftGray, fontSize = 11.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SoftTheme.SoftPink,
                        unfocusedBorderColor = SoftTheme.SoftGray,
                        focusedTextColor = SoftTheme.TextWhite,
                        unfocusedTextColor = SoftTheme.TextWhite
                    ),
                    shape = RoundedCornerShape(10.dp),
                    textStyle = TextStyle(fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                )

                Button(
                    onClick = {
                        if (restoreText.isNotEmpty()) {
                            val success = viewModel.importMaonatyBackup(restoreText)
                            if (success) {
                                ScaffoldMessengerHelper.showToast(context, "تم استرجاع نسخة مؤونتي بالكامل بنجاح! 🎉📦")
                                restoreText = ""
                                backupText = viewModel.exportMaonatyBackup()
                            } else {
                                ScaffoldMessengerHelper.showToast(context, "فشل استرجاع الكود. تأكدي من سلامة كود التصدير الملصق! ⚠️")
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.MintTeal),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp), tint = SoftTheme.DeepSlate)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("تأكيد استرجاع البيانات 📥", fontWeight = FontWeight.Bold, color = SoftTheme.DeepSlate)
                }
            }
        }
    }
}
