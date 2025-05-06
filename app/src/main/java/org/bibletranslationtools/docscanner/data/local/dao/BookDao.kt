package org.bibletranslationtools.docscanner.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import org.bibletranslationtools.docscanner.data.models.Book

@Dao
interface BookDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(book: Book): Long

    @Delete
    suspend fun delete(book: Book): Int

    @Update
    suspend fun update(book: Book): Int

    @Query("SELECT * FROM books")
    fun getAllBooks(): List<Book>

    @Query("SELECT * FROM books WHERE anthology='ot'")
    fun getOtBooks(): List<Book>

    @Query("SELECT * FROM books WHERE anthology='nt'")
    fun getNtBooks(): List<Book>

    @Query("DELETE FROM books")
    fun deleteAll(): Int
}