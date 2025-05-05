package org.bibletranslationtools.docscanner.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import org.bibletranslationtools.docscanner.data.models.Level

@Dao
interface LevelDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(level: Level): Long

    @Delete
    suspend fun delete(level: Level): Int

    @Update
    suspend fun update(level: Level): Int

    @Query("SELECT * FROM levels")
    fun getAllLevels(): Flow<List<Level>>
}