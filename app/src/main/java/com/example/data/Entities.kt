package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "santri")
data class Santri(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val foto: String,
    val nomorInduk: String,
    val namaLengkap: String,
    val tempatLahir: String,
    val tanggalLahir: String,
    val jenisKelamin: String,
    val alamat: String,
    val namaWali: String,
    val nomorHpWali: String,
    val statusSantri: String, // e.g., "Aktif", "Alumni"
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "galeri")
data class Galeri(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val judul: String,
    val foto: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "settings")
data class SettingsEntity(
    @PrimaryKey val id: Int = 1, // Only 1 settings row
    val namaPondok: String,
    val logo: String,
    val warnaUtama: String, // Primary color hex (e.g. "#1E3A8A")
    val warnaSekunder: String, // Secondary color hex (e.g. "#10B981")
    val whatsapp: String,
    val email: String,
    val alamat: String,
    val mapsEmbedUrl: String,
    val firebaseDbUrl: String = "",
    val isFirebaseSyncEnabled: Boolean = false
)
