package com.example.ui.screens

import android.app.DatePickerDialog
import android.widget.Space
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.Santri
import com.example.data.SettingsEntity
import androidx.compose.ui.graphics.asImageBitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import android.graphics.Bitmap
import android.content.Intent
import android.util.Log
import android.net.Uri
import androidx.core.content.FileProvider
import java.util.*

// 1. Admin Login Dialog
@Composable
fun AdminLoginDialog(
    onDismiss: () -> Unit,
    onLoginSubmit: (String, String) -> Unit
) {
    var email by remember { mutableStateOf("admin@pesantren.com") }
    var password by remember { mutableStateOf("admin") }
    var isError by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("login_dialog_container")
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    Icons.Default.AdminPanelSettings,
                    contentDescription = "Admin Icon",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )

                Text(
                    text = "Login Administrator",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "Gunakan sandi default demi demonstrasi:\nadmin@pesantren.com / admin",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it; isError = false },
                    label = { Text("E-mail Admin") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("login_email_input"),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email") }
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it; isError = false },
                    label = { Text("Kata Sandi") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("login_password_input"),
                    visualTransformation = PasswordVisualTransformation(),
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Sandi") }
                )

                if (isError) {
                    Text(
                        text = "Kredensial salah!",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Batal")
                    }

                    Button(
                        onClick = {
                            if (email.isBlank() || password.isBlank()) {
                                isError = true
                            } else {
                                onLoginSubmit(email, password)
                            }
                        },
                        modifier = Modifier.weight(1f).testTag("login_submit_button")
                    ) {
                        Text("Masuk")
                    }
                }
            }
        }
    }
}

