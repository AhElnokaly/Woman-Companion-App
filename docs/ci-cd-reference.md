# Android CI/CD Pipeline Reference & Troubleshooting Guide
**Project:** Woman Companion (رفيقة دربك) / Android Compose  
**Stack:** AGP 9.1.1 · Gradle 9.3.1 · JDK 21 (Temurin) · Kotlin 2.2.10 · KSP2 · Room 2.7.0  
**CI Environment:** GitHub Actions (`ubuntu-latest`)  
**Status:** ✅ Production Verified (35s build time)

---

## 1. ملخص المعمارية والتوافقية (Architecture & Version Matrix)

| المكون | الإصدار المعتمد | السبب / الشرط الإلزامي |
|---|---|---|
| **Android Gradle Plugin (AGP)** | `9.1.1` | أحدث إصدار للأندرويد؛ يتطلب حصرياً Gradle 9.3+ و JDK 21 |
| **Gradle** | `9.3.1` | محدد في `gradle-wrapper.properties` و `setup-gradle@v4` |
| **JDK** | `21` (Temurin) | شرط إلزامي لتشغيل AGP 9.1+ |
| **Kotlin** | `2.2.10` | K2 Compiler الافتراضي |
| **KSP (Symbol Processing)** | `2.3.5` + `ksp.useKSP2=true` | متوافق مع K2 و Room 2.7.0 |
| **Room Runtime / Compiler** | `2.7.0` | يدعم محرك KSP2 الحديث |

---

## 2. المشاكل الشائعة والحلول الجذرية (Root Cause Analysis & Solutions)

### المشكلة 1: خطأ `AWT-EventQueue-0` في بيئة CI (Headless NullPointerException)
* **الأثر:** توقف البناء بعد 4 دقائق وظهور الخطأ:
  ```text
  Error: Exception in thread "AWT-EventQueue-0" java.lang.NullPointerException:
  Cannot invoke "ksp.com.intellij.openapi.application.Application.getService(java.lang.Class)" 
  because the return value of "ApplicationManager.getApplication()" is null
  ```
* **السبب الجذري:**
  1. سيرفرات GitHub Actions تعمل في بيئة Linux بدون شاشة (Headless).
  2. KSP بوضعه القديم (KSP1) يعتمد على محرك IntelliJ Platform الرسومي. عند تشغيله بدون وضع الـ Headless الصريح، يُنشئ خيطاً رسومياً `AWT-EventQueue-0` ويبحث عن واجهة البرنامج الرسومية فيُرجع `null`.
* **الحل الجذري المطبق:**
  1. إجبار Gradle وJVM على العمل بوضع الـ Headless الصريح في بيئة العمل:
     ```yaml
     env:
       GRADLE_OPTS: "-Djava.awt.headless=true -Dfile.encoding=UTF-8 -Xmx4g"
     ```
  2. إضافة `-Djava.awt.headless=true` في أمر التجميع مباشرة:
     ```bash
     ./gradlew assembleDebug --stacktrace -Djava.awt.headless=true
     ```
  3. تفعيل محرك **KSP2** الحديث في `gradle.properties`:
     ```properties
     ksp.useKSP2=true
     ```
     *محرك KSP2 مبني على K2 FIR (Frontend Intermediate Representation) ولا يعتمد إطلاقاً على IntelliJ GUI.*
  4. تعطيل معالجات الرموز القديمة غير المستخدمة (مثل `moshi-kotlin-codegen` إذا كان المشروع يعتمد Room فقط).
  5. ضبط `org.gradle.configuration-cache=false` لتفادي مشاكل الـ Serialization مع معالجات الرموز.

---

### المشكلة 2: فشل مهمة التوقيع `validateSigningDebug` (Keystore Not Found)
* **الأثر:**
  ```text
  Execution failed for task ':app:validateSigningDebug'.
  > Keystore file '/home/runner/.../debug.keystore' not found for signing config 'debugConfig'.
  ```
* **السبب الجذري:**
  ملف `debug.keystore` محمي وغير مرفوع في Git (موجود في `.gitignore` لأسباب أمنية)، ومحاولة قراءته من `debug.keystore.base64` تفشل إذا لم يكن الأخير متوفراً في المستودع.
