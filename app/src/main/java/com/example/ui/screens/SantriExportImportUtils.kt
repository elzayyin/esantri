package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.core.content.FileProvider
import com.example.data.Santri
import com.example.data.SettingsEntity
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.InputStreamReader

object SantriExportImportUtils {

    // --- 1. QR CODE GENERATOR ---
    fun generateQRCode(text: String, size: Int = 300): Bitmap? {
        return try {
            val bitMatrix = MultiFormatWriter().encode(text, BarcodeFormat.QR_CODE, size, size)
            val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
            for (x in 0 until size) {
                for (y in 0 until size) {
                    bitmap.setPixel(x, y, if (bitMatrix.get(x, y)) Color.BLACK else Color.WHITE)
                }
            }
            bitmap
        } catch (e: Exception) {
            Log.e("ExportUtils", "Error generating QR code", e)
            null
        }
    }

    // --- 2. SPREADSHEET (CSV / TABULAR TEXT) EXCEL-COMPATIBLE PARSER ---
    // Parses lines of copy-pasted tab/comma/semicolon-separated spreadsheet rows
    fun parseSpreadsheetData(text: String): List<SantriImportData> {
        val result = mutableListOf<SantriImportData>()
        val lines = text.split("\n")
        
        val menAvatars = listOf(
            "https://randomuser.me/api/portraits/men/32.jpg",
            "https://randomuser.me/api/portraits/men/75.jpg",
            "https://randomuser.me/api/portraits/men/52.jpg",
            "https://randomuser.me/api/portraits/men/6.jpg"
        )
        val womenAvatars = listOf(
            "https://randomuser.me/api/portraits/women/44.jpg",
            "https://randomuser.me/api/portraits/women/62.jpg",
            "https://randomuser.me/api/portraits/women/11.jpg",
            "https://randomuser.me/api/portraits/women/70.jpg"
        )

        for (line in lines) {
            if (line.trim().isEmpty()) continue
            
            // Detect delimiter (Tab, Semicolon, or Comma)
            val delimiter = when {
                line.contains("\t") -> "\t"
                line.contains(";") -> ";"
                else -> ","
            }
            
            val parts = line.split(delimiter).map { it.trim().removeSurrounding("\"") }
            
            // Skip typical headers
            if (parts.isNotEmpty() && (parts[0].lowercase().contains("no") || parts[0].lowercase().contains("induk") || parts[0].lowercase().contains("id") || parts[0].lowercase().contains("nama"))) {
                continue
            }
            
            if (parts.size >= 2) {
                val nomorInduk = parts.getOrNull(0) ?: ""
                val namaLengkap = parts.getOrNull(1) ?: ""
                if (nomorInduk.isBlank() || namaLengkap.isBlank()) continue
                
                val tempatLahir = parts.getOrNull(2) ?: "Jakarta"
                val tanggalLahir = parts.getOrNull(3) ?: "2006-01-01"
                val jenisKelaminRaw = parts.getOrNull(4) ?: "Laki-laki"
                val jenisKelamin = if (jenisKelaminRaw.lowercase().startsWith("p")) "Perempuan" else "Laki-laki"
                val alamat = parts.getOrNull(5) ?: "Alamat Pondok"
                val namaWali = parts.getOrNull(6) ?: "Wali"
                val nomorHpWali = parts.getOrNull(7) ?: ""
                val statusRaw = parts.getOrNull(8) ?: "Aktif"
                val statusSantri = if (statusRaw.lowercase().startsWith("al")) "Alumni" else "Aktif"
                
                val fotoDefault = if (jenisKelamin == "Perempuan") womenAvatars.random() else menAvatars.random()
                
                result.add(
                    SantriImportData(
                        nomorInduk = nomorInduk,
                        namaLengkap = namaLengkap,
                        tempatLahir = tempatLahir,
                        tanggalLahir = tanggalLahir,
                        jenisKelamin = jenisKelamin,
                        alamat = alamat,
                        namaWali = namaWali,
                        nomorHpWali = nomorHpWali,
                        statusSantri = statusSantri,
                        foto = fotoDefault
                    )
                )
            }
        }
        return result
    }

    // Helper to read CSV file content from Stream
    fun parseCsvFromStream(inputStream: InputStream): List<SantriImportData> {
        val reader = BufferedReader(InputStreamReader(inputStream))
        val fullContent = StringBuilder()
        var line: String? = reader.readLine()
        while (line != null) {
            fullContent.append(line).append("\n")
            line = reader.readLine()
        }
        return parseSpreadsheetData(fullContent.toString())
    }

