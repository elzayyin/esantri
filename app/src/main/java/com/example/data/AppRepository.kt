package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow

class AppRepository(private val db: AppDatabase) {
    val santriDao = db.santriDao()
    val galeriDao = db.galeriDao()
    val settingsDao = db.settingsDao()

    val allSantri: Flow<List<Santri>> = santriDao.getAllSantri()
    val allGaleri: Flow<List<Galeri>> = galeriDao.getAllGaleri()
    val settings: Flow<SettingsEntity?> = settingsDao.getSettings()

    fun getSantriById(id: Int): Flow<Santri?> = santriDao.getSantriById(id)

    suspend fun insertSantri(santri: Santri): Long = santriDao.insertSantri(santri)
    suspend fun updateSantri(santri: Santri) = santriDao.updateSantri(santri)
    suspend fun deleteSantri(santri: Santri) = santriDao.deleteSantri(santri)
    suspend fun deleteSantriById(id: Int) = santriDao.deleteSantriById(id)

    suspend fun insertGaleri(galeri: Galeri): Long = galeriDao.insertGaleri(galeri)
    suspend fun deleteGaleri(galeri: Galeri) = galeriDao.deleteGaleri(galeri)
    suspend fun deleteGaleriById(id: Int) = galeriDao.deleteGaleriById(id)

    suspend fun saveSettings(settingsEntity: SettingsEntity) = settingsDao.insertOrUpdateSettings(settingsEntity)

    suspend fun prepopulateIfNeeded() {
        val currentSettings = settingsDao.getSettingsDirect()
        if (currentSettings == null) {
            // Seed Settings
            val defaultSettings = SettingsEntity(
                namaPondok = "Pondok Pesantren Al-Hidayah",
                logo = "https://images.unsplash.com/photo-1541339907198-e08756dedf3f?q=80&w=200&auto=format&fit=crop",
                warnaUtama = "#1E40AF", // Blue
                warnaSekunder = "#059669", // Green
                whatsapp = "081234567890",
                email = "info@alhidayah.or.id",
                alamat = "Jl. Raya Pesantren No. 45, Kel. Sukamaju, Kec. Cibeunying, Bandung, Jawa Barat 40123",
                mapsEmbedUrl = "https://maps.google.com/maps?q=Bandung&t=&z=13&ie=UTF8&iwloc=&output=embed"
            )
            saveSettings(defaultSettings)

            // Seed Santri
            val defaultSantri = listOf(
                Santri(
                    nomorInduk = "202604011",
                    namaLengkap = "Ahmad Rifai Hakim",
                    tempatLahir = "Bandung",
                    tanggalLahir = "2006-03-15",
                    jenisKelamin = "Laki-laki",
                    alamat = "Jl. Merdeka No. 12, Bandung",
                    namaWali = "H. Mansyur Hakim",
                    nomorHpWali = "081234567801",
                    statusSantri = "Aktif",
                    foto = "https://randomuser.me/api/portraits/men/32.jpg"
                ),
                Santri(
                    nomorInduk = "202604012",
                    namaLengkap = "Fatima Az-Zahra",
                    tempatLahir = "Yogyakarta",
                    tanggalLahir = "2007-07-22",
                    jenisKelamin = "Perempuan",
                    alamat = "Sleman, DI Yogyakarta",
                    namaWali = "Drs. Ahmad Solihin",
                    nomorHpWali = "085678901202",
                    statusSantri = "Aktif",
                    foto = "https://randomuser.me/api/portraits/women/44.jpg"
                ),
                Santri(
                    nomorInduk = "202604013",
                    namaLengkap = "Zulfikar Ali",
                    tempatLahir = "Surabaya",
                    tanggalLahir = "2005-11-02",
                    jenisKelamin = "Laki-laki",
                    alamat = "Gubeng Kertajaya, Surabaya",
                    namaWali = "Zainuddin Ali",
                    nomorHpWali = "081395847403",
                    statusSantri = "Alumni",
                    foto = "https://randomuser.me/api/portraits/men/75.jpg"
                ),
                Santri(
                    nomorInduk = "202604014",
                    namaLengkap = "Siti Aisyah",
                    tempatLahir = "Jakarta",
                    tanggalLahir = "2008-01-10",
                    jenisKelamin = "Perempuan",
                    alamat = "Kebayoran Baru, Jakarta Selatan",
                    namaWali = "Rudi Hermawan",
                    nomorHpWali = "089876543204",
                    statusSantri = "Aktif",
                    foto = "https://randomuser.me/api/portraits/women/62.jpg"
                ),
                Santri(
                    nomorInduk = "202604015",
                    namaLengkap = "Muhammad Yusuf",
                    tempatLahir = "Bogor",
                    tanggalLahir = "2006-09-30",
                    jenisKelamin = "Laki-laki",
                    alamat = "Ciawi, Bogor",
                    namaWali = "Syarif Abdullah",
                    nomorHpWali = "087711223305",
                    statusSantri = "Aktif",
                    foto = "https://randomuser.me/api/portraits/men/52.jpg"
                )
            )
            for (s in defaultSantri) {
                insertSantri(s)
            }

            // Seed Galeri
            val defaultGaleri = listOf(
                Galeri(
                    judul = "Pengajian Mingguan Kitab Kuning",
                    foto = "https://images.unsplash.com/photo-1541339907198-e08756dedf3f?w=600&auto=format&fit=crop&q=60"
                ),
                Galeri(
                    judul = "Kerja Bakti Kebersihan Komplek Asrama",
                    foto = "https://images.unsplash.com/photo-1528605248644-14dd04022da1?w=600&auto=format&fit=crop&q=60"
                ),
                Galeri(
                    judul = "Gema Shalawat Hari Santri Nasional",
                    foto = "https://images.unsplash.com/photo-1517048676732-d65bc937f952?w=600&auto=format&fit=crop&q=60"
                ),
                Galeri(
                    judul = "Pelatihan Pidato Multi Bahasa",
                    foto = "https://images.unsplash.com/photo-1475721027785-f74eccf877e2?w=600&auto=format&fit=crop&q=60"
                )
            )
            for (g in defaultGaleri) {
                insertGaleri(g)
            }
        }
    }
}
