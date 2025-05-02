package org.bibletranslationtools.docscanner.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import org.bibletranslationtools.docscanner.data.models.Project

@Dao
interface ProjectDao {

//    Insert
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(project: Project) : Long

//    Delete
    @Delete
    suspend fun delete(project: Project) : Int

//    Update
    @Update
    suspend fun update(project: Project) : Int

//    Query
    @Query("SELECT * FROM projects")
    fun getAllProjects(): Flow<List<Project>>
}