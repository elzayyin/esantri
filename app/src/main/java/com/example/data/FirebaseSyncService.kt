package com.example.data

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.util.Log

object FirebaseSyncService {
    private val client = OkHttpClient()
    private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaTypeOrNull()

    private fun cleanUrl(baseUrl: String): String {
        var clean = baseUrl.trim()
        if (clean.isBlank()) return ""
        if (!clean.startsWith("http://") && !clean.startsWith("https://")) {
            clean = "https://$clean"
        }
        if (!clean.endsWith("/")) {
            clean += "/"
        }
        return clean
    }

    suspend fun syncSantri(baseUrl: String, santri: Santri): Boolean = withContext(Dispatchers.IO) {
        val cleaned = cleanUrl(baseUrl)
        if (cleaned.isBlank()) return@withContext false
        val url = "$cleaned" + "santri/${santri.id}.json"
        val json = santriToJson(santri)
        return@withContext put(url, json)
    }

    suspend fun deleteSantri(baseUrl: String, id: Int): Boolean = withContext(Dispatchers.IO) {
        val cleaned = cleanUrl(baseUrl)
        if (cleaned.isBlank()) return@withContext false
        val url = "$cleaned" + "santri/$id.json"
        return@withContext delete(url)
    }

    suspend fun syncGaleri(baseUrl: String, galeri: Galeri): Boolean = withContext(Dispatchers.IO) {
        val cleaned = cleanUrl(baseUrl)
        if (cleaned.isBlank()) return@withContext false
        val url = "$cleaned" + "galeri/${galeri.id}.json"
        val json = galeriToJson(galeri)
        return@withContext put(url, json)
    }

    suspend fun deleteGaleri(baseUrl: String, id: Int): Boolean = withContext(Dispatchers.IO) {
        val cleaned = cleanUrl(baseUrl)
        if (cleaned.isBlank()) return@withContext false
        val url = "$cleaned" + "galeri/$id.json"
        return@withContext delete(url)
    }

    suspend fun syncSettings(baseUrl: String, settings: SettingsEntity): Boolean = withContext(Dispatchers.IO) {
        val cleaned = cleanUrl(baseUrl)
        if (cleaned.isBlank()) return@withContext false
        val url = "$cleaned" + "settings.json"
        val json = settingsToJson(settings)
        return@withContext put(url, json)
    }

    suspend fun syncAll(
        baseUrl: String,
        santriList: List<Santri>,
        galeriList: List<Galeri>,
        settings: SettingsEntity
    ): Boolean = withContext(Dispatchers.IO) {
        val cleaned = cleanUrl(baseUrl)
        if (cleaned.isBlank()) return@withContext false
        
        try {
            // 1. Sync settings
            syncSettings(cleaned, settings)

            // 2. Sync all santri as key-value pairs
            if (santriList.isEmpty()) {
                delete("$cleaned" + "santri.json")
            } else {
                val santriMap = santriList.associateBy { it.id }
                val santriJson = buildString {
                    append("{")
                    santriMap.entries.forEachIndexed { index, entry ->
                        append("\"${entry.key}\": ${santriToJson(entry.value)}")
                        if (index < santriMap.size - 1) append(",")
                    }
                    append("}")
                }
                put("$cleaned" + "santri.json", santriJson)
            }

            // 3. Sync all galeri
            if (galeriList.isEmpty()) {
                delete("$cleaned" + "galeri.json")
            } else {
                val galeriMap = galeriList.associateBy { it.id }
                val galeriJson = buildString {
                    append("{")
                    galeriMap.entries.forEachIndexed { index, entry ->
                        append("\"${entry.key}\": ${galeriToJson(entry.value)}")
                        if (index < galeriMap.size - 1) append(",")
                    }
                    append("}")
                }
                put("$cleaned" + "galeri.json", galeriJson)
            }

            return@withContext true
        } catch (e: Exception) {
            Log.e("FirebaseSync", "Error in syncAll", e)
            return@withContext false
        }
    }

    private fun put(url: String, json: String): Boolean {
        return try {
            val body = json.toRequestBody(JSON_MEDIA_TYPE)
            val request = Request.Builder()
                .url(url)
                .put(body)
                .build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.e("FirebaseSync", "PUT Failed: ${response.code} - ${response.message}")
                }
                response.isSuccessful
            }
        } catch (e: Exception) {
            Log.e("FirebaseSync", "PUT Exception for $url", e)
            false
        }
    }

    private fun delete(url: String): Boolean {
        return try {
            val request = Request.Builder()
                .url(url)
                .delete()
                .build()
            client.newCall(request).execute().use { response ->
                response.isSuccessful
            }
        } catch (e: Exception) {
            Log.e("FirebaseSync", "DELETE Exception for $url", e)
            false
        }
    }

    private fun escapeJson(text: String): String {
        return text.replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
    }

    private fun santriToJson(s: Santri): String {
        return """
        {
            "id": ${s.id},
            "foto": "${escapeJson(s.foto)}",
            "nomorInduk": "${escapeJson(s.nomorInduk)}",
            "namaLengkap": "${escapeJson(s.namaLengkap)}",
            "tempatLahir": "${escapeJson(s.tempatLahir)}",
            "tanggalLahir": "${escapeJson(s.tanggalLahir)}",
            "jenisKelamin": "${escapeJson(s.jenisKelamin)}",
            "alamat": "${escapeJson(s.alamat)}",
            "namaWali": "${escapeJson(s.namaWali)}",
            "nomorHpWali": "${escapeJson(s.nomorHpWali)}",
            "statusSantri": "${escapeJson(s.statusSantri)}",
            "createdAt": ${s.createdAt}
        }
        """.trimIndent()
    }

    private fun galeriToJson(g: Galeri): String {
        return """
        {
            "id": ${g.id},
            "judul": "${escapeJson(g.judul)}",
            "foto": "${escapeJson(g.foto)}",
            "createdAt": ${g.createdAt}
        }
        """.trimIndent()
    }

    private fun settingsToJson(s: SettingsEntity): String {
        return """
        {
            "namaPondok": "${escapeJson(s.namaPondok)}",
            "logo": "${escapeJson(s.logo)}",
            "warnaUtama": "${escapeJson(s.warnaUtama)}",
            "warnaSekunder": "${escapeJson(s.warnaSekunder)}",
            "whatsapp": "${escapeJson(s.whatsapp)}",
            "email": "${escapeJson(s.email)}",
            "alamat": "${escapeJson(s.alamat)}",
            "mapsEmbedUrl": "${escapeJson(s.mapsEmbedUrl)}",
            "firebaseDbUrl": "${escapeJson(s.firebaseDbUrl)}",
            "isFirebaseSyncEnabled": ${s.isFirebaseSyncEnabled}
        }
        """.trimIndent()
    }
}