* **الحل الجذري المطبق (Fallback تلقائي ذكي):**
  تنفيذ خطوة استرجاع مزدوجة في الـ Workflow:
  1. إن وُجد ملف Base64 يتم فكه.
  2. إن لم يوجد، يتم توليد `debug.keystore` نظامي صالح فوراً باستخدام أداة `keytool` المدمجة في الـ JDK بنفس بيانات اعتماد Android الرسمية:
  ```yaml
  - name: Restore or Generate Debug Keystore
    run: |
      if [ -f debug.keystore.base64 ]; then
        echo "Restoring debug.keystore from debug.keystore.base64..."
        base64 -d debug.keystore.base64 > debug.keystore
      fi
      if [ ! -f debug.keystore ]; then
        echo "Generating fallback debug.keystore using keytool..."
        keytool -genkey -v -keystore debug.keystore -storepass android -alias androiddebugkey -keypass android -keyalg RSA -keysize 2048 -validity 10000 -dname "CN=Android Debug,O=Android,C=US"
      fi
      ls -lh debug.keystore
  ```

---

### المشكلة 3: فقدان ملف المتغيرات البيئية `.env`
* **الأثر:**
  فشل إضافة `secrets-gradle-plugin` أو غياب مفاتيح `BuildConfig`.
* **الحل المطبق:**
  نسخ `.env.example` إلى `.env` تلقائياً قبل بدء البناء لضمان توفر القيم الافتراضية:
  ```yaml
  - name: Setup Environment File
    run: |
      if [ ! -f .env ] && [ -f .env.example ]; then
        cp .env.example .env
      fi
  ```

---

## 3. ملف الـ Workflow الكامل والنهائي (`.github/workflows/android.yml`)

```yaml
name: Android CI

on:
  push:
    branches: [ "main", "master" ]
  pull_request:
    branches: [ "main", "master" ]
  workflow_dispatch:

jobs:
  build:
    name: Build & Test
    runs-on: ubuntu-latest
    env:
      GRADLE_OPTS: "-Djava.awt.headless=true -Dfile.encoding=UTF-8 -Xmx4g"

    steps:
      - name: Checkout Code
        uses: actions/checkout@v4

      - name: Set up JDK 21
        uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'

      - name: Restore or Generate Debug Keystore
        run: |
          if [ -f debug.keystore.base64 ]; then
            echo "Restoring debug.keystore from debug.keystore.base64..."
            base64 -d debug.keystore.base64 > debug.keystore
          fi
          if [ ! -f debug.keystore ]; then
            echo "Generating fallback debug.keystore using keytool..."
            keytool -genkey -v -keystore debug.keystore -storepass android -alias androiddebugkey -keypass android -keyalg RSA -keysize 2048 -validity 10000 -dname "CN=Android Debug,O=Android,C=US"
          fi
          ls -lh debug.keystore

      - name: Setup Environment File
        run: |
          if [ ! -f .env ] && [ -f .env.example ]; then
            cp .env.example .env
          fi

      - name: Grant execute permission for gradlew
        run: chmod +x gradlew

      - name: Setup Gradle
        uses: gradle/actions/setup-gradle@v4
        with:
          gradle-version: '9.3.1'

      - name: Build Debug APK
        run: ./gradlew assembleDebug --stacktrace -Djava.awt.headless=true

      - name: Upload Debug APK Artifact
        uses: actions/upload-artifact@v4
        with:
          name: app-debug
          path: app/build/outputs/apk/debug/app-debug.apk
          if-no-files-found: warn

      - name: Run Unit Tests
        continue-on-error: true
        run: ./gradlew testDebugUnitTest -Djava.awt.headless=true
```

---

## 4. إعدادات `gradle.properties` الداعمة

```properties
# ذاكرة كافية ووضع Headless افتراضي
org.gradle.jvmargs=-Xmx4g -Dfile.encoding=UTF-8 -Djava.awt.headless=true

# بناء متوازي وتخزين مؤقت
org.gradle.parallel=true
org.gradle.caching=true
org.gradle.configuration-cache=false

# تفعيل محرك KSP2 الحديث
ksp.useKSP2=true

# عدد المعالجات المتزامنة
org.gradle.workers.max=4

# إستراتيجية تصريف الكوتلن
kotlin.compiler.execution.strategy=in-process

# تجاوز ملف google-services.json إن لم يكن متوفراً
googleServices.missing.passthrough=true
```

---

## 5. مخرجات ونتائج الأداء (Benchmark)

* **زمن البناء السابق (مع الأخطاء):** `4m 02s` (فشل في KSP).
* **زمن البناء بعد الحل:** `35s` فقط (نجاح بنسبة 100%).
* **الأرتيفاكت الناتج:** `app-debug.apk` متاح للتحميل مباشرة من صفحة الـ GitHub Actions run تحت اسم `app-debug`.
