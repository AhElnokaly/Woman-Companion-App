package com.example.viewmodel

import android.util.Log
import com.example.ui.FetalStandardData
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun WomanCompanionViewModel.generateDoctorReportText(daysRange: Int = 30): String {
    markDoctorReportGenerated()

    val preg = pregnancyState.value
    val name = preg?.motherName ?: "السيدة العزيزة"
    val nickname = preg?.nickname ?: name
    val isPreg = preg?.isPregnant == true
    val dateStr = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(Date())

    val cutoffMs = System.currentTimeMillis() - (daysRange.toLong() * 24 * 60 * 60 * 1000L)

    val sb = StringBuilder()
    sb.appendLine("🩺 =========================================")
    sb.appendLine("   تقرير زيارة الطبيبة - تطبيق رفيقة المرأة جوري")
    sb.appendLine("=========================================")
    sb.appendLine("📅 تاريخ التقرير: $dateStr (النطاق: آخر $daysRange يوم)")
    sb.appendLine("👤 الاسم: $name ($nickname)")
    
    if (isPreg) {
        val prog = getPregnancyProgression()
        sb.appendLine("👶 الحالة: حامل في الأسبوع ${prog?.weeks ?: 1}")
        if (prog?.dueDate != null) {
            val dueStr = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(Date(prog.dueDate))
            sb.appendLine("🗓️ تاريخ الولادة المتوقع: $dueStr")
        }
        if (preg?.prePregnancyWeight != null) {
            sb.appendLine("⚖️ الوزن قبل الحمل: ${preg.prePregnancyWeight} كجم")
        }
    } else {
        sb.appendLine("🌸 الحالة: متابعة الدورة الشهرية")
    }

    // Chronic conditions
    val chronics = mutableListOf<String>()
    if (preg?.hasHighBp == true) chronics.add("ارتفاع ضغط الدم")
    if (preg?.hasLowBp == true) chronics.add("انخفاض ضغط الدم")
    if (preg?.hasDiabetes == true) chronics.add("السكري")
    if (!preg?.chronicOthers.isNullOrBlank()) chronics.add(preg.chronicOthers)
    if (chronics.isNotEmpty()) {
        sb.appendLine("⚠️ الأمراض/الحالات المزمنة: ${chronics.joinToString("، ")}")
    } else {
        sb.appendLine("💚 لا توجد حالات مزمنة مسجلة")
    }

    // Period logs summary & cycle irregularity
    val periodLogs = periodLogsState.value.filter { it.startDate >= cutoffMs }.sortedByDescending { it.startDate }
    if (periodLogs.isNotEmpty()) {
        sb.appendLine("\n🩸 --- سجلات الدورة الشهرية (خلال $daysRange يوم) ---")
        val sdf = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
        periodLogs.take(5).forEach { log ->
            val start = sdf.format(Date(log.startDate))
            val end = if (log.endDate != null) sdf.format(Date(log.endDate)) else "مستمرة"
            sb.appendLine(" • من $start إلى $end (الشدة: ${log.flowIntensity})")
        }
        val isIrregular = checkCycleIrregularity()
        if (isIrregular) {
            sb.appendLine(" ⚠️ ملاحظة الانتظام: يرجى العلم بوجود تباين في أطوال الدورات الأخيرة بنسبة تعادل أو تزيد عن 7 أيام مقارنة بالمعدل الشخصي.")
        } else {
            sb.appendLine(" ✨ الانتظام: الدورة ضمن المعدل الطبيعي والمستقر")
        }
    }

    // Fetal logs if pregnant
    if (isPreg) {
        val fetalLogs = allFetalGrowthLogsState.value
        if (fetalLogs.isNotEmpty()) {
            sb.appendLine("\n👶 --- سجلات نمو الجنين والقياسات ---")
            val latest = fetalLogs.maxByOrNull { it.pregnancyWeek }
            if (latest != null) {
                sb.appendLine(" • أحدث قياس (الأسبوع ${latest.pregnancyWeek}): الوزن ${latest.weightGrams} جرام، الطول ${latest.lengthCm} سم")
                val dev = FetalStandardData.calculateWeightDeviation(latest.pregnancyWeek, latest.weightGrams)
                val devText = if (dev >= 0) "+${String.format(Locale.US, "%.1f", dev)}%" else "${String.format(Locale.US, "%.1f", dev)}%"
                sb.appendLine(" • الانحراف عن النمو القياسي: $devText")
            }
        }
    }

    // Medications & Adherence
    val medications = allMedicationsState.value
    if (medications.isNotEmpty()) {
        sb.appendLine("\n💊 --- الأدوية والفيتامينات والالتزام ---")
        medications.forEach { med ->
            val status = if (med.isActive) "نشط" else "متوقف"
            sb.appendLine(" • ${med.name} (${med.dosage}) - $status - المتبقي: ${med.remainingQuantity} جرعة")
        }
    }

    // Vitals / Daily Logs summary
    sb.appendLine("\n📊 --- المؤشرات الحيوية والنشاط اليومي ---")
    val avgWater = todayWaterLogState.value?.amountMl ?: 0
    val steps = todayStepLogState.value?.steps ?: 0
    sb.appendLine(" • شرب الماء اليومي (المعدل): $avgWater مل")
    sb.appendLine(" • النشاط والخطوات اليومية: $steps خطوة")

    sb.appendLine("\n=========================================")
    sb.appendLine("✨ تم توليد التقرير تلقائياً بواسطة رفيقة المرأة جوري")
    return sb.toString()
}

