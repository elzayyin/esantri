package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SettingsEntity
import com.example.viewmodel.SantriViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PengaturanScreen(
    viewModel: SantriViewModel,
    settings: SettingsEntity?,
    isAdmin: Boolean,
    onLoginPrompt: () -> Unit
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    // Mutable states initialized with saved values
    var namaPondok by remember { mutableStateOf("") }
    var logoUrl by remember { mutableStateOf("") }
    var warnaUtama by remember { mutableStateOf("") }
    var warnaSekunder by remember { mutableStateOf("") }
    var whatsapp by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var alamat by remember { mutableStateOf("") }
    var firebaseDbUrl by remember { mutableStateOf("") }
    var isFirebaseSyncEnabled by remember { mutableStateOf(false) }

    // Sync input states once settings are loaded from Database
    LaunchedEffect(settings) {
        settings?.let {
            namaPondok = it.namaPondok
            logoUrl = it.logo
            warnaUtama = it.warnaUtama
            warnaSekunder = it.warnaSekunder
            whatsapp = it.whatsapp
            email = it.email
            alamat = it.alamat
            firebaseDbUrl = it.firebaseDbUrl
            isFirebaseSyncEnabled = it.isFirebaseSyncEnabled
        }
    }

    // Color preset lists
    val primaryColorPresets = listOf("#1E40AF", "#2563EB", "#1E3B8B", "#4F46E5", "#0F172A")
    val secondaryColorPresets = listOf("#059669", "#10B981", "#0284C7", "#D97706", "#65A30D")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Mode protection card check
        if (!isAdmin) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("admin_restriction_card")
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = "🔒 Hubungi Admin",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Akses Pengaturan Terkunci",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Anda melihat halaman ini sebagai Pengunjung. Mode mengedit pengaturan pesantren hanya tersedia setelah Login Admin.",
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onLoginPrompt,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("request_login_button")
                    ) {
                        Icon(Icons.Default.Login, contentDescription = "Selesai")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Mulai Login Admin", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Configuration Form (Disabled/Enabled based on Admin login state)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("settings_form_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = "Pengaturan",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Identitas Pesantren",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Divider()

                // Pondok Name
                OutlinedTextField(
                    value = namaPondok,
                    onValueChange = { if (isAdmin) namaPondok = it },
                    label = { Text("Nama Pondok Pesantren") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("settings_pondok_name_input"),
                    enabled = isAdmin,
                    shape = RoundedCornerShape(10.dp)
                )

                // Logo url string field
                OutlinedTextField(
                    value = logoUrl,
                    onValueChange = { if (isAdmin) logoUrl = it },
                    label = { Text("URL Link Logo Pondok") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("settings_logo_input"),
                    enabled = isAdmin,
                    shape = RoundedCornerShape(10.dp),
                    helperText = { Text("Masukkan URL foto logo", fontSize = 10.sp) }
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Primary Color Customizers
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Warna Utama Aplikasi (Biru)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        primaryColorPresets.forEach { hex ->
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(android.graphics.Color.parseColor(hex)))
                                    .border(
                                        width = if (warnaUtama.uppercase() == hex.uppercase()) 3.dp else 1.dp,
                                        color = if (warnaUtama.uppercase() == hex.uppercase()) MaterialTheme.colorScheme.onSurface else Color.LightGray,
                                        shape = CircleShape
                                    )
                                    .clickable(enabled = isAdmin) { warnaUtama = hex }
                                    .testTag("preset_primary_$hex")
                            )
                        }
                    }
                    OutlinedTextField(
                        value = warnaUtama,
                        onValueChange = { if (isAdmin) warnaUtama = it },
                        label = { Text("Kode Hex Warna Utama (misal: #1E40AF)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("settings_warna_utama"),
                        enabled = isAdmin,
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Secondary Color Customizers
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Warna Sekunder Aplikasi (Hijau)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        secondaryColorPresets.forEach { hex ->
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(android.graphics.Color.parseColor(hex)))
                                    .border(
                                        width = if (warnaSekunder.uppercase() == hex.uppercase()) 3.dp else 1.dp,
                                        color = if (warnaSekunder.uppercase() == hex.uppercase()) MaterialTheme.colorScheme.onSurface else Color.LightGray,
                                        shape = CircleShape
                                    )
                                    .clickable(enabled = isAdmin) { warnaSekunder = hex }
                                    .testTag("preset_secondary_$hex")
                            )
                        }
                    }
                    OutlinedTextField(
                        value = warnaSekunder,
                        onValueChange = { if (isAdmin) warnaSekunder = it },
                        label = { Text("Kode Hex Warna Sekunder (misal: #059669)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("settings_warna_sekunder"),
                        enabled = isAdmin,
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Detail Kontak",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )

                Divider()

                // WhatsApp input
                OutlinedTextField(
                    value = whatsapp,
                    onValueChange = { if (isAdmin) whatsapp = it },
                    label = { Text("Nomor WhatsApp (misal: 08123456789)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("settings_whatsapp"),
                    enabled = isAdmin,
                    shape = RoundedCornerShape(10.dp)
                )

                // Email input
                OutlinedTextField(
                    value = email,
                    onValueChange = { if (isAdmin) email = it },
                    label = { Text("Email Resmi") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("settings_email"),
                    enabled = isAdmin,
                    shape = RoundedCornerShape(10.dp)
                )

                // Alamat input
                OutlinedTextField(
                    value = alamat,
                    onValueChange = { if (isAdmin) alamat = it },
                    label = { Text("Alamat Resmi Pondok") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("settings_alamat"),
                    enabled = isAdmin,
                    shape = RoundedCornerShape(10.dp),
                    minLines = 2
                )

                // Submission/Save button
                if (isAdmin) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            if (namaPondok.isBlank() || warnaUtama.isBlank() || warnaSekunder.isBlank()) {
                                Toast.makeText(context, "Silakan isi field wajib!", Toast.LENGTH_SHORT).show()
                            } else {
                                viewModel.updateSettings(
                                    namaPondok = namaPondok,
                                    logo = logoUrl,
                                    warnaUtama = warnaUtama,
                                    warnaSekunder = warnaSekunder,
                                    whatsapp = whatsapp,
                                    email = email,
                                    alamat = alamat,
                                    mapsEmbedUrl = settings?.mapsEmbedUrl ?: "",
                                    firebaseDbUrl = firebaseDbUrl,
                                    isFirebaseSyncEnabled = isFirebaseSyncEnabled
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .shadow(2.dp, RoundedCornerShape(12.dp))
                            .testTag("settings_save_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = "Save settings")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Simpan Konfigurasi", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
            }
        }

        // Firebase Configuration & Sync Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("firebase_sync_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Cloud,
                        contentDescription = "Sync",
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = "Sinkronisasi Firebase Database",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Divider()

                Text(
                    text = "Hubungkan data aplikasi lokal langsung ke Firebase Realtime Database Anda secara real-time.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )

                // Sync active switch row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Auto-Sinkronisasi Aktif",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Otomatis sinkronkan setiap tambah, edit, & hapus data",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                    Switch(
                        checked = isFirebaseSyncEnabled,
                        onCheckedChange = { if (isAdmin) isFirebaseSyncEnabled = it },
                        enabled = isAdmin,
                        modifier = Modifier.testTag("firebase_sync_switch")
                    )
                }

                // Database URL input
                OutlinedTextField(
                    value = firebaseDbUrl,
                    onValueChange = { if (isAdmin) firebaseDbUrl = it },
                    label = { Text("URL Firebase Realtime Database") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("firebase_url_input"),
                    enabled = isAdmin,
                    shape = RoundedCornerShape(10.dp),
                    helperText = { Text("Contoh: https://proyek-pesantren-rtdb.firebaseio.com/", fontSize = 10.sp) }
                )

                // Manual sync action panel
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.25f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Default.CloudSync,
                            contentDescription = "Status",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(32.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Status Sinkronisasi",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Text(
                                text = if (settings?.isFirebaseSyncEnabled == true && !settings.firebaseDbUrl.isNullOrBlank())
                                    "Aktif & siap sinkronisasi"
                                else
                                    "Belum aktif / URL kosong",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                            )
                        }
                    }
                }

                if (isAdmin) {
                    // Sync Now Button
                    Button(
                        onClick = { viewModel.syncAllToFirebase() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary,
                            contentColor = MaterialTheme.colorScheme.onSecondary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("firebase_sync_now_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Sync, contentDescription = "Sync", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Sinkronkan Semua Data Sekarang", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

// Simple extension helper
@Composable
fun OutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: androidx.compose.ui.graphics.Shape = OutlinedTextFieldDefaults.shape,
    singleLine: Boolean = false,
    minLines: Int = 1,
    helperText: @Composable (() -> Unit)? = null
) {
    Column(modifier = modifier) {
        androidx.compose.material3.OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = label,
            enabled = enabled,
            shape = shape,
            singleLine = singleLine,
            minLines = minLines,
            modifier = Modifier.fillMaxWidth()
        )
        if (helperText != null) {
            Spacer(modifier = Modifier.height(2.dp))
            helperText()
        }
    }
}
