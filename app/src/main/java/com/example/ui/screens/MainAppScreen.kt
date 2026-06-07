package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.data.Galeri
import com.example.data.Santri
import com.example.data.SettingsEntity
import com.example.viewmodel.SantriViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(
    viewModel: SantriViewModel,
    onColorSettings: (String, String) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // State bindings
    val settingsState by viewModel.settings.collectAsState()
    val allSantriList by viewModel.allSantri.collectAsState()
    val allGaleriList by viewModel.allGaleri.collectAsState()
    val filteredSantriList by viewModel.filteredSantri.collectAsState()
    val paginatedSantriList by viewModel.paginatedSantri.collectAsState()
    val toastMsgState by viewModel.toastMessage.collectAsState()
    val isAdminLoggedIn by viewModel.isAdminLoggedIn.collectAsState()

    // Active screen navigation
    var currentScreen by remember { mutableStateOf("dashboard") } // "dashboard", "santri_list", "galeri", "kontak", "pengaturan"

    // Active filters
    val searchQuery by viewModel.searchQuery.collectAsState()
    val statusFilter by viewModel.statusFilter.collectAsState()
    val genderFilter by viewModel.genderFilter.collectAsState()
    val sortBy by viewModel.sortBy.collectAsState()
    val currentPage by viewModel.currentPage.collectAsState()

    // UI overlays & active santri details
    var selectedSantriForDetail by remember { mutableStateOf<Santri?>(null) }
    var selectedSantriForEdit by remember { mutableStateOf<Santri?>(null) }
    var isAddingSantri by remember { mutableStateOf(false) }
    var isAddingGaleri by remember { mutableStateOf(false) }
    var isLoginDialogOpen by remember { mutableStateOf(false) }
    var isImportingSantri by remember { mutableStateOf(false) }
    var santriForCardPreview by remember { mutableStateOf<Santri?>(null) }

    // Dialog Confirmation overlays
    var santriToDelete by remember { mutableStateOf<Santri?>(null) }
    var galeriToDelete by remember { mutableStateOf<Galeri?>(null) }

    // Drawer state
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    // Apply colors to dynamic theme on change
    ThemeColorSync(settingsState, onColorSettings)

    // Custom Toast Bar Notification
    Box(modifier = Modifier.fillMaxSize()) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet(
                    modifier = Modifier.width(300.dp),
                    drawerContainerColor = MaterialTheme.colorScheme.surface
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                                    )
                                )
                            )
                            .padding(vertical = 32.dp, horizontal = 20.dp)
                    ) {
                        Column {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(settingsState?.logo ?: "https://images.unsplash.com/photo-1541339907198-e08756dedf3f?w=120")
                                    .crossfade(true)
                                    .error(android.R.drawable.ic_menu_gallery)
                                    .build(),
                                contentDescription = "Logo",
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .border(2.dp, Color.White, CircleShape),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = settingsState?.namaPondok ?: "Pondok Pesantren Al-Hidayah",
                                color = Color.White,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Sistem Informasi Pusat Data",
                                color = Color.White.copy(alpha = 0.8f),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Draw items
                    val menuItems = listOf(
                        Triple("dashboard", "Dashboard", Icons.Default.Dashboard),
                        Triple("santri_list", "Data Santri", Icons.Default.People),
                        Triple("galeri", "Galeri Pondok", Icons.Default.PhotoLibrary),
                        Triple("kontak", "Kontak & Alamat", Icons.Default.ContactSupport),
                        Triple("pengaturan", "Pengaturan Aplikasi", Icons.Default.Settings)
                    )

                    menuItems.forEach { (screenId, label, icon) ->
                        NavigationDrawerItem(
                            icon = { Icon(icon, contentDescription = label) },
                            label = { Text(label, fontWeight = FontWeight.SemiBold) },
                            selected = currentScreen == screenId,
                            onClick = {
                                currentScreen = screenId
                                coroutineScope.launch { drawerState.close() }
                            },
                            modifier = Modifier
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                                .testTag("drawer_menu_$screenId"),
                            shape = RoundedCornerShape(12.dp),
                            colors = NavigationDrawerItemDefaults.colors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // Admin Session button in Drawer side panel
                    Divider()
                    Box(modifier = Modifier.padding(16.dp)) {
                        if (isAdminLoggedIn) {
                            Button(
                                onClick = { viewModel.logoutAdmin() },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("drawer_logout_button")
                            ) {
                                Icon(Icons.Default.Logout, contentDescription = "Logout")
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Logout Admin")
                            }
                        } else {
                            OutlinedButton(
                                onClick = { isLoginDialogOpen = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("drawer_login_button")
                            ) {
                                Icon(Icons.Default.Login, contentDescription = "Admin Login")
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Admin Login")
                            }
                        }
                    }
                }
            }
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Column {
                                Text(
                                    text = settingsState?.namaPondok ?: "Pondok Pesantren Al-Hidayah",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = when (currentScreen) {
                                        "dashboard" -> "Dashboard Statistik"
                                        "santri_list" -> "Administrasi Data Santri"
                                        "galeri" -> "Galeri Kegiatan Pondok"
                                        "kontak" -> "Hubungi & Alamat Kami"
                                        "pengaturan" -> "Konfigurasi Sistem"
                                        else -> "Pusat Data Santri"
                                    },
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        },
                        navigationIcon = {
                            IconButton(
                                onClick = { coroutineScope.launch { drawerState.open() } },
                                modifier = Modifier.testTag("drawer_open_button")
                            ) {
                                Icon(Icons.Default.Menu, contentDescription = "Menu Drawer")
                            }
                        },
                        actions = {
                            if (isAdminLoggedIn) {
                                AssistChip(
                                    onClick = {},
                                    label = { Text("Mode Admin", fontWeight = FontWeight.Bold) },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.AdminPanelSettings,
                                            contentDescription = "Admin panel",
                                            tint = MaterialTheme.colorScheme.secondary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    },
                                    colors = AssistChipDefaults.assistChipColors(
                                        containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f),
                                        labelColor = MaterialTheme.colorScheme.secondary
                                    ),
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                            } else {
                                IconButton(
                                    onClick = { isLoginDialogOpen = true },
                                    modifier = Modifier.testTag("header_login_button")
                                ) {
                                    Icon(Icons.Default.AccountCircle, contentDescription = "Masuk Admin")
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            titleContentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier.shadow(4.dp)
                    )
                },
                bottomBar = {
                    // Mobile navigation compatibility, clean, lightweight responsive footer
                    NavigationBar(
                        modifier = Modifier.height(64.dp),
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 8.dp
                    ) {
                        val items = listOf(
                            Triple("dashboard", "Dashboard", Icons.Default.Dashboard),
                            Triple("santri_list", "Santri", Icons.Default.People),
                            Triple("galeri", "Galeri", Icons.Default.PhotoLibrary),
                            Triple("kontak", "Kontak", Icons.Default.ContactSupport)
                        )
                        items.forEach { (screenId, label, icon) ->
                            NavigationBarItem(
                                icon = { Icon(icon, contentDescription = label, modifier = Modifier.size(20.dp)) },
                                label = { Text(label, fontSize = 10.sp) },
                                selected = currentScreen == screenId,
                                onClick = { currentScreen = screenId },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                )
                            )
                        }
                    }
                }
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    AnimatedContent(
                        targetState = currentScreen,
                        label = "screen_transition"
                    ) { target ->
                        when (target) {
                            "dashboard" -> DashboardScreen(
                                viewModel = viewModel,
                                allSantri = allSantriList,
                                allGaleri = allGaleriList,
                                onNavigate = { currentScreen = it }
                            )
                            "santri_list" -> SantriListScreen(
                                viewModel = viewModel,
                                santriList = filteredSantriList,
                                paginatedList = paginatedSantriList,
                                currentPage = currentPage,
                                totalCount = filteredSantriList.size,
                                isAdmin = isAdminLoggedIn,
                                onAddClick = { isAddingSantri = true },
                                onDetailClick = { selectedSantriForDetail = it },
                                onEditClick = { selectedSantriForEdit = it },
                                onDeleteClick = { santriToDelete = it },
                                onImportExcelClick = { isImportingSantri = true },
                                onExportPdfClick = {
                                    val file = SantriExportImportUtils.exportToPdf(context, filteredSantriList, settingsState)
                                    if (file != null) {
                                        SantriExportImportUtils.shareGeneratedFile(context, file, "Daftar_Santri_Laporan.pdf")
                                    } else {
                                        viewModel.showToast("Gagal mengekspor laporan PDF")
                                    }
                                }
                            )
                            "galeri" -> GaleriScreen(
                                viewModel = viewModel,
                                galeriList = allGaleriList,
                                isAdmin = isAdminLoggedIn,
                                onAddClick = { isAddingGaleri = true },
                                onDeleteClick = { galeriToDelete = it }
                            )
                            "kontak" -> KontakScreen(
                                settings = settingsState
                            )
                            "pengaturan" -> PengaturanScreen(
                                viewModel = viewModel,
                                settings = settingsState,
                                isAdmin = isAdminLoggedIn,
                                onLoginPrompt = { isLoginDialogOpen = true }
                            )
                        }
                    }
                }
            }

            // Interactive overlays / dialog managers
            if (isLoginDialogOpen) {
                AdminLoginDialog(
                    onDismiss = { isLoginDialogOpen = false },
                    onLoginSubmit = { email, password ->
                        val success = viewModel.loginAdmin(email, password)
                        if (success) isLoginDialogOpen = false
                    }
                )
            }

            if (isAddingSantri) {
                SantriFormDialog(
                    title = "Tambah Santri Baru",
                    santri = null,
                    onDismiss = { isAddingSantri = false },
                    onSubmit = { nom, nama, tempat, tgl, jk, alm, wali, hp, stat, foto ->
                        viewModel.addSantri(nom, nama, tempat, tgl, jk, alm, wali, hp, stat, foto)
                        isAddingSantri = false
                    }
                )
            }

            if (selectedSantriForEdit != null) {
                SantriFormDialog(
                    title = "Edit Data Santri",
                    santri = selectedSantriForEdit,
                    onDismiss = { selectedSantriForEdit = null },
                    onSubmit = { nom, nama, tempat, tgl, jk, alm, wali, hp, stat, foto ->
                        val updated = selectedSantriForEdit!!.copy(
                            nomorInduk = nom,
                            namaLengkap = nama,
                            tempatLahir = tempat,
                            tanggalLahir = tgl,
                            jenisKelamin = jk,
                            alamat = alm,
                            namaWali = wali,
                            nomorHpWali = hp,
                            statusSantri = stat,
                            foto = foto
                        )
                        viewModel.updateSantri(updated)
                        selectedSantriForEdit = null
                    }
                )
            }

            if (selectedSantriForDetail != null) {
                SantriDetailDialog(
                    santri = selectedSantriForDetail!!,
                    isAdmin = isAdminLoggedIn,
                    onDismiss = { selectedSantriForDetail = null },
                    onEdit = {
                        selectedSantriForEdit = it
                        selectedSantriForDetail = null
                    },
                    onDelete = {
                        santriToDelete = it
                        selectedSantriForDetail = null
                    },
                    onPrintCardClick = {
                        santriForCardPreview = it
                        selectedSantriForDetail = null
                    }
                )
            }

            if (isAddingGaleri) {
                AddGaleriDialog(
                    onDismiss = { isAddingGaleri = false },
                    onSubmit = { judul, foto ->
                        viewModel.addGaleri(judul, foto)
                        isAddingGaleri = false
                    }
                )
            }

            if (isImportingSantri) {
                SantriImportDialog(
                    onDismiss = { isImportingSantri = false },
                    onImportDone = { parsed ->
                        viewModel.importSantriList(parsed)
                        isImportingSantri = false
                    }
                )
            }

            if (santriForCardPreview != null) {
                SantriCardPreviewDialog(
                    santri = santriForCardPreview!!,
                    settings = settingsState,
                    onDismiss = { santriForCardPreview = null }
                )
            }

            // Confirm Delete Santri
            if (santriToDelete != null) {
                AlertDialog(
                    onDismissRequest = { santriToDelete = null },
                    title = { Text("Konfirmasi Hapus", fontWeight = FontWeight.Bold) },
                    text = { Text("Apakah Anda yakin ingin menghapus data santri '${santriToDelete?.namaLengkap}' secara permanen?") },
                    confirmButton = {
                        Button(
                            onClick = {
                                santriToDelete?.let { viewModel.deleteSantri(it) }
                                santriToDelete = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Hapus")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { santriToDelete = null }) {
                            Text("Batal")
                        }
                    },
                    modifier = Modifier.testTag("confirm_delete_santri_dialog")
                )
            }

            // Confirm Delete Galeri Foto
            if (galeriToDelete != null) {
                AlertDialog(
                    onDismissRequest = { galeriToDelete = null },
                    title = { Text("Konfirmasi Hapus Foto", fontWeight = FontWeight.Bold) },
                    text = { Text("Apakah Anda yakin ingin menghapus foto '${galeriToDelete?.judul}' dari galeri pondok?") },
                    confirmButton = {
                        Button(
                            onClick = {
                                galeriToDelete?.let { viewModel.deleteGaleri(it) }
                                galeriToDelete = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Hapus")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { galeriToDelete = null }) {
                            Text("Batal")
                        }
                    }
                )
            }
        }

        // Custom Floating Toast Notification Animation
        toastMsgState?.let { msg ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 80.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF1E293B),
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .padding(horizontal = 24.dp, vertical = 12.dp)
                        .shadow(8.dp, RoundedCornerShape(24.dp)),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = "Info",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = msg,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // Automatically hide after 3 seconds
            LaunchedEffect(msg) {
                delay(3000)
                viewModel.clearToast()
            }
        }
    }
}

// Side-effects scheduler for setting custom app themes dynamically!
@Composable
fun ThemeColorSync(
    settings: SettingsEntity?,
    onColorsChanged: (String, String) -> Unit
) {
    LaunchedEffect(settings?.warnaUtama, settings?.warnaSekunder) {
        if (settings != null) {
            onColorsChanged(settings.warnaUtama, settings.warnaSekunder)
        }
    }
}
