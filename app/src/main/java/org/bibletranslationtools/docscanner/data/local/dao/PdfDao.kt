package org.bibletranslationtools.docscanner.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import org.bibletranslationtools.docscanner.data.models.Pdf

@Dao
interface PdfDao {

    //    Insert
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(pdf: Pdf): Long

    //    Delete
    @Delete
    suspend fun delete(pdf: Pdf): Int

    //    Update
    @Update
    suspend fun update(pdf: Pdf): Int

    //    Query
    @Query("SELECT * FROM pdfs WHERE project_id=:projectId")
    fun getProjectPdfs(projectId: String): Flow<List<Pdf>>
}