// 2. Add / Edit Santri Dialog Form
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SantriFormDialog(
    title: String,
    santri: Santri? = null,
    onDismiss: () -> Unit,
    onSubmit: (
        nomorInduk: String,
        namaLengkap: String,
        tempatLahir: String,
        tanggalLahir: String,
        jenisKelamin: String,
        alamat: String,
        namaWali: String,
        nomorHpWali: String,
        statusSantri: String,
        foto: String
    ) -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Form State Inputs
    var nomorInduk by remember { mutableStateOf(santri?.nomorInduk ?: "") }
    var namaLengkap by remember { mutableStateOf(santri?.namaLengkap ?: "") }
    var tempatLahir by remember { mutableStateOf(santri?.tempatLahir ?: "") }
    var tanggalLahir by remember { mutableStateOf(santri?.tanggalLahir ?: "2006-01-01") }
    var jenisKelamin by remember { mutableStateOf(santri?.jenisKelamin ?: "Laki-laki") }
    var alamat by remember { mutableStateOf(santri?.alamat ?: "") }
    var namaWali by remember { mutableStateOf(santri?.namaWali ?: "") }
    var nomorHpWali by remember { mutableStateOf(santri?.nomorHpWali ?: "") }
    var statusSantri by remember { mutableStateOf(santri?.statusSantri ?: "Aktif") }
    var fotoUrl by remember { mutableStateOf(santri?.foto ?: "https://randomuser.me/api/portraits/men/1.jpg") }

    // Local image picker launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val localPath = SantriExportImportUtils.saveSelectedImageToInternalStorage(context, uri)
            if (localPath != null) {
                fotoUrl = localPath
            }
        }
    }

    // Validation helpers
    var nomorIndukError by remember { mutableStateOf(false) }
    var namaLengkapError by remember { mutableStateOf(false) }
    var nomorHpWaliError by remember { mutableStateOf(false) }

    // Sample pre-populated avatars templates
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

    // Calendar Picker launcher
    val calendar = Calendar.getInstance()
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val formattedMonth = String.format("%02d", month + 1)
            val formattedDay = String.format("%02d", dayOfMonth)
            tanggalLahir = "$year-$formattedMonth-$formattedDay"
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnBackPress = true)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .background(Color.Transparent),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
                    .testTag("santri_form_container")
            ) {
                // Header Panel
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Keluar")
                    }
                }

                Divider()

                // Form Scroll Space
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Spacer(modifier = Modifier.height(6.dp))

                    // 1. Photo Avatar Upload section
                    Text(
                        text = "Foto Profil Santri",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(84.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                .background(Color.White)
                                .clickable { photoPickerLauncher.launch("image/*") }
                                .testTag("form_image_picker_trigger_box"),
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(fotoUrl)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Tinjauan Foto",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            
                            // Edit overlay banner
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.Black.copy(alpha = 0.55f))
                                    .align(Alignment.BottomCenter)
                                    .padding(vertical = 3.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.PhotoCamera,
                                    contentDescription = "Pilih Gambar",
                                    tint = Color.White,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }

                        // Options picker and preset selector
                        Column(modifier = Modifier.weight(1f)) {
                            Button(
                                onClick = { photoPickerLauncher.launch("image/*") },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondary,
                                    contentColor = MaterialTheme.colorScheme.onSecondary
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(38.dp)
                                    .testTag("form_image_picker_button")
                            ) {
                                Icon(Icons.Default.Upload, contentDescription = "Upload Photo", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Unggah Foto (.jpg/.jpeg/.png)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = "Atau Pilih Preset Avatar:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                val relevantList = if (jenisKelamin == "Laki-laki") menAvatars else womenAvatars
                                relevantList.forEachIndexed { i, url ->
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(Color.White)
                                            .border(
                                                width = if (fotoUrl == url) 2.dp else 1.dp,
                                                color = if (fotoUrl == url) MaterialTheme.colorScheme.secondary else Color.LightGray,
                                                shape = CircleShape
                                            )
                                            .clickable { fotoUrl = url }
                                            .testTag("preset_avatar_$i")
                                    ) {
                                        AsyncImage(
                                            model = url,
                                            contentDescription = "AvatarPreset",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = fotoUrl,
                        onValueChange = { fotoUrl = it },
                        label = { Text("Atau Tempel Link URL Foto Baru") },
                        modifier = Modifier.fillMaxWidth().testTag("form_foto_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )

                    // 2. Identity inputs
                    OutlinedTextField(
                        value = nomorInduk,
                        onValueChange = {
                            nomorInduk = it
                            nomorIndukError = it.isBlank()
                        },
                        label = { Text("Nomor Induk Santri (NI) *") },
                        modifier = Modifier.fillMaxWidth().testTag("form_ni_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        isError = nomorIndukError
                    )
                    if (nomorIndukError) {
                        Text("Nomor Induk wajib diisi!", color = MaterialTheme.colorScheme.error, fontSize = 10.sp)
                    }

                    OutlinedTextField(
                        value = namaLengkap,
                        onValueChange = {
                            namaLengkap = it
                            namaLengkapError = it.isBlank()
                        },
                        label = { Text("Nama Lengkap Santri *") },
                        modifier = Modifier.fillMaxWidth().testTag("form_nama_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        isError = namaLengkapError
                    )
                    if (namaLengkapError) {
                        Text("Nama Lengkap wajib diisi!", color = MaterialTheme.colorScheme.error, fontSize = 10.sp)
                    }

                    // Gender options checkboxes
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Jenis Kelamin: ",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.width(110.dp)
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = jenisKelamin == "Laki-laki",
                                onClick = {
                                    jenisKelamin = "Laki-laki"
                                    if (!menAvatars.contains(fotoUrl)) {
                                        fotoUrl = menAvatars.random()
                                    }
                                },
                                modifier = Modifier.testTag("form_gender_male")
                            )
                            Text("Laki-laki", fontSize = 13.sp)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = jenisKelamin == "Perempuan",
                                onClick = {
                                    jenisKelamin = "Perempuan"
                                    if (!womenAvatars.contains(fotoUrl)) {
                                        fotoUrl = womenAvatars.random()
                                    }
                                },
                                modifier = Modifier.testTag("form_gender_female")
                            )
                            Text("Perempuan", fontSize = 13.sp)
                        }
                    }

                    // Tempat dan Tanggal Lahir
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = tempatLahir,
                            onValueChange = { tempatLahir = it },
                            label = { Text("Tempat Lahir") },
                            modifier = Modifier.weight(1.2f).testTag("form_tempat_input"),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )

                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                                .clickable { datePickerDialog.show() }
                                .testTag("form_date_trigger"),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)),
                            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = tanggalLahir,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Icon(Icons.Default.CalendarToday, contentDescription = "Pick date", modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    // 3. Status Santri
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Status Aktif: ",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.width(110.dp)
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = statusSantri == "Aktif",
                                onClick = { statusSantri = "Aktif" },
                                modifier = Modifier.testTag("form_status_aktif")
                            )
                            Text("Aktif", fontSize = 13.sp)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = statusSantri == "Alumni",
                                onClick = { statusSantri = "Alumni" },
                                modifier = Modifier.testTag("form_status_alumni")
                            )
                            Text("Alumni", fontSize = 13.sp)
                        }
                    }

                    Divider()

                    // Wali and Alamat
                    OutlinedTextField(
                        value = namaWali,
                        onValueChange = { namaWali = it },
                        label = { Text("Nama Lengkap Wali") },
                        modifier = Modifier.fillMaxWidth().testTag("form_wali_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = nomorHpWali,
                        onValueChange = {
                            nomorHpWali = it
                            nomorHpWaliError = it.any { c -> !c.isDigit() }
                        },
                        label = { Text("Nomor HP / WhatsApp Wali") },
                        modifier = Modifier.fillMaxWidth().testTag("form_hp_input"),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        shape = RoundedCornerShape(10.dp),
                        isError = nomorHpWaliError
                    )
                    if (nomorHpWaliError) {
                        Text("Masukkan angka saja untuk Nomer HP!", color = MaterialTheme.colorScheme.error, fontSize = 10.sp)
                    }

                    OutlinedTextField(
                        value = alamat,
                        onValueChange = { alamat = it },
                        label = { Text("Alamat Tinggal Lengkap") },
                        modifier = Modifier.fillMaxWidth().testTag("form_alamat_input"),
                        shape = RoundedCornerShape(10.dp),
                        minLines = 2
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }

                Divider()

                // Actions panel
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Batal")
                    }

                    Button(
                        onClick = {
                            if (nomorInduk.isBlank()) {
                                nomorIndukError = true
                            }
                            if (namaLengkap.isBlank()) {
                                namaLengkapError = true
                            }
                            if (!nomorIndukError && !namaLengkapError && !nomorHpWaliError) {
                                onSubmit(
                                    nomorInduk,
                                    namaLengkap,
                                    tempatLahir,
                                    tanggalLahir,
                                    jenisKelamin,
                                    alamat,
                                    namaWali,
                                    nomorHpWali,
                                    statusSantri,
                                    fotoUrl
                                )
                            }
                        },
                        modifier = Modifier.weight(1f).testTag("form_save_button")
                    ) {
                        Text("Simpan Data")
                    }
                }
            }
        }
    }
}

