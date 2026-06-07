package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SantriViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: AppRepository

    // Native flow lists from DAO
    val allSantri: StateFlow<List<Santri>>
    val allGaleri: StateFlow<List<Galeri>>
    val settings: StateFlow<SettingsEntity?>

    // Admin Auth State
    private val _isAdminLoggedIn = MutableStateFlow(false)
    val isAdminLoggedIn: StateFlow<Boolean> = _isAdminLoggedIn.asStateFlow()

    // Search and Filter Settings
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _statusFilter = MutableStateFlow("Semua") // "Semua", "Aktif", "Alumni"
    val statusFilter: StateFlow<String> = _statusFilter.asStateFlow()

    private val _genderFilter = MutableStateFlow("Semua") // "Semua", "Laki-laki", "Perempuan"
    val genderFilter: StateFlow<String> = _genderFilter.asStateFlow()

    private val _sortBy = MutableStateFlow("Baru Ditambahkan") // "Baru Ditambahkan", "Nama A-Z", "Nama Z-A", "Nomor Induk"
    val sortBy: StateFlow<String> = _sortBy.asStateFlow()

    // Pagination
    private val _currentPage = MutableStateFlow(0)
    val currentPage: StateFlow<Int> = _currentPage.asStateFlow()
    val itemsPerPage = 3 // Standard small number so pagination is actively visible

    // UI Feedback Banner state
    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = AppRepository(database)

        // Seed initial values in background thread
        viewModelScope.launch(Dispatchers.IO) {
            repository.prepopulateIfNeeded()
        }

        allSantri = repository.allSantri.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allGaleri = repository.allGaleri.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        settings = repository.settings.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )
    }

    // Helper to send feedback toasts
    fun showToast(message: String) {
        _toastMessage.value = message
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    // Admin Authentication Handlers
    fun loginAdmin(email: String, sandri: String): Boolean {
        return if (email == "admin@pesantren.com" && sandri == "admin") {
            _isAdminLoggedIn.value = true
            showToast("Login Berhasil! Selamat datang Admin.")
            true
        } else {
            showToast("Email/Password Salah! Default: admin@pesantren.com / admin")
            false
        }
    }

    fun logoutAdmin() {
        _isAdminLoggedIn.value = false
        showToast("Logout Berhasil.")
    }

    // List Filtering & Searching Operations
    val filteredSantri: StateFlow<List<Santri>> = combine(
        allSantri, _searchQuery, _statusFilter, _genderFilter, _sortBy
    ) { list, query, status, gender, sort ->
        var result = list

        // Apply Search query (Nomor Induk or Name)
        if (query.isNotEmpty()) {
            result = result.filter {
                it.namaLengkap.contains(query, ignoreCase = true) ||
                it.nomorInduk.contains(query, ignoreCase = true) ||
                it.alamat.contains(query, ignoreCase = true)
            }
        }

        // Apply Status Filter
        if (status != "Semua") {
            result = result.filter { it.statusSantri == status }
        }

        // Apply Gender Filter
        if (gender != "Semua") {
            result = result.filter { it.jenisKelamin == gender }
        }

        // Apply Sort Ordering
        result = when (sort) {
            "Nama A-Z" -> result.sortedBy { it.namaLengkap.lowercase() }
            "Nama Z-A" -> result.sortedByDescending { it.namaLengkap.lowercase() }
            "Nomor Induk" -> result.sortedBy { it.nomorInduk }
            else -> result.sortedByDescending { it.createdAt } // "Baru Ditambahkan"
        }

        result
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Paginated list calculation based on filtered list
    val paginatedSantri: StateFlow<List<Santri>> = combine(
        filteredSantri, _currentPage
    ) { filtered, page ->
        val startIndex = page * itemsPerPage
        if (startIndex >= filtered.size) {
            _currentPage.value = 0 // Reset page if boundary is crossed
            filtered.take(itemsPerPage)
        } else {
            filtered.drop(startIndex).take(itemsPerPage)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filter updates
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        _currentPage.value = 0 // Reset pagination on filter change
    }

    fun setStatusFilter(filter: String) {
        _statusFilter.value = filter
        _currentPage.value = 0
    }

    fun setGenderFilter(filter: String) {
        _genderFilter.value = filter
        _currentPage.value = 0
    }

    fun setSortBy(sort: String) {
        _sortBy.value = sort
        _currentPage.value = 0
    }

    fun setPage(page: Int) {
        _currentPage.value = page
    }

    // CRUD Handlers for Santri
    fun addSantri(
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
    ) {
        viewModelScope.launch {
            val newSantri = Santri(
                nomorInduk = nomorInduk,
                namaLengkap = namaLengkap,
                tempatLahir = tempatLahir,
                tanggalLahir = tanggalLahir,
                jenisKelamin = jenisKelamin,
                alamat = alamat,
                namaWali = namaWali,
                nomorHpWali = nomorHpWali,
                statusSantri = statusSantri,
                foto = foto
            )
            val insertedId = repository.insertSantri(newSantri)
            showToast("Berhasil menambahkan santri: $namaLengkap")
            
            // Trigger Firebase auto-sync if active
            val curSettings = settings.value
            if (curSettings != null && curSettings.isFirebaseSyncEnabled && curSettings.firebaseDbUrl.isNotBlank()) {
                launch(Dispatchers.IO) {
                    FirebaseSyncService.syncSantri(curSettings.firebaseDbUrl, newSantri.copy(id = insertedId.toInt()))
                }
            }
        }
    }

    fun importSantriList(list: List<com.example.ui.screens.SantriImportData>) {
        viewModelScope.launch {
            val curSettings = settings.value
            val isSyncActive = curSettings != null && curSettings.isFirebaseSyncEnabled && curSettings.firebaseDbUrl.isNotBlank()
            
            list.forEach { item ->
                val newSantri = Santri(
                    nomorInduk = item.nomorInduk,
                    namaLengkap = item.namaLengkap,
                    tempatLahir = item.tempatLahir,
                    tanggalLahir = item.tanggalLahir,
                    jenisKelamin = item.jenisKelamin,
                    alamat = item.alamat,
                    namaWali = item.namaWali,
                    nomorHpWali = item.nomorHpWali,
                    statusSantri = item.statusSantri,
                    foto = item.foto
                )
                val insertedId = repository.insertSantri(newSantri)
                
                if (isSyncActive) {
                    launch(Dispatchers.IO) {
                        FirebaseSyncService.syncSantri(curSettings!!.firebaseDbUrl, newSantri.copy(id = insertedId.toInt()))
                    }
                }
            }
            showToast("Berhasil mengimpor ${list.size} data santri dari spreadsheet.")
        }
    }

    fun updateSantri(santri: Santri) {
        viewModelScope.launch {
            repository.updateSantri(santri)
            showToast("Berhasil menyimpan perubahan data santri.")
            
            // Trigger Firebase auto-sync if active
            val curSettings = settings.value
            if (curSettings != null && curSettings.isFirebaseSyncEnabled && curSettings.firebaseDbUrl.isNotBlank()) {
                launch(Dispatchers.IO) {
                    FirebaseSyncService.syncSantri(curSettings.firebaseDbUrl, santri)
                }
            }
        }
    }

    fun deleteSantri(santri: Santri) {
        viewModelScope.launch {
            repository.deleteSantri(santri)
            showToast("Santri ${santri.namaLengkap} berhasil dihapus.")
            
            // Trigger Firebase auto-sync if active
            val curSettings = settings.value
            if (curSettings != null && curSettings.isFirebaseSyncEnabled && curSettings.firebaseDbUrl.isNotBlank()) {
                launch(Dispatchers.IO) {
                    FirebaseSyncService.deleteSantri(curSettings.firebaseDbUrl, santri.id)
                }
            }
        }
    }

    // CRUD Handlers for Gallery
    fun addGaleri(judul: String, foto: String) {
        viewModelScope.launch {
            val galleryItem = Galeri(judul = judul, foto = foto)
            val insertedId = repository.insertGaleri(galleryItem)
            showToast("Berhasil menambahkan foto galeri baru.")
            
            // Trigger Firebase auto-sync if active
            val curSettings = settings.value
            if (curSettings != null && curSettings.isFirebaseSyncEnabled && curSettings.firebaseDbUrl.isNotBlank()) {
                launch(Dispatchers.IO) {
                    FirebaseSyncService.syncGaleri(curSettings.firebaseDbUrl, galleryItem.copy(id = insertedId.toInt()))
                }
            }
        }
    }

    fun deleteGaleri(galeri: Galeri) {
        viewModelScope.launch {
            repository.deleteGaleri(galeri)
            showToast("Foto galeri berhasil dihapus.")
            
            // Trigger Firebase auto-sync if active
            val curSettings = settings.value
            if (curSettings != null && curSettings.isFirebaseSyncEnabled && curSettings.firebaseDbUrl.isNotBlank()) {
                launch(Dispatchers.IO) {
                    FirebaseSyncService.deleteGaleri(curSettings.firebaseDbUrl, galeri.id)
                }
            }
        }
    }

    // Settings adjustments saved instantly to DB
    fun updateSettings(
        namaPondok: String,
        logo: String,
        warnaUtama: String,
        warnaSekunder: String,
        whatsapp: String,
        email: String,
        alamat: String,
        mapsEmbedUrl: String,
        firebaseDbUrl: String = "",
        isFirebaseSyncEnabled: Boolean = false
    ) {
        viewModelScope.launch {
            val updatedSettings = SettingsEntity(
                namaPondok = namaPondok,
                logo = logo,
                warnaUtama = warnaUtama,
                warnaSekunder = warnaSekunder,
                whatsapp = whatsapp,
                email = email,
                alamat = alamat,
                mapsEmbedUrl = mapsEmbedUrl,
                firebaseDbUrl = firebaseDbUrl,
                isFirebaseSyncEnabled = isFirebaseSyncEnabled
            )
            repository.saveSettings(updatedSettings)
            showToast("Pengaturan pondok berhasil diperbarui.")
            
            // Trigger Firebase sync for settings if enabled
            if (isFirebaseSyncEnabled && firebaseDbUrl.isNotBlank()) {
                launch(Dispatchers.IO) {
                    FirebaseSyncService.syncSettings(firebaseDbUrl, updatedSettings)
                }
            }
        }
    }

    // Trigger full manual upload sync with Firebase
    fun syncAllToFirebase() {
        viewModelScope.launch {
            val curSettings = settings.value
            if (curSettings == null || curSettings.firebaseDbUrl.isBlank()) {
                showToast("Silakan atur URL Firebase Realtime Database terlebih dahulu di menu Pengaturan.")
                return@launch
            }
            
            showToast("Memulai sinkronisasi data ke Firebase...")
            
            val santriList = allSantri.value
            val galeriList = allGaleri.value
            
            val success = FirebaseSyncService.syncAll(
                baseUrl = curSettings.firebaseDbUrl,
                santriList = santriList,
                galeriList = galeriList,
                settings = curSettings
            )
            if (success) {
                showToast("Sinkronisasi Berhasil! Seluruh data terunggah ke Firebase.")
            } else {
                showToast("Gagal melakukan sinkronisasi dengan Firebase. Periksa koneksi/URL Anda.")
            }
        }
    }

    class Factory(private val application: Application) : androidx.lifecycle.ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SantriViewModel::class.java)) {
                return SantriViewModel(application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
