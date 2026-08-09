package com.apocalyptolabs.viking.core.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Build
import com.apocalyptolabs.viking.core.model.ThreatResult
import java.io.File
import java.io.FileOutputStream

object PdfReportGenerator {

    fun generateSecurityCertificatePdf(
        context: Context,
        threatLogs: List<ThreatResult>,
        activeThreatCount: Int
    ): File {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val paint = Paint()
        val titlePaint = Paint().apply {
            color = Color.rgb(13, 27, 42)
            textSize = 22f
            isFakeBoldText = true
        }

        val bodyPaint = Paint().apply {
            color = Color.DKGRAY
            textSize = 12f
        }

        val headerPaint = Paint().apply {
            color = Color.rgb(0, 180, 216)
            textSize = 14f
            isFakeBoldText = true
        }

        canvas.drawText("VIKING ON-DEVICE AI SECURITY CERTIFICATE", 40f, 60f, titlePaint)
        canvas.drawText("Generated on-device with zero network transmission • DPDP Act Compliant", 40f, 85f, bodyPaint)
        canvas.drawLine(40f, 100f, 555f, 100f, paint)

        val healthScore = if (activeThreatCount == 0) 100 else (100 - activeThreatCount * 15).coerceAtLeast(10)
        canvas.drawText("Device Security Score: $healthScore / 100", 40f, 135f, headerPaint)
        canvas.drawText("Device Model: ${Build.MODEL} (${Build.MANUFACTURER}) | Android API ${Build.VERSION.SDK_INT}", 40f, 155f, bodyPaint)

        canvas.drawText("Recent Security Log Summary (${threatLogs.size} Total Scans):", 40f, 195f, headerPaint)

        var y = 225f
        threatLogs.take(12).forEachIndexed { idx, log ->
            val line = "${idx + 1}. [${log.severity.name}] ${log.type.name} - ${log.target} (${log.timestamp.toFormattedDate()})"
            canvas.drawText(line, 40f, y, bodyPaint)
            y += 22f
        }

        canvas.drawText("Certified by Viking Gemma 270M On-Device Cybersecurity Engine", 40f, 800f, bodyPaint)

        pdfDocument.finishPage(page)

        val outputFile = File(context.cacheDir, "Viking_Security_Certificate.pdf")
        pdfDocument.writeTo(FileOutputStream(outputFile))
        pdfDocument.close()

        return outputFile
    }
}
