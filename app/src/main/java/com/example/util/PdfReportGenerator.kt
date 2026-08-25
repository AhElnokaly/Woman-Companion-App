package com.example.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.content.FileProvider
import java.io.File

object PdfReportGenerator {

    fun generateAndSharePdf(context: Context, reportText: String) {
        try {
            val pdfDocument = PdfDocument()
            val pageWidth = 595 // A4 width in pt (72 dpi)
            val pageHeight = 842 // A4 height in pt
            val margin = 40f
            val contentWidth = (pageWidth - margin * 2).toInt()

            val textPaint = TextPaint().apply {
                color = Color.BLACK
                textSize = 12f
                isAntiAlias = true
            }

            val layout = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                StaticLayout.Builder.obtain(reportText, 0, reportText.length, textPaint, contentWidth)
                    .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                    .setLineSpacing(0f, 1.25f)
                    .setIncludePad(true)
                    .build()
            } else {
                @Suppress("DEPRECATION")
                StaticLayout(reportText, textPaint, contentWidth, Layout.Alignment.ALIGN_NORMAL, 1.25f, 0f, true)
            }

            val totalHeight = layout.height
            val drawableHeight = pageHeight - margin * 2
            var pageIndex = 1
            var startY = 0

            while (startY < totalHeight || pageIndex == 1) {
                val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageIndex).create()
                val page = pdfDocument.startPage(pageInfo)
                val canvas = page.canvas

                // Header background banner
                val headerPaint = Paint().apply {
                    color = Color.rgb(230, 242, 240)
                }
                canvas.drawRect(0f, 0f, pageWidth.toFloat(), 50f, headerPaint)

                val titlePaint = TextPaint().apply {
                    color = Color.rgb(0, 128, 110)
                    textSize = 13f
                    isFakeBoldText = true
                    isAntiAlias = true
                }
                canvas.drawText("🌸 تقرير زيارة الطبيبة - تطبيق جوري (صفحة $pageIndex)", margin, 32f, titlePaint)

                canvas.save()
                canvas.translate(margin, margin + 20f - startY)
                canvas.clipRect(0f, startY.toFloat(), contentWidth.toFloat(), (startY + drawableHeight - 40f))
                layout.draw(canvas)
                canvas.restore()

                pdfDocument.finishPage(page)
                startY += (drawableHeight - 40f).toInt()
                pageIndex++
                if (startY >= totalHeight && pageIndex > 1) break
            }

            val pdfFile = File(context.cacheDir, "doctor_report_${System.currentTimeMillis()}.pdf")
            pdfFile.outputStream().use { out ->
                pdfDocument.writeTo(out)
            }
            pdfDocument.close()

            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", pdfFile)
            val shareIntent = android.content.Intent().apply {
                action = android.content.Intent.ACTION_SEND
                putExtra(android.content.Intent.EXTRA_STREAM, uri)
                type = "application/pdf"
                addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(android.content.Intent.createChooser(shareIntent, "مشاركة التقرير الطبي PDF 📄"))
        } catch (e: Exception) {
            com.example.util.AppLogger.e("PdfReportGenerator", "Failed to generate or share PDF report", e)
            // Fallback text share if PDF rendering fails
            val sendIntent = android.content.Intent().apply {
                action = android.content.Intent.ACTION_SEND
                putExtra(android.content.Intent.EXTRA_TEXT, reportText)
                type = "text/plain"
            }
            context.startActivity(android.content.Intent.createChooser(sendIntent, "مشاركة التقرير الطبي (نص)"))
        }
    }
}
