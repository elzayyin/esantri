package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SantriDao {
    @Query("SELECT * FROM santri ORDER BY createdAt DESC")
    fun getAllSantri(): Flow<List<Santri>>

    @Query("SELECT * FROM santri WHERE id = :id LIMIT 1")
    fun getSantriById(id: Int): Flow<Santri?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSantri(santri: Santri): Long

    @Update
    suspend fun updateSantri(santri: Santri)

    @Delete
    suspend fun deleteSantri(santri: Santri)

    @Query("DELETE FROM santri WHERE id = :id")
    suspend fun deleteSantriById(id: Int)
}

@Dao
interface GaleriDao {
    @Query("SELECT * FROM galeri ORDER BY createdAt DESC")
    fun getAllGaleri(): Flow<List<Galeri>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGaleri(galeri: Galeri): Long

    @Delete
    suspend fun deleteGaleri(galeri: Galeri)

    @Query("DELETE FROM galeri WHERE id = :id")
    suspend fun deleteGaleriById(id: Int)
}

@Dao
interface SettingsDao {
    @Query("SELECT * FROM settings WHERE id = 1 LIMIT 1")
    fun getSettings(): Flow<SettingsEntity?>

    @Query("SELECT * FROM settings WHERE id = 1 LIMIT 1")
    suspend fun getSettingsDirect(): SettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateSettings(settings: SettingsEntity)
}
