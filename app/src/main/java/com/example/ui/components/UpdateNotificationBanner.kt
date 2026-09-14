package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.SystemUpdate
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
import com.example.ui.SoftTheme
import com.example.util.GitHubReleaseInfo
import com.example.util.UpdateStatus
import com.example.viewmodel.WomanCompanionViewModel

@Composable
fun UpdateNotificationBanner(
    viewModel: WomanCompanionViewModel,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val updateStatus by viewModel.appUpdateStatus.collectAsStateWithLifecycle()
    var isDismissed by remember { mutableStateOf(false) }

    val releaseInfo = (updateStatus as? UpdateStatus.UpdateAvailable)?.release

    AnimatedVisibility(
        visible = releaseInfo != null && !isDismissed,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        if (releaseInfo != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
                shape = RoundedCornerShape(14.dp),
                modifier = modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .testTag("update_notification_banner")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Icon(
                        imageVector = Icons.Default.SystemUpdate,
                        contentDescription = null,
                        tint = SoftTheme.MintTeal,
                        modifier = Modifier.size(24.dp)
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "يتوفر تحديث جديد: ${releaseInfo.tagName} 🌸",
                            fontWeight = FontWeight.Bold,
                            color = SoftTheme.TextWhite,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "اضغطي للتحديث والحفاظ على كافة بياناتك بأمان.",
                            color = SoftTheme.SoftGray,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
                                !context.packageManager.canRequestPackageInstalls()
                            ) {
                                Toast.makeText(context, "الرجاء تفعيل إذن تثبيت التطبيقات أولاً", Toast.LENGTH_LONG).show()
                                val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                                    data = Uri.parse("package:${context.packageName}")
                                }
                                context.startActivity(intent)
                            } else {
                                onNavigateToSettings()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.MintTeal),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "تحديث الآن",
                            color = SoftTheme.DeepSlate,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(
                        onClick = { isDismissed = true },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "إغلاق الإشعار",
                            tint = SoftTheme.SoftGray
                        )
                    }
                }
            }
        }
    }
}
