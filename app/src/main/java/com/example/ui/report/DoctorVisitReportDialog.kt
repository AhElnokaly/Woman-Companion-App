package com.example.ui.report

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.SoftTheme

/**
 * نافذة عرض ومشاركة التقرير الطبي الشامل لزيارة الطبيب (Doctor Visit Report)
 */
@Composable
fun DoctorVisitReportDialog(
    reportText: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.88f)
                .testTag("doctor_visit_report_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SoftTheme.CardDark),
            border = androidx.compose.foundation.BorderStroke(1.dp, SoftTheme.CardBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("🩺", fontSize = 24.sp)
                        Column {
                            Text(
                                text = "تقرير المتابعة الطبية الشامل",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = SoftTheme.TextWhite
                            )
                            Text(
                                text = "ملخص جاهز ومخصص لزيارتكِ القادمة للطبيب",
                                style = MaterialTheme.typography.bodySmall,
                                color = SoftTheme.SoftGray,
                                fontSize = 11.sp
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = SoftTheme.SoftGray)
                    }
                }

                HorizontalDivider(color = SoftTheme.DeepSlate, thickness = 1.dp)

                // Report Preview Paper Box
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(SoftTheme.DeepSlate)
                        .border(1.dp, SoftTheme.CardBorder, RoundedCornerShape(16.dp))
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = reportText,
                        color = SoftTheme.TextWhite,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 20.sp,
                            fontSize = 12.sp
                        )
                    )
                }

                // Actions Footer (Share / Print intent)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            shareReport(context, reportText)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.PrimaryPink),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("share_report_btn")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, tint = SoftTheme.DeepSlate)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "مشاركة التقرير للطبيب 📤",
                            color = SoftTheme.DeepSlate,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            copyOrPrintReport(context, reportText)
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = SoftTheme.MintTeal),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SoftTheme.MintTeal),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.testTag("copy_report_btn")
                    ) {
                        Icon(Icons.Default.Print, contentDescription = null, tint = SoftTheme.MintTeal)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("نسخ / طباعة 📋", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

private fun shareReport(context: Context, text: String) {
    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_SUBJECT, "تقرير المتابعة الطبية - رفيقة المرأة جوري")
        putExtra(Intent.EXTRA_TEXT, text)
        type = "text/plain"
    }
    val shareIntent = Intent.createChooser(sendIntent, "مشاركة تقرير المتابعة مع طبيبكِ 🩺")
    context.startActivity(shareIntent)
}

private fun copyOrPrintReport(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager
    val clip = android.content.ClipData.newPlainText("Medical Report", text)
    clipboard?.setPrimaryClip(clip)
    android.widget.Toast.makeText(context, "تم نسخ التقرير للحافظة بنجاح، يمكنكِ لصقه أو طباعته 📄", android.widget.Toast.LENGTH_SHORT).show()
}