// 3. Santri Detail Overlay Dialog
@Composable
fun SantriDetailDialog(
    santri: Santri,
    isAdmin: Boolean,
    onDismiss: () -> Unit,
    onEdit: (Santri) -> Unit,
    onDelete: (Santri) -> Unit,
    onPrintCardClick: (Santri) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .testTag("detail_dialog_container")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Top header toolbar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AccountCircle, contentDescription = "Detail Icon", tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Profil Lengkap Santri", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close detailed view")
                    }
                }

                Divider()

                // Large Premium Photo
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp))
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(santri.foto)
                            .crossfade(true)
                            .error(android.R.drawable.ic_menu_gallery)
                            .build(),
                        contentDescription = "Foto besar ${santri.namaLengkap}",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Name and ID
                Text(
                    text = santri.namaLengkap,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Nomor Induk: ${santri.nomorInduk}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Detail descriptive grids
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.03f), RoundedCornerShape(10.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    DetailRow("Jenis Kelamin", santri.jenisKelamin)
                    DetailRow("Tempat, Tgl Lahir", "${santri.tempatLahir}, ${santri.tanggalLahir}")
                    DetailRow("Status Keaktifan", santri.statusSantri)
                    DetailRow("Nama Wali Murid", santri.namaWali)
                    DetailRow("No WhatsApp Wali", santri.nomorHpWali)
                    DetailRow("Alamat Rumah", santri.alamat)
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Cetak Kartu & QR button (Available to all users)
                Button(
                    onClick = { onPrintCardClick(santri) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("detail_card_qr_button")
                ) {
                    Icon(Icons.Default.QrCode, contentDescription = "Kartu Santri & QR", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Cetak Kartu Santri & QR", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Actions for edit and delete if admin
                if (isAdmin) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onEdit(santri) },
                            modifier = Modifier.weight(1f).testTag("detail_edit_button")
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit data", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Edit Profil")
                        }

                        Button(
                            onClick = { onDelete(santri) },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            modifier = Modifier.weight(1f).testTag("detail_delete_button")
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Hapus data", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Hapus Data")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            modifier = Modifier.width(110.dp)
        )
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f)
        )
    }
}

