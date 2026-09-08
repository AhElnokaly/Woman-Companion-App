package com.example.ui.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.BuildConfig
import com.example.ui.theme.SoftTheme
import com.example.util.GitHubReleaseInfo
import com.example.util.UpdateStatus
import com.example.viewmodel.WomanCompanionViewModel

@Composable
fun AppUpdatesCard(
    viewModel: WomanCompanionViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val updateStatus by viewModel.appUpdateStatus.collectAsStateWithLifecycle()
    var showChangelogDialog by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
        shape = RoundedCornerShape(20.dp),
        modifier = modifier.fillMaxWidth().testTag("app_updates_card")
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
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
                    Icon(
                        imageVector = Icons.Filled.SystemUpdate,
                        contentDescription = "تحديثات التطبيق",
                        tint = SoftTheme.MintTeal,
                        modifier = Modifier.size(24.dp)
                    )
                    Column {
                        Text(
                            text = "تحديثات التطبيق 🚀",
                            fontWeight = FontWeight.Bold,
                            color = SoftTheme.MintTeal,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "الإصدار الحالي: v${BuildConfig.VERSION_NAME}",
                            color = SoftTheme.SoftGray,
                            fontSize = 12.sp
                        )
                    }
                }

                // مؤشر أو بادج
                when (updateStatus) {
                    is UpdateStatus.UpdateAvailable -> {
                        Surface(
                            color = SoftTheme.SoftPink.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "تحديث متوفر!",
                                color = SoftTheme.SoftPink,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                    else -> {}
                }
            }

            Text(
                text = "يتم فحص أحدث الإصدارات مباشرة من مستودع GitHub الرسمي (Woman-Companion-App) وتنزيل التحديث وتثبيته بضغطة زر دون مسح بياناتك.",
                style = MaterialTheme.typography.bodySmall,
                color = SoftTheme.SoftGray,
                lineHeight = 18.sp
            )

            // حالة التحديث التفاعلية
            when (val status = updateStatus) {
                is UpdateStatus.Idle, is UpdateStatus.NoUpdateAvailable -> {
                    if (status is UpdateStatus.NoUpdateAvailable) {
                        Surface(
                            color = SoftTheme.MintTeal.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.CheckCircle,
                                    contentDescription = null,
                                    tint = SoftTheme.MintTeal,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "أنتِ تستخدمين أحدث إصدار متاح حالياً 🌸",
                                    color = SoftTheme.MintTeal,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    Button(
                        onClick = { viewModel.checkForAppUpdates(manual = true) },
                        colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.MintTeal),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("check_updates_btn")
                    ) {
                        Icon(Icons.Filled.Refresh, contentDescription = null, tint = SoftTheme.DeepSlate, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("التحقق من وجود تحديثات الآن", color = SoftTheme.DeepSlate, fontWeight = FontWeight.Bold)
                    }
                }

                is UpdateStatus.Checking -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            color = SoftTheme.MintTeal,
                            strokeWidth = 2.5.dp,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "جارٍ الفحص عبر GitHub...",
                            color = SoftTheme.SoftGray,
                            fontSize = 13.sp
                        )
                    }
                }

                is UpdateStatus.UpdateAvailable -> {
                    val release = status.release
                    Surface(
                        color = SoftTheme.SoftPink.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "إصدار جديد متاح: ${release.tagName}",
                                    fontWeight = FontWeight.Bold,
                                    color = SoftTheme.TextWhite,
                                    fontSize = 14.sp
                                )
                                if (release.releaseNotes.isNotBlank()) {
                                    TextButton(
                                        onClick = { showChangelogDialog = true },
                                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text("ما الجديد؟ 📝", color = SoftTheme.SoftPink, fontSize = 12.sp)
                                    }
                                }
                            }

                            if (release.releaseTitle.isNotBlank() && release.releaseTitle != release.tagName) {
                                Text(
                                    text = release.releaseTitle,
                                    color = SoftTheme.SoftGray,
                                    fontSize = 12.sp
                                )
                            }

                            Button(
                                onClick = {
                                    // التحقق من صلاحية تثبيت الحزم على Android 8.0+
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
                                        !context.packageManager.canRequestPackageInstalls()
                                    ) {
                                        Toast.makeText(context, "الرجاء تفعيل إذن تثبيت التطبيقات أولاً", Toast.LENGTH_LONG).show()
                                        val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                                            data = Uri.parse("package:${context.packageName}")
                                        }
                                        context.startActivity(intent)
                                    } else {
                                        viewModel.downloadAndInstallAppUpdate(context, release)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.SoftPink),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().testTag("download_update_btn")
                            ) {
                                Icon(Icons.Filled.Download, contentDescription = null, tint = SoftTheme.TextWhite, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("تحميل وتثبيت التحديث الآن 📥", color = SoftTheme.TextWhite, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    if (showChangelogDialog) {
                        AlertDialog(
                            onDismissRequest = { showChangelogDialog = false },
                            title = {
                                Text("ما الجديد في ${release.tagName} 🌸", fontWeight = FontWeight.Bold, color = SoftTheme.SoftPink)
                            },
                            text = {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = release.releaseNotes.ifBlank { "تحسينات عامة وإصلاحات أداء." },
                                        color = SoftTheme.TextWhite,
                                        fontSize = 13.sp,
                                        lineHeight = 18.sp
                                    )
                                }
                            },
                            confirmButton = {
                                TextButton(onClick = { showChangelogDialog = false }) {
                                    Text("حسناً", color = SoftTheme.SoftPink)
                                }
                            },
                            containerColor = SoftTheme.DeepSlate
                        )
                    }
                }

                is UpdateStatus.Downloading -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("جارٍ تحميل التحديث من GitHub...", color = SoftTheme.TextWhite, fontSize = 12.sp)
                            Text("${status.progressPercent}%", color = SoftTheme.MintTeal, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        LinearProgressIndicator(
                            progress = { status.progressPercent / 100f },
                            color = SoftTheme.MintTeal,
                            trackColor = SoftTheme.CardSlate.copy(alpha = 0.5f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                        )
                    }
                }

                is UpdateStatus.ReadyToInstall -> {
                    Button(
                        onClick = { viewModel.launchUpdateInstaller(context, status.apkFile) },
                        colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.MintTeal),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("install_now_btn")
                    ) {
                        Icon(Icons.Filled.InstallMobile, contentDescription = null, tint = SoftTheme.DeepSlate, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("اكتمل التحميل! اضغطي للتثبيت الآن 🚀", color = SoftTheme.DeepSlate, fontWeight = FontWeight.Bold)
                    }
                }

                is UpdateStatus.Error -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = status.message,
                            color = SoftTheme.RedDanger,
                            fontSize = 12.sp
                        )
                        Button(
                            onClick = { viewModel.checkForAppUpdates(manual = true) },
                            colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.CardSlate),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("إعادة المحاولة 🔄", color = SoftTheme.TextWhite)
                        }
                    }
                }
            }
        }
    }
}