fun WomanCompanionViewModel.generateMedicalReportText(): String {
    val pregnancy = pregnancyState.value
    val bpLogs = bloodPressureLogsState.value.take(15)
    val symptoms = symptomLogsState.value.take(15)
    val medications = activeMedicationsState.value
    val adherenceLogs = allMedicationAdherenceLogsState.value
    val fetalGrowthLogs = allFetalGrowthLogsState.value.take(10)
    val sdf = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())

    val sb = StringBuilder()
    sb.append("📋 **تقرير المتابعة الطبية الشامل - تطبيق رفيقة المرأة**\n")
    sb.append("تاريخ استخراج التقرير: ${sdf.format(Date())}\n")
    sb.append("═══════════════════════════════════════════\n\n")

    // 1. البيانات الأساسية
    sb.append("👤 **البيانات الشخصية والحالة:**\n")
    sb.append("- الاسم: ${pregnancy?.motherName ?: "الأم"}\n")
    if (pregnancy?.age != null) sb.append("- العمر: ${pregnancy.age} سنة\n")
    if (pregnancy?.isPregnant == true) {
        val prog = getPregnancyProgression()
        sb.append("- الحالة: حامل بالأسبوع ${prog?.weeks ?: 0} (${prog?.trimester ?: 1} الثلث)\n")
        if (pregnancy.dueDate != null) {
            sb.append("- الموعد المتوقع للولادة: ${sdf.format(Date(pregnancy.dueDate))}\n")
        }
        if (pregnancy.babyName != null) sb.append("- اسم الجنين: ${pregnancy.babyName}\n")
    } else {
        sb.append("- الحالة: متابعة الدورة الشهرية والتخطيط الصحي\n")
    }
    sb.append("- الأمراض المزمنة: ")
    val chronicList = mutableListOf<String>()
    if (pregnancy?.hasHighBp == true) chronicList.add("ارتفاع ضغط الدم")
    if (pregnancy?.hasLowBp == true) chronicList.add("انخفاض ضغط الدم")
    if (pregnancy?.hasDiabetes == true) chronicList.add("السكري")
    if (!pregnancy?.chronicOthers.isNullOrBlank()) chronicList.add(pregnancy?.chronicOthers!!)
    if (chronicList.isEmpty()) sb.append("لا يوجد سجل لأمراض مزمنة\n\n") else sb.append("${chronicList.joinToString("، ")}\n\n")

    // 2. سجل قياسات ضغط الدم والنبض الأخيرة
    sb.append("🩺 **سجل قياسات ضغط الدم والنبض (آخر القراءات):**\n")
    if (bpLogs.isEmpty()) {
        sb.append("- لم يتم تسجيل قياسات ضغط دم مؤخراً.\n\n")
    } else {
        bpLogs.forEach { log ->
            val dateStr = sdf.format(Date(log.date))
            val pulseStr = log.pulse?.let { " (نبض: $it)" } ?: ""
            val noteStr = if (!log.notes.isNullOrBlank()) " [ملاحظة: ${log.notes}]" else ""
            sb.append("• $dateStr : ${log.systolic}/${log.diastolic} ملم زئبق$pulseStr$noteStr\n")
        }
        sb.append("\n")
    }

    // 3. سجل الأدوية والالتزام بالجرعات
    sb.append("💊 **الأدوية والمكملات الحالية والالتزام:**\n")
    if (medications.isEmpty()) {
        sb.append("- لا توجد أدوية أو مكملات مسجلة حالياً.\n\n")
    } else {
        medications.forEach { med ->
            val doc = if (!med.prescribedBy.isNullOrBlank()) " (موصوف من: د. ${med.prescribedBy})" else ""
            val stock = if (med.totalQuantity > 0) " [المتبقي: ${med.remainingQuantity}/${med.totalQuantity}]" else ""
            val medAdherence = adherenceLogs.filter { it.medicationId == med.id }
            val takenCount = medAdherence.count { it.status == "TAKEN" }
            val totalLogged = medAdherence.size
            val adhRate = if (totalLogged > 0) " - نسبة الالتزام: ${(takenCount * 100 / totalLogged)}%" else ""
            sb.append("• ${med.name} - ${med.dosage ?: "جرعة"} (${med.timesPerDay} مرات يومياً)$doc$stock$adhRate\n")
        }
        sb.append("\n")
    }

    // 4. الأعراض الصحية المسجلة
    sb.append("🌡️ **سجل الأعراض والملاحظات الصحية:**\n")
    if (symptoms.isEmpty()) {
        sb.append("- لم يتم تسجيل أية أعراض غير اعتيادية.\n\n")
    } else {
        symptoms.forEach { sym ->
            val dateStr = sdf.format(Date(sym.date))
            val notes = if (!sym.notes.isNullOrBlank()) " (${sym.notes})" else ""
            sb.append("• $dateStr : ${sym.symptom} [الشدة: ${sym.severity}/10]$notes\n")
        }
        sb.append("\n")
    }

    // 5. سجل نمو الجنين
    if (pregnancy?.isPregnant == true) {
        sb.append("📈 **متابعة نمو الجنين:**\n")
        if (fetalGrowthLogs.isNotEmpty()) {
            val latestFetal = fetalGrowthLogs.first()
            sb.append("- آخر قياس للجنين (أسبوع ${latestFetal.pregnancyWeek}): الوزن ${latestFetal.weightGrams} جم، الطول ${latestFetal.lengthCm} سم\n")
        }
        sb.append("\n")
    }

    sb.append("═══════════════════════════════════════════\n")
    sb.append("تم إعداد هذا التقرير آلياً بواسطة تطبيق رفيقة المرأة لمساندة الطبيبة في المتابعة الطبية الدقيقة 🌸")
    return sb.toString()
}