// 4. Add Gallery Photo Dialog
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddGaleriDialog(
    onDismiss: () -> Unit,
    onSubmit: (judul: String, foto: String) -> Unit
) {
    var judul by remember { mutableStateOf("") }
    var fotoUrl by remember { mutableStateOf("https://images.unsplash.com/photo-1541339907198-e08756dedf3f?w=600") }

    // Preset gallery presets
    val photoPresets = listOf(
        "https://images.unsplash.com/photo-1541339907198-e08756dedf3f?w=600",
        "https://images.unsplash.com/photo-1528605248644-14dd04022da1?w=600",
        "https://images.unsplash.com/photo-1517048676732-d65bc937f952?w=600",
        "https://images.unsplash.com/photo-1475721027785-f74eccf877e2?w=600",
        "https://images.unsplash.com/photo-1524178232363-1fb2b075b655?w=600"
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("add_gallery_dialog_container")
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Upload Foto Kegiatan",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.primary
                )

                Divider()

                // Picture preview
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(fotoUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Preview",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Title input
                OutlinedTextField(
                    value = judul,
                    onValueChange = { judul = it },
                    label = { Text("Judul Kegiatan") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("form_gallery_title"),
                    shape = RoundedCornerShape(10.dp)
                )

                // Presets select segment
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Pilih Preset Foto:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        photoPresets.forEachIndexed { i, url ->
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color.White)
                                    .border(
                                        width = if (fotoUrl == url) 2.dp else 1.dp,
                                        color = if (fotoUrl == url) MaterialTheme.colorScheme.secondary else Color.LightGray,
                                        shape = RoundedCornerShape(4.dp)
                                    )
                                    .clickable { fotoUrl = url }
                                    .testTag("gallery_preset_$i")
                            ) {
                                AsyncImage(
                                    model = url,
                                    contentDescription = "PresetPhoto",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = fotoUrl,
                    onValueChange = { fotoUrl = it },
                    label = { Text("Atau Tempel URL Foto Kegiatan") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("form_gallery_foto"),
                    shape = RoundedCornerShape(10.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Batal")
                    }

                    Button(
                        onClick = {
                            if (judul.isNotBlank() && fotoUrl.isNotBlank()) {
                                onSubmit(judul, fotoUrl)
                            }
                        },
                        enabled = judul.isNotBlank() && fotoUrl.isNotBlank(),
                        modifier = Modifier.weight(1f).testTag("gallery_save_button")
                    ) {
                        Text("Simpan")
                    }
                }
            }
        }
    }
}

