package org.bibletranslationtools.docscanner.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import org.bibletranslationtools.docscanner.data.models.Language

@Dao
interface LanguageDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(language: Language): Long

    @Delete
    suspend fun delete(language: Language): Int

    @Update
    suspend fun update(language: Language): Int

    @Query("SELECT * FROM languages")
    fun getAllLanguages(): List<Language>

    @Query("SELECT * FROM languages WHERE gw=1")
    fun getGlLanguages(): List<Language>

    @Query("SELECT * FROM languages WHERE gw=0")
    fun getHeartLanguages(): List<Language>

    @Query("DELETE FROM languages")
    fun deleteAll(): Int
}