fun WomanCompanionViewModel.exportDataAsJson(): String {
    val root = JSONObject()
    root.put("version", 1)
    root.put("exportDate", System.currentTimeMillis())

    pregnancyState.value?.let { p ->
        val pObj = JSONObject()
        pObj.put("lastPeriodDate", p.lastPeriodDate ?: 0L)
        pObj.put("dueDate", p.dueDate ?: 0L)
        pObj.put("babyName", p.babyName ?: "")
        pObj.put("motherName", p.motherName ?: "")
        pObj.put("isPregnant", p.isPregnant)
        root.put("pregnancy", pObj)
    }

    val bpArray = JSONArray()
    bloodPressureLogsState.value.forEach { bp ->
        val obj = JSONObject()
        obj.put("systolic", bp.systolic)
        obj.put("diastolic", bp.diastolic)
        obj.put("pulse", bp.pulse ?: 0)
        obj.put("date", bp.date)
        obj.put("notes", bp.notes ?: "")
        bpArray.put(obj)
    }
    root.put("bloodPressureLogs", bpArray)

    val symArray = JSONArray()
    symptomLogsState.value.forEach { sym ->
        val obj = JSONObject()
        obj.put("symptom", sym.symptom)
        obj.put("severity", sym.severity)
        obj.put("date", sym.date)
        obj.put("notes", sym.notes ?: "")
        symArray.put(obj)
    }
    root.put("symptomLogs", symArray)

    val medArray = JSONArray()
    allMedicationsState.value.forEach { med ->
        val obj = JSONObject()
        obj.put("name", med.name)
        obj.put("dosage", med.dosage ?: "")
        obj.put("timesPerDay", med.timesPerDay)
        obj.put("prescribedBy", med.prescribedBy ?: "")
        obj.put("notes", med.notes ?: "")
        obj.put("totalQuantity", med.totalQuantity)
        obj.put("remainingQuantity", med.remainingQuantity)
        obj.put("isActive", med.isActive)
        medArray.put(obj)
    }
    root.put("medications", medArray)

    val periodArray = JSONArray()
    periodLogsState.value.forEach { per ->
        val obj = JSONObject()
        obj.put("startDate", per.startDate)
        obj.put("endDate", per.endDate ?: 0L)
        obj.put("flowIntensity", per.flowIntensity)
        obj.put("symptoms", per.symptoms)
        obj.put("painLevel", per.painLevel)
        obj.put("notes", per.notes ?: "")
        periodArray.put(obj)
    }
    root.put("periodLogs", periodArray)

    val apptArray = JSONArray()
    appointmentsState.value.forEach { appt ->
        val obj = JSONObject()
        obj.put("title", appt.title)
        obj.put("doctorName", appt.doctorName ?: "")
        obj.put("dateTime", appt.dateTime)
        obj.put("notes", appt.notes ?: "")
        obj.put("completed", appt.completed)
        apptArray.put(obj)
    }
    root.put("appointments", apptArray)

    return root.toString(2)
}