// 5. Card Preview Overlay Dialog (Cetak Kartu & QR)
@Composable
fun SantriCardPreviewDialog(
    santri: Santri,
    settings: SettingsEntity?,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val qrBitmap = remember(santri) {
        SantriExportImportUtils.generateQRCode(
            "SANTRI_PROFILE:${santri.nomorInduk}:${santri.namaLengkap}:${santri.statusSantri}",
            350
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .testTag("card_preview_dialog_container")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Toolbar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CreditCard, contentDescription = "ID Card Icon", tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Cetak Kartu Santri & QR", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close preview")
                    }
                }

                Divider()

                // Physically-Styled ID Card Preview Layout
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1.58f) // Golden ratio for CR80 standard ID cards
                        .shadow(4.dp, RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Header band
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(0.24f)
                                .background(MaterialTheme.colorScheme.primary)
                                .padding(horizontal = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "KARTU ANGGOTA SANTRI",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = (settings?.namaPondok ?: "PONDOK PESANTREN AL-HIDAYAH").uppercase(),
                                    color = Color.White.copy(alpha = 0.9f),
                                    fontSize = 7.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                            }
                        }

                        // Card Body
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(0.76f)
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Photo space
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                                    .background(Color(0xFFF1F5F9)),
                                contentAlignment = Alignment.Center
                            ) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(santri.foto)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "Card Photo",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            // Details column
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = santri.namaLengkap,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.Black,
                                    maxLines = 1
                                )
                                Text(
                                    text = "Nomor Induk: ${santri.nomorInduk}",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    maxLines = 1
                                )
                                Text(
                                    text = "TTL: ${santri.tempatLahir}, ${santri.tanggalLahir}",
                                    fontSize = 7.sp,
                                    color = Color.DarkGray,
                                    maxLines = 1
                                )
                                Text(
                                    text = "Status: ${santri.statusSantri}",
                                    fontSize = 8.sp,
                                    color = if (santri.statusSantri == "Aktif") Color(0xFF16A34A) else Color(0xFFD97706),
                                    fontWeight = FontWeight.Black
                                )
                            }

                            // QR Preview slot
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .border(0.5.dp, Color.LightGray, RoundedCornerShape(4.dp))
                                    .background(Color.White)
                                    .padding(2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (qrBitmap != null) {
                                    Image(
                                        bitmap = qrBitmap.asImageBitmap(),
                                        contentDescription = "QR Code",
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Actions Layout
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            val cardFile = SantriExportImportUtils.exportSantriCard(context, santri, settings)
                            if (cardFile != null) {
                                SantriExportImportUtils.shareGeneratedFile(
                                    context,
                                    cardFile,
                                    "Cetak Kartu Santri"
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("preview_print_pdf_button")
                    ) {
                        Icon(Icons.Default.Print, contentDescription = "Print PDF", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Cetak Kartu / Ekspor ke PDF", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            if (qrBitmap != null) {
                                // Save and Share QR Code Bitmap directly
                                try {
                                    val file = java.io.File(context.cacheDir, "QR_Santri_${santri.nomorInduk}.png")
                                    java.io.FileOutputStream(file).use { out ->
                                        qrBitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                                    }
                                    val authority = "${context.packageName}.fileprovider"
                                    val uri = FileProvider.getUriForFile(context, authority, file)
                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                        type = "image/png"
                                        putExtra(Intent.EXTRA_STREAM, uri)
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(Intent.createChooser(intent, "Bagikan QR Code Santri"))
                                } catch (e: Exception) {
                                    Log.e("CardPreviewDialog", "Error sharing QR Bitmap", e)
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("preview_share_qr_button")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Share QR", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Bagikan Gambar QR Code saja", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

// 6. Excel & CSV Spreadsheet Importer Dialog
@Composable
fun SantriImportDialog(
    onDismiss: () -> Unit,
    onImportDone: (List<SantriImportData>) -> Unit
) {
    val context = LocalContext.current
    var pastedText by remember { mutableStateOf("") }
    var importedFromFileList by remember { mutableStateOf<List<SantriImportData>>(emptyList()) }
    var selectedFileName by remember { mutableStateOf("") }

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            try {
                selectedFileName = "File Spreadsheet Terpilih"
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    val parsed = SantriExportImportUtils.parseCsvFromStream(stream)
                    importedFromFileList = parsed
                }
            } catch (e: Exception) {
                Log.e("ImportDialog", "Error picking file", e)
            }
        }
    }

    val finalParsedList = remember(pastedText, importedFromFileList) {
        val typedList = SantriExportImportUtils.parseSpreadsheetData(pastedText)
        importedFromFileList + typedList
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("import_excel_dialog_container")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header Toolbar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.FileUpload, contentDescription = "Upload excel icon", tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Import Excel / CSV", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close import")
                    }
                }

                Divider()

                Text(
                    text = "Metode 1: Unggah Berkas Spreadsheet (.csv)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary
                )

                OutlinedButton(
                    onClick = { filePicker.launch("*/*") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("import_file_picker_button"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.AttachFile, contentDescription = "Attach File")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (selectedFileName.isEmpty()) "Pilih File .csv / Excel" else "Ganti File .csv (" + importedFromFileList.size + " santri)")
                }

                Divider()

                Text(
                    text = "Metode 2: Tempel Langsung Kolom dari Excel/Google Sheets",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = "Format urutan kolom: Nomor Induk | Nama Lengkap | Tempat Lahir | Tanggal Lahir (YYYY-MM-DD) | L/P | Alamat | Wali | HP Wali | Status (Aktif/Alumni)",
                    fontSize = 9.sp,
                    lineHeight = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )

                OutlinedTextField(
                    value = pastedText,
                    onValueChange = { pastedText = it },
                    placeholder = {
                        Text(
                            "Tempel baris spreadsheet di sini...\nFormat contoh:\n12903\tAhmad Khoirul\tBoyolali\t2006-11-20\tLaki-laki\tAlamat RT 01\tZainal\t081234567\tAktif",
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .testTag("import_pasted_text_input"),
                    shape = RoundedCornerShape(10.dp),
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp)
                )

                // Preview panel
                if (finalParsedList.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "Pratinjau Data Terbaca (${finalParsedList.size} santri):",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(72.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                items(finalParsedList) { item ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "${item.namaLengkap} (NI: ${item.nomorInduk})",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = item.jenisKelamin,
                                            fontSize = 9.sp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Batal")
                    }

                    Button(
                        onClick = {
                            onImportDone(finalParsedList)
                        },
                        enabled = finalParsedList.isNotEmpty(),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("import_confirm_button")
                    ) {
                        Text("Import (${finalParsedList.size})")
                    }
                }
            }
        }
    }
}