    // --- 3. MULTI-PAGE PDF REPORT EXPORTER ---
    fun exportToPdf(
        context: Context,
        santriList: List<Santri>,
        settings: SettingsEntity?
    ): File? {
        val pdfDocument = PdfDocument()
        val pageWidth = 595 // A4 Width in PostScript points
        val pageHeight = 842 // A4 Height in PostScript points
        
        val margin = 36f
        var currentY = 50f
        
        val paintTitle = Paint().apply {
            color = Color.DKGRAY
            textSize = 16f
            isFakeBoldText = true
            isAntiAlias = true
        }
        val paintHeader = Paint().apply {
            color = Color.BLACK
            textSize = 18f
            isFakeBoldText = true
            isAntiAlias = true
        }
        val paintSub = Paint().apply {
            color = Color.GRAY
            textSize = 10f
            isAntiAlias = true
        }
        val paintTableHead = Paint().apply {
            color = Color.WHITE
            textSize = 11f
            isFakeBoldText = true
            isAntiAlias = true
        }
        val paintTableBody = Paint().apply {
            color = Color.BLACK
            textSize = 10f
            isAntiAlias = true
        }
        val paintTableDivider = Paint().apply {
            color = Color.LTGRAY
            strokeWidth = 1f
            style = Paint.Style.STROKE
            isAntiAlias = true
        }
        val paintHeaderBg = Paint().apply {
            color = Color.parseColor(settings?.warnaUtama ?: "#1E3A8A")
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        var pageNumber = 1
        var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas

        // Draw Document Header
        fun drawHeaderSection(cv: Canvas, pgNum: Int) {
            currentY = 40f
            
            // Draw Main Title
            cv.drawText(settings?.namaPondok ?: "PONDOK PESANTREN AL-HIDAYAH", margin, currentY, paintHeader)
            currentY += 18f
            cv.drawText("SISTEM INFORMASI DATA SANTRI - DAFTAR INDUK", margin, currentY, paintTitle)
            currentY += 16f
            cv.drawText("${settings?.alamat ?: "Alamat Pondok"} | Email: ${settings?.email ?: "info@pesantren.com"}", margin, currentY, paintSub)
            currentY += 12f
            
            // Draw top divider line
            cv.drawLine(margin, currentY, pageWidth - margin, currentY, paintTableDivider)
            currentY += 20f

            // Draw Page Number indicator
            cv.drawText("Halaman $pgNum", pageWidth - margin - 60f, currentY - 30f, paintSub)

            // Draw Table Headers with Background
            val headRect = Rect(margin.toInt(), currentY.toInt() - 15, (pageWidth - margin).toInt(), currentY.toInt() + 10)
            cv.drawRect(headRect, paintHeaderBg)

            // Draw Header Texts
            cv.drawText("No", margin + 10f, currentY, paintTableHead)
            cv.drawText("Nomor Induk", margin + 40f, currentY, paintTableHead)
            cv.drawText("Nama Lengkap", margin + 130f, currentY, paintTableHead)
            cv.drawText("L/P", margin + 290f, currentY, paintTableHead)
            cv.drawText("No HP Wali", margin + 340f, currentY, paintTableHead)
            cv.drawText("Status", margin + 440f, currentY, paintTableHead)
            
            currentY += 24f
        }

        drawHeaderSection(canvas, pageNumber)

        for ((index, santri) in santriList.withIndex()) {
            // Check if we need a new page
            if (currentY + 24f > pageHeight - margin) {
                pdfDocument.finishPage(page)
                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                drawHeaderSection(canvas, pageNumber)
            }

            // Zebra striping background
            if (index % 2 == 1) {
                val stripeBg = Paint().apply { color = Color.parseColor("#F1F5F9") }
                canvas.drawRect(margin, currentY - 14, pageWidth - margin, currentY + 6, stripeBg)
            }

            // Draw Santri rows
            val noTxt = (index + 1).toString()
            val genderShort = if (santri.jenisKelamin == "Perempuan") "P" else "L"
            
            canvas.drawText(noTxt, margin + 10f, currentY, paintTableBody)
            canvas.drawText(santri.nomorInduk, margin + 40f, currentY, paintTableBody)
            
            // Handle name clipping safely
            var rawName = santri.namaLengkap
            if (rawName.length > 25) rawName = rawName.take(23) + ".."
            canvas.drawText(rawName, margin + 130f, currentY, paintTableBody)
            
            canvas.drawText(genderShort, margin + 290f, currentY, paintTableBody)
            canvas.drawText(santri.nomorHpWali.ifBlank { "-" }, margin + 340f, currentY, paintTableBody)
            canvas.drawText(santri.statusSantri, margin + 440f, currentY, paintTableBody)

            canvas.drawLine(margin, currentY + 6, pageWidth - margin, currentY + 6, paintTableDivider)
            currentY += 20f
        }

        pdfDocument.finishPage(page)

        return try {
            val file = File(context.cacheDir, "Daftar_Santri_${System.currentTimeMillis()}.pdf")
            FileOutputStream(file).use { out ->
                pdfDocument.writeTo(out)
            }
            pdfDocument.close()
            file
        } catch (e: Exception) {
            Log.e("ExportUtils", "Error writing PDF report", e)
            pdfDocument.close()
            null
        }
    }

    // --- 4. HIGH-RESOLUTION ID CARD (KARTU SANTRI) PDF GENERATOR ---
    fun exportSantriCard(
        context: Context,
        santri: Santri,
        settings: SettingsEntity?
    ): File? {
        val pdfDocument = PdfDocument()
        
        // CR85 ID Card standard layout size (scaled to 150 points width, 300 points height index grid)
        // High quality: 480 points width, 300 points height (approx. Credit card shape landscape!)
        val cardWidth = 480
        val cardHeight = 300
        
        val pageInfo = PdfDocument.PageInfo.Builder(cardWidth, cardHeight, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        // 1. Draw elegant background card frame
        val bgPaint = Paint().apply {
            color = Color.parseColor("#F8FAFC") // light slate base
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        canvas.drawRect(0f, 0f, cardWidth.toFloat(), cardHeight.toFloat(), bgPaint)

        // Draw professional Green/Blue header container
        val headerColorHex = settings?.warnaUtama ?: "#0284C7"
        val headerPaint = Paint().apply {
            color = Color.parseColor(headerColorHex)
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        canvas.drawRect(0f, 0f, cardWidth.toFloat(), 70f, headerPaint)

        // Clean white subframe
        val subFramePaint = Paint().apply {
            color = Color.WHITE
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        canvas.drawRect(12f, 82f, cardWidth - 12f, cardHeight - 12f, subFramePaint)

        // Draw Border
        val borderPaint = Paint().apply {
            color = Color.parseColor(headerColorHex)
            style = Paint.Style.STROKE
            strokeWidth = 2f
            isAntiAlias = true
        }
        canvas.drawRect(12f, 82f, cardWidth - 12f, cardHeight - 12f, borderPaint)

        // Header text elements
        val paintHeaderTitle = Paint().apply {
            color = Color.WHITE
            textSize = 14f
            isFakeBoldText = true
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
        val paintHeaderSubTitle = Paint().apply {
            color = Color.argb(229, 255, 255, 255)
            textSize = 9f
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
        
        val pondokName = (settings?.namaPondok ?: "Pondok Pesantren Al-Hidayah").uppercase()
        canvas.drawText("KARTU TANDA ANGGOTA SANTRI", cardWidth / 2f, 26f, paintHeaderTitle)
        canvas.drawText(pondokName, cardWidth / 2f, 44f, paintHeaderSubTitle)
        canvas.drawText("Sistem Informasi Pusat Data Santri", cardWidth / 2f, 56f, Paint().apply {
            color = Color.argb(178, 255, 255, 255)
            textSize = 7f
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        })

        // 2. Draw Left-aligned Avatar default space
        val avatarRect = Rect(24, 100, 114, 212)
        val defaultAvatarBg = Paint().apply {
            color = Color.parseColor("#E2E8F0")
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        canvas.drawRect(avatarRect, defaultAvatarBg)

        // Draw some visual geometric person placeholder as photo load fallback
        val placeholderPaint = Paint().apply {
            color = Color.parseColor("#94A3B8")
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        canvas.drawCircle(69f, 140f, 22f, placeholderPaint)
        canvas.drawRect(42f, 175f, 96f, 205f, placeholderPaint)

        // Label on top of Avatar placeholder
        val paintPhotoTip = Paint().apply {
            color = Color.parseColor("#64748B")
            textSize = 6f
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("PAS FOTO", 69f, 203f, paintPhotoTip)

        // 3. Draw middle details
        val textLabelPaint = Paint().apply {
            color = Color.parseColor("#475569")
            textSize = 8f
            isAntiAlias = true
        }
        val textValuePaint = Paint().apply {
            color = Color.BLACK
            textSize = 10f
            isFakeBoldText = true
            isAntiAlias = true
        }

        var detailY = 112f
        val detailsX = 132f
        val lineSpacing = 28f

        // Name
        canvas.drawText("NAMA LENGKAP", detailsX, detailY, textLabelPaint)
        var clippedName = santri.namaLengkap
        if (clippedName.length > 22) clippedName = clippedName.take(20) + ".."
        canvas.drawText(clippedName, detailsX, detailY + 12f, textValuePaint)
        detailY += lineSpacing

        // Code ID
        canvas.drawText("NOMOR INDUK (NI)", detailsX, detailY, textLabelPaint)
        canvas.drawText(santri.nomorInduk, detailsX, detailY + 12f, textValuePaint)
        detailY += lineSpacing

        // Birth
        canvas.drawText("TEMPAT / TGL LAHIR", detailsX, detailY, textLabelPaint)
        canvas.drawText("${santri.tempatLahir}, ${santri.tanggalLahir}", detailsX, detailY + 12f, textValuePaint)
        detailY += lineSpacing

        // Address
        canvas.drawText("STATUS KEANGGOTAAN", detailsX, detailY, textLabelPaint)
        canvas.drawText(santri.statusSantri, detailsX, detailY + 12f, Paint().apply {
            color = Color.parseColor(if (santri.statusSantri == "Aktif") "#16A34A" else "#D97706")
            textSize = 10f
            isFakeBoldText = true
            isAntiAlias = true
        })

        // 4. Generate & Draw QR Code
        val qrSize = 100
        val qrBitmap = generateQRCode(
            text = "SANTRI_PROFILE:${santri.nomorInduk}:${santri.namaLengkap}:${santri.statusSantri}",
            size = qrSize
        )
        if (qrBitmap != null) {
            val qrX = cardWidth - qrSize - 24f
            val qrY = 100f
            canvas.drawBitmap(qrBitmap, qrX, qrY, Paint())
            
            // Draw a tiny text label below QR
            val qrTip = Paint().apply {
                color = Color.GRAY
                textSize = 6f
                textAlign = Paint.Align.CENTER
                isAntiAlias = true
            }
            canvas.drawText("OFFICIAL SECURE QR", qrX + (qrSize / 2f), qrY + qrSize + 12f, qrTip)
        }

        // Draw card footer
        val paintCardFooter = Paint().apply {
            color = Color.GRAY
            textSize = 6f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText("Pesantren Al-Hidayah e-Card | ID generated strictly based on local system records.", cardWidth / 2f, cardHeight - 18f, paintCardFooter)

        pdfDocument.finishPage(page)

        return try {
            val file = File(context.cacheDir, "Kartu_Santri_${santri.nomorInduk}.pdf")
            FileOutputStream(file).use { out ->
                pdfDocument.writeTo(out)
            }
            pdfDocument.close()
            file
        } catch (e: Exception) {
            Log.e("ExportUtils", "Error writing PDF ID card", e)
            pdfDocument.close()
            null
        }
    }

    // --- SHARE SYSTEM TRIGGERS ---
    fun shareGeneratedFile(context: Context, file: File, title: String) {
        try {
            val authority = "${context.packageName}.fileprovider"
            val uri: Uri = FileProvider.getUriForFile(context, authority, file)
            
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            
            val chooserIntent = Intent.createChooser(intent, title).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooserIntent)
        } catch (e: Exception) {
            Log.e("ExportUtils", "Error sharing file", e)
        }
    }

    // --- 6. SAVE IMAGE FROM LOCAL PICKER TO INTERNAL STORAGE ---
    fun saveSelectedImageToInternalStorage(context: Context, uri: Uri): String? {
        return try {
            val resolver = context.contentResolver
            val inputStream = resolver.openInputStream(uri) ?: return null
            
            // Get proper file extension from MIME or fall back to png
            val mimeType = resolver.getType(uri)
            val extension = when {
                mimeType?.contains("jpeg") == true || mimeType?.contains("jpg") == true -> "jpg"
                mimeType?.contains("png") == true -> "png"
                else -> "jpg"
            }
            
            // Create target folders if they don't exist
            val photosDir = File(context.filesDir, "photos")
            if (!photosDir.exists()) {
                photosDir.mkdirs()
            }
            
            val destFile = File(photosDir, "santri_${System.currentTimeMillis()}.$extension")
            FileOutputStream(destFile).use { out ->
                inputStream.copyTo(out)
            }
            destFile.absolutePath
        } catch (e: Exception) {
            Log.e("ExportUtils", "Error saving selected image", e)
            null
        }
    }
}

// Data holder for imports
data class SantriImportData(
    val nomorInduk: String,
    val namaLengkap: String,
    val tempatLahir: String,
    val tanggalLahir: String,
    val jenisKelamin: String,
    val alamat: String,
    val namaWali: String,
    val nomorHpWali: String,
    val statusSantri: String,
    val foto: String
)
