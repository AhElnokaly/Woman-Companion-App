package com.example.util

import android.content.Context
import android.net.Uri
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import com.example.data.AppDatabase
import net.sqlcipher.database.SupportFactory
import java.io.File

object BackupManager {

    private const val TAG = "BackupManager"
    private fun ByteArray.toHexString(): String = joinToString("") { "%02x".format(it) }

    fun exportEncryptedBackup(context: Context, destinationUri: Uri, userPassphraseStr: String): Boolean {
        var tempExportFile: File? = null
        var openHelper: SupportSQLiteOpenHelper? = null
        return try {
            try {
                val db = AppDatabase.getDatabase(context)
                db.openHelper.writableDatabase.query("PRAGMA wal_checkpoint(FULL);")
            } catch (e: Exception) {
                AppLogger.w(TAG, "WAL checkpoint before export encountered warning", e)
            }

            val dbFile = context.getDatabasePath("woman_companion_database")
            if (!dbFile.exists()) return false

            tempExportFile = File(context.cacheDir, "temp_export_${System.currentTimeMillis()}.db")
            dbFile.copyTo(tempExportFile, overwrite = true)

            val currentDevicePassphrase = AppDatabase.getDatabasePassphrase(context.applicationContext)
            val factory = SupportFactory(currentDevicePassphrase)

            openHelper = factory.create(
                SupportSQLiteOpenHelper.Configuration.builder(context)
                    .name(tempExportFile.absolutePath)
                    .callback(object : SupportSQLiteOpenHelper.Callback(17) {
                        override fun onCreate(db: SupportSQLiteDatabase) {}
                        override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {}
                    })
                    .build()
            )

            val db = openHelper.writableDatabase
            val userPassBytes = userPassphraseStr.toByteArray(Charsets.UTF_8)
            val hexKey = userPassBytes.toHexString()
            db.execSQL("PRAGMA rekey = \"x'$hexKey'\";")

            val checkCursor = db.query("SELECT count(*) FROM sqlite_master")
            val isValid = checkCursor.moveToFirst()
            checkCursor.close()
            openHelper.close()

            if (!isValid) {
                tempExportFile.delete()
                return false
            }

            context.contentResolver.openOutputStream(destinationUri)?.use { outputStream ->
                tempExportFile.inputStream().use { inputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            tempExportFile.delete()
            true
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to export encrypted backup", e)
            try { openHelper?.close() } catch (_: Exception) {}
            tempExportFile?.let { if (it.exists()) it.delete() }
            false
        }
    }

    fun validateBackupFile(context: Context, backupUri: Uri, userPassphraseStr: String): Pair<Boolean, File?> {
        val tempFile = File(context.cacheDir, "temp_import_backup_${System.currentTimeMillis()}.db")
        var openHelper: SupportSQLiteOpenHelper? = null
        try {
            context.contentResolver.openInputStream(backupUri)?.use { inputStream ->
                tempFile.outputStream().use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            } ?: return Pair(false, null)

            val userPassBytes = userPassphraseStr.toByteArray(Charsets.UTF_8)
            var factory = SupportFactory(userPassBytes)

            var db: SupportSQLiteDatabase? = null
            var isValid = false

            try {
                openHelper = factory.create(
                    SupportSQLiteOpenHelper.Configuration.builder(context)
                        .name(tempFile.absolutePath)
                        .callback(object : SupportSQLiteOpenHelper.Callback(17) {
                            override fun onCreate(db: SupportSQLiteDatabase) {}
                            override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {}
                        })
                        .build()
                )
                db = openHelper.writableDatabase
                val cursor = db.query("SELECT count(*) FROM sqlite_master")
                isValid = cursor.moveToFirst()
                cursor.close()
            } catch (e: Exception) {
                // Fallback attempt for legacy device passphrase
                try { openHelper?.close() } catch (_: Exception) {}
                val devicePassphrase = AppDatabase.getDatabasePassphrase(context.applicationContext)
                factory = SupportFactory(devicePassphrase)
                openHelper = factory.create(
                    SupportSQLiteOpenHelper.Configuration.builder(context)
                        .name(tempFile.absolutePath)
                        .callback(object : SupportSQLiteOpenHelper.Callback(17) {
                            override fun onCreate(db: SupportSQLiteDatabase) {}
                            override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {}
                        })
                        .build()
                )
                db = openHelper.writableDatabase
                val cursor = db.query("SELECT count(*) FROM sqlite_master")
                isValid = cursor.moveToFirst()
                cursor.close()
            }

            if (isValid && db != null) {
                // Perform PRAGMA integrity_check before re-keying
                val integrityCursor = db.query("PRAGMA integrity_check;")
                var integrityOk = false
                if (integrityCursor.moveToFirst()) {
                    val integrityResult = integrityCursor.getString(0)
                    integrityOk = integrityResult.equals("ok", ignoreCase = true)
                }
                integrityCursor.close()

                if (!integrityOk) {
                    AppLogger.e(TAG, "Backup file integrity check failed")
                    try { openHelper?.close() } catch (_: Exception) {}
                    if (tempFile.exists()) tempFile.delete()
                    return Pair(false, null)
                }

                val targetDevicePassphrase = AppDatabase.getDatabasePassphrase(context.applicationContext)
                val hexKey = targetDevicePassphrase.toHexString()
                db.execSQL("PRAGMA rekey = \"x'$hexKey'\";")

                val verifyCursor = db.query("SELECT count(*) FROM sqlite_master")
                val isRekeyValid = verifyCursor.moveToFirst()
                verifyCursor.close()
                openHelper.close()

                if (isRekeyValid) {
                    return Pair(true, tempFile)
                }
            }

            try { openHelper?.close() } catch (_: Exception) {}
            if (tempFile.exists()) tempFile.delete()
            return Pair(false, null)
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to validate backup file", e)
            try { openHelper?.close() } catch (_: Exception) {}
            if (tempFile.exists()) tempFile.delete()
            return Pair(false, null)
        }
    }

    /**
     * Atomically restores database from validated temp file with full rollback protection.
     */
    fun restoreBackupFile(context: Context, validatedTempFile: File): Boolean {
        val dbFile = context.getDatabasePath("woman_companion_database")
        val walFile = File(dbFile.path + "-wal")
        val shmFile = File(dbFile.path + "-shm")

        val parentDir = dbFile.parentFile ?: context.filesDir
        val rollbackDb = File(parentDir, "woman_companion_database.rollback")
        val rollbackWal = File(parentDir, "woman_companion_database-wal.rollback")
        val rollbackShm = File(parentDir, "woman_companion_database-shm.rollback")

        return try {
            AppDatabase.closeDatabase()

            // 1. Create rollback copy of current database if present
            if (dbFile.exists()) {
                dbFile.copyTo(rollbackDb, overwrite = true)
            }
            if (walFile.exists()) {
                walFile.copyTo(rollbackWal, overwrite = true)
                walFile.delete()
            }
            if (shmFile.exists()) {
                shmFile.copyTo(rollbackShm, overwrite = true)
                shmFile.delete()
            }

            // 2. Remove old DB and atomically rename validated temp file into live location
            if (dbFile.exists()) {
                dbFile.delete()
            }

            val renamed = validatedTempFile.renameTo(dbFile)
            if (!renamed) {
                // Fallback to copy if rename across mount boundaries fails
                validatedTempFile.copyTo(dbFile, overwrite = true)
                validatedTempFile.delete()
            }

            // 3. Reopen Room and verify read succeeds
            val restoredDb = AppDatabase.getDatabase(context)
            val readSuccess = try {
                val cursor = restoredDb.openHelper.readableDatabase.query("SELECT count(*) FROM sqlite_master;")
                val ok = cursor.moveToFirst()
                cursor.close()
                ok
            } catch (e: Exception) {
                AppLogger.e(TAG, "Post-restore read verification query failed", e)
                false
            }

            if (readSuccess) {
                // Successful verification: safely clean up rollback files
                if (rollbackDb.exists()) rollbackDb.delete()
                if (rollbackWal.exists()) rollbackWal.delete()
                if (rollbackShm.exists()) rollbackShm.delete()
                AppLogger.i(TAG, "Database restore completed and verified successfully")
                true
            } else {
                // Verification failed: restore from rollback
                AppLogger.e(TAG, "Restore verification failed, rolling back to previous database")
                AppDatabase.closeDatabase()
                if (dbFile.exists()) dbFile.delete()

                if (rollbackDb.exists()) {
                    rollbackDb.copyTo(dbFile, overwrite = true)
                    rollbackDb.delete()
                }
                if (rollbackWal.exists()) {
                    rollbackWal.copyTo(walFile, overwrite = true)
                    rollbackWal.delete()
                }
                if (rollbackShm.exists()) {
                    rollbackShm.copyTo(shmFile, overwrite = true)
                    rollbackShm.delete()
                }
                AppDatabase.getDatabase(context) // Re-open rollback database
                false
            }
        } catch (e: Exception) {
            AppLogger.e(TAG, "Exception during database restore, attempting rollback", e)
            try {
                AppDatabase.closeDatabase()
                if (rollbackDb.exists()) {
                    rollbackDb.copyTo(dbFile, overwrite = true)
                    rollbackDb.delete()
                }
                if (rollbackWal.exists()) {
                    rollbackWal.copyTo(walFile, overwrite = true)
                    rollbackWal.delete()
                }
                if (rollbackShm.exists()) {
                    rollbackShm.copyTo(shmFile, overwrite = true)
                    rollbackShm.delete()
                }
                AppDatabase.getDatabase(context)
            } catch (_: Exception) {}
            if (validatedTempFile.exists()) validatedTempFile.delete()
            false
        }
    }
}