suspend fun WomanCompanionViewModel.importDataFromJson(jsonString: String): Result<Int> {
    return try {
        val root = JSONObject(jsonString)
        var importedCount = 0

        if (root.has("pregnancy")) {
            val pObj = root.getJSONObject("pregnancy")
            val lmp = pObj.optLong("lastPeriodDate", 0L).let { if (it > 0) it else null }
            setPregnancy(
                lastPeriodDate = lmp,
                preWeight = null,
                height = null
            )
            importedCount++
        }

        if (root.has("bloodPressureLogs")) {
            val bpArray = root.getJSONArray("bloodPressureLogs")
            for (i in 0 until bpArray.length()) {
                val obj = bpArray.getJSONObject(i)
                addBloodPressureLog(
                    systolic = obj.getInt("systolic"),
                    diastolic = obj.getInt("diastolic"),
                    pulse = obj.optInt("pulse").let { if (it > 0) it else null },
                    notes = obj.optString("notes", "").ifEmpty { null }
                )
                importedCount++
            }
        }

        if (root.has("symptomLogs")) {
            val symArray = root.getJSONArray("symptomLogs")
            for (i in 0 until symArray.length()) {
                val obj = symArray.getJSONObject(i)
                addSymptom(
                    symptom = obj.getString("symptom"),
                    severity = obj.optInt("severity", 1),
                    notes = obj.optString("notes", "").ifEmpty { null }
                )
                importedCount++
            }
        }

        if (root.has("medications")) {
            val medArray = root.getJSONArray("medications")
            for (i in 0 until medArray.length()) {
                val obj = medArray.getJSONObject(i)
                addMedication(
                    name = obj.getString("name"),
                    dosage = obj.optString("dosage", ""),
                    timesPerDay = obj.optInt("timesPerDay", 1),
                    prescby = obj.optString("prescribedBy", "").ifEmpty { null },
                    notes = obj.optString("notes", "").ifEmpty { null },
                    start = System.currentTimeMillis(),
                    totalQuantity = obj.optInt("totalQuantity", 0),
                    remainingQuantity = obj.optInt("remainingQuantity", 0)
                )
                importedCount++
            }
        }

        if (root.has("appointments")) {
            val apptArray = root.getJSONArray("appointments")
            for (i in 0 until apptArray.length()) {
                val obj = apptArray.getJSONObject(i)
                addAppointment(
                    title = obj.getString("title"),
                    dateTime = obj.getLong("dateTime"),
                    doctor = obj.optString("doctorName", "").ifEmpty { null },
                    notes = obj.optString("notes", "").ifEmpty { null }
                )
                importedCount++
            }
        }

        Result.success(importedCount)
    } catch (e: Exception) {
        Log.e("WomanCompanionVM", "Failed to import JSON backup", e)
        Result.failure(e)
    }
}
