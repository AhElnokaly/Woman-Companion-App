package com.example.ui

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.viewmodel.WomanCompanionViewModel

@Composable
fun AppLockScreen(
    viewModel: WomanCompanionViewModel,
    onSuccess: () -> Unit
) {
    val settings by viewModel.appLockSettingsState.collectAsStateWithLifecycle()
    var pinInput by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(SoftTheme.BackgroundBrush)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Lock,
                    contentDescription = "قفل التطبيق",
                    tint = SoftTheme.SoftPink,
                    modifier = Modifier.size(72.dp)
                )

                Text(
                    text = "رفيق المرأة 🌸",
                    style = MaterialTheme.typography.headlineMedium,
                    color = SoftTheme.TextWhite,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = if (settings?.isStealthModeEnabled == true) "تأكيد الهوية للوصول" else "الرجاء إدخال رمز المرور PIN لحماية خصوصيتك",
                    style = MaterialTheme.typography.bodyLarge,
                    color = SoftTheme.SoftGray,
                    textAlign = TextAlign.Center
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    repeat(4) { idx ->
                        val active = idx < pinInput.length
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(if (active) SoftTheme.SoftPink else SoftTheme.CardSlate)
                                .border(1.dp, SoftTheme.SoftGray, CircleShape)
                        )
                    }
                }

                if (showError) {
                    Text(
                        text = "رمز PIN غير صحيح، يرجى المحاولة مرة أخرى",
                        color = SoftTheme.RedDanger,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Custom keypad
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.width(280.dp)
                ) {
                    val rows = listOf(
                        listOf("1", "2", "3"),
                        listOf("4", "5", "6"),
                        listOf("7", "8", "9"),
                        listOf("مسح", "0", "موافق")
                    )
                    rows.forEach { row ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            row.forEach { char ->
                                Button(
                                    onClick = {
                                        showError = false
                                        when (char) {
                                            "مسح" -> {
                                                if (pinInput.isNotEmpty()) pinInput = pinInput.dropLast(1)
                                            }
                                            "موافق" -> {
                                                if (viewModel.unlockApp(pinInput)) {
                                                    onSuccess()
                                                } else {
                                                    showError = true
                                                    pinInput = ""
                                                }
                                            }
                                            else -> {
                                                if (pinInput.length < 4) {
                                                    pinInput += char
                                                    if (pinInput.length == 4) {
                                                        if (viewModel.unlockApp(pinInput)) {
                                                            onSuccess()
                                                        } else {
                                                            showError = true
                                                            pinInput = ""
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(60.dp)
                                        .testTag("keypad_$char"),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = SoftTheme.CardSlate,
                                        contentColor = SoftTheme.TextWhite
                                    ),
                                    shape = RoundedCornerShape(16.dp),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text(
                                        text = char,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsScreen(
    viewModel: WomanCompanionViewModel,
    onNavigateBack: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val settings by viewModel.appLockSettingsState.collectAsStateWithLifecycle()
    val savedApiKey by viewModel.apiKeyFlow.collectAsStateWithLifecycle(initialValue = null)
    val savedBaseUrl by viewModel.apiBaseUrlFlow.collectAsStateWithLifecycle(initialValue = "https://generativelanguage.googleapis.com/")
    val savedModelName by viewModel.modelNameFlow.collectAsStateWithLifecycle(initialValue = "gemini-3.5-flash")

    var pinCodeInput by remember { mutableStateOf("") }
    var isLockEnabled by remember { mutableStateOf(false) }
    var isStealthEnabled by remember { mutableStateOf(false) }
    var companionNameInput by remember { mutableStateOf("جوري") }
    var stepTargetInput by remember { mutableStateOf("6000") }
    var gitHubUrlInput by remember { mutableStateOf("https://raw.githubusercontent.com/your_username/your_repo/main/matrix.json") }
    var userApiKeyInput by remember { mutableStateOf("") }
    var userApiBaseUrlInput by remember { mutableStateOf("https://generativelanguage.googleapis.com/") }
    var userModelNameInput by remember { mutableStateOf("gemini-3.5-flash") }
    var isDarkModeLocal by remember { mutableStateOf(true) }
    var isApiKeySavedShow by remember { mutableStateOf(false) }

    val syncStatus by viewModel.gitHubSyncStatus.collectAsStateWithLifecycle()

    LaunchedEffect(savedApiKey) {
        savedApiKey?.let {
            userApiKeyInput = it
        }
    }

    LaunchedEffect(savedBaseUrl) {
        userApiBaseUrlInput = savedBaseUrl
    }

    LaunchedEffect(savedModelName) {
        userModelNameInput = savedModelName
    }

    LaunchedEffect(settings) {
        settings?.let {
            isLockEnabled = it.isLockEnabled
            isStealthEnabled = it.isStealthModeEnabled
            pinCodeInput = it.pinHash ?: ""
            companionNameInput = it.companionName
            stepTargetInput = it.dailyStepTarget.toString()
            gitHubUrlInput = it.gitHubRepoUrl ?: "https://raw.githubusercontent.com/your_username/your_repo/main/matrix.json"
            isDarkModeLocal = it.isDarkMode
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(SoftTheme.DeepSlate)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع", tint = SoftTheme.TextWhite)
                }
                Text("الإعدادات والأمان ⚙️", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = SoftTheme.TextWhite)
            }

            // Exact Alarm Permission Card (Android 12+)
            val alarmManager = remember { context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager }
            val canScheduleExact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                alarmManager?.canScheduleExactAlarms() ?: true
            } else {
                true
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth(),
                border = androidx.compose.foundation.BorderStroke(1.dp, if (canScheduleExact) SoftTheme.MintTeal.copy(alpha = 0.3f) else SoftTheme.GoldFasting)
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("إذن المنبهات الدقيقة ⏰", fontWeight = FontWeight.Bold, color = SoftTheme.SoftPink)
                        Text(
                            if (canScheduleExact) "مفعّل ✅" else "معطّل ⚠️",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (canScheduleExact) SoftTheme.MintTeal else SoftTheme.GoldFasting
                        )
                    }
                    Text(
                        if (canScheduleExact)
                            "تذكيرات الأدوية والمواعيد ستصلكِ في وقتها المظبوط بالدقيقة."
                        else
                            "إذن المنبه الدقيق غير مفعّل، مما قد يؤدي لتأخير إشعارات الأدوية والمواعيد من النظام. يرجى تفعيله.",
                        style = MaterialTheme.typography.bodySmall,
                        color = SoftTheme.SoftGray
                    )
                    if (!canScheduleExact) {
                        Button(
                            onClick = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                    try {
                                        val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                                            data = Uri.parse("package:${context.packageName}")
                                        }
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        val intent = Intent(Settings.ACTION_SETTINGS)
                                        context.startActivity(intent)
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.GoldFasting),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("تفعيل إذن المنبهات الدقيقة الان ⚙️", color = androidx.compose.ui.graphics.Color.White)
                        }
                    }
                }
            }

            // Gemini API Key Card
            Card(
                colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("إعدادات مفتاح Gemini AI (الأمان والحماية) 🔑", fontWeight = FontWeight.Bold, color = SoftTheme.SoftPink)
                    Text("يمكنك هنا إضافة أو تعديل مفتاح Gemini API الخاص بك لتشغيل المساعد الذكي بكل خصوصية، وسيتم حفظه مشفراً بالكامل داخل الجهاز.", style = MaterialTheme.typography.bodySmall, color = SoftTheme.SoftGray)

                    OutlinedTextField(
                        value = userApiKeyInput,
                        onValueChange = { userApiKeyInput = it },
                        label = { Text("مفتاح Gemini API Key (AI_STUDIO_KEY)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = userApiBaseUrlInput,
                        onValueChange = { userApiBaseUrlInput = it },
                        label = { Text("رابط API Base URL") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = userModelNameInput,
                        onValueChange = { userModelNameInput = it },
                        label = { Text("اسم الموديل (مثال: gemini-3.5-flash)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Button(
                        onClick = {
                            viewModel.saveApiKey(userApiKeyInput, userApiBaseUrlInput, userModelNameInput)
                            isApiKeySavedShow = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.SoftPink),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("حفظ المفتاح والموديل 💾")
                    }

                    if (isApiKeySavedShow) {
                        Text("تم حفظ المفتاح والموديل بنجاح مشفراً! ✨", color = SoftTheme.MintTeal, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            // Privacy & Security Card
            Card(
                colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("حماية الخصوصية والقفل 🔒", fontWeight = FontWeight.Bold, color = SoftTheme.SoftPink)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("تفعيل قفل التطبيق بـ PIN", color = SoftTheme.TextWhite)
                        Switch(
                            checked = isLockEnabled,
                            onCheckedChange = { isLockEnabled = it }
                        )
                    }

                    if (isLockEnabled) {
                        OutlinedTextField(
                            value = pinCodeInput,
                            onValueChange = { if (it.length <= 4) pinCodeInput = it },
                            label = { Text("رمز المرور PIN (4 أرقام)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("وضع التخفي (Stealth Mode)", color = SoftTheme.TextWhite)
                                Text("إخفاء اسم التطبيق على الشاشة الرئيسية", style = MaterialTheme.typography.bodySmall, color = SoftTheme.SoftGray)
                            }
                            Switch(
                                checked = isStealthEnabled,
                                onCheckedChange = { isStealthEnabled = it }
                            )
                        }
                    }

                    Button(
                        onClick = {
                            viewModel.configureAppLock(
                                pin = pinCodeInput.ifEmpty { null },
                                isEnabled = isLockEnabled,
                                isStealth = isStealthEnabled
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.SoftPink),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("حفظ إعدادات الأمان")
                    }
                }
            }

            // Companion & Customization Card
            Card(
                colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("التخصيص والرفيقة 🌸", fontWeight = FontWeight.Bold, color = SoftTheme.SoftPink)

                    OutlinedTextField(
                        value = companionNameInput,
                        onValueChange = { companionNameInput = it },
                        label = { Text("اسم الرفيقة الذكية") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = stepTargetInput,
                        onValueChange = { stepTargetInput = it },
                        label = { Text("هدف الخطوات اليومي") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Button(
                        onClick = {
                            viewModel.configureAppLock(
                                pin = pinCodeInput.ifEmpty { null },
                                isEnabled = isLockEnabled,
                                isStealth = isStealthEnabled,
                                companionName = companionNameInput,
                                dailyStepTarget = stepTargetInput.toIntOrNull() ?: 6000
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.SoftPink),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("حفظ التخصيص")
                    }
                }
            }

            // Remote GitHub Matrix Card
            Card(
                colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("المزامن السحابي الذكي (Matrix.json) 🌐", fontWeight = FontWeight.Bold, color = SoftTheme.MintTeal)
                    Text(
                        "يمكنك مواءمة ومزامنة قواعد البيانات والحسابات المعقدة مع مستودعك الخاص على GitHub عبر رابط Matrix Raw.",
                        style = MaterialTheme.typography.bodySmall,
                        color = SoftTheme.SoftGray
                    )

                    OutlinedTextField(
                        value = gitHubUrlInput,
                        onValueChange = { gitHubUrlInput = it },
                        label = { Text("رابط ملف matrix.json (Raw JSON)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Button(
                        onClick = {
                            viewModel.configureAppLock(
                                pin = pinCodeInput.ifEmpty { null },
                                isEnabled = isLockEnabled,
                                isStealth = isStealthEnabled,
                                companionName = companionNameInput,
                                dailyStepTarget = stepTargetInput.toIntOrNull() ?: 6000,
                                gitHubRepoUrl = gitHubUrlInput
                            )
                            viewModel.syncJouriMatrix()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.MintTeal),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("مزامنة الآن من GitHub 🔄", color = SoftTheme.DeepSlate, fontWeight = FontWeight.Bold)
                    }

                    if (syncStatus != null) {
                        Text(syncStatus ?: "", color = SoftTheme.MintTeal, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            // Export & Backup Data Card
            Card(
                colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("تصدير واستعادة النسخ الاحتياطية 📦", fontWeight = FontWeight.Bold, color = SoftTheme.SoftPink)
                    Text("يمكنك طباعة أو حفظ ملخص طبي كامل ببياناتك وسجلاتك أو نسخها للاسترجاع.", style = MaterialTheme.typography.bodySmall, color = SoftTheme.SoftGray)

                    Button(
                        onClick = {
                            val backupJson = viewModel.exportMaonatyBackup()
                            try {
                                val sendIntent = android.content.Intent().apply {
                                    action = android.content.Intent.ACTION_SEND
                                    putExtra(android.content.Intent.EXTRA_TEXT, backupJson)
                                    type = "text/plain"
                                }
                                context.startActivity(android.content.Intent.createChooser(sendIntent, "تصدير النسخة الاحتياطية"))
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.SoftPink),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("تصدير النسخة الاحتياطية (JSON) 📤")
                    }
                }
            }

            // Factory Reset / Nuke Card
            Card(
                colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("حذف جميع البيانات (Factory Reset) ⚠️", fontWeight = FontWeight.Bold, color = SoftTheme.RedDanger)
                    Text(
                        "تطبيق رفيق المرأة يعمل بشكل أوفلاين بالكامل. نسيان رمز الـ PIN أو حذف التطبيق سيؤدي لضياع بياناتك المكتوبة. يمكنك تصفير كافة السجلات الحالية من هنا.",
                        style = MaterialTheme.typography.bodySmall,
                        color = SoftTheme.SoftGray
                    )

                    Button(
                        onClick = { viewModel.factoryReset() },
                        colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.RedDanger),
                        modifier = Modifier.fillMaxWidth().testTag("factory_reset_btn")
                    ) {
                        Text("مسح كافة البيانات نهائياً")
                    }
                }
            }
        }
    }
}
