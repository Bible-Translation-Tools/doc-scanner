package org.bibletranslationtools.docscanner.data.repository

import android.app.Application
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import org.bibletranslationtools.docscanner.data.local.DocScanDatabase
import org.bibletranslationtools.docscanner.data.models.Book

interface BookRepository {
    fun getAllBooks(): Flow<List<Book>>
    fun getOtBooks(): Flow<List<Book>>
    fun getNtBooks(): Flow<List<Book>>
    suspend fun insert(book: Book): Long
    suspend fun delete(book: Book): Int
    suspend fun update(book: Book): Int
}

class BookRepositoryImpl(application: Application) : BookRepository {
    private val bookDao = DocScanDatabase.getInstance(application).bookDao

    override fun getAllBooks() = bookDao.getAllBooks().flowOn(Dispatchers.IO)

    override fun getOtBooks() = bookDao.getOtBooks().flowOn(Dispatchers.IO)

    override fun getNtBooks() = bookDao.getNtBooks().flowOn(Dispatchers.IO)

    override suspend fun insert(book: Book): Long {
        return bookDao.insert(book)
    }

    override suspend fun delete(book: Book): Int {
        return bookDao.delete(book)
    }

    override suspend fun update(book: Book): Int {
        return bookDao.update(book)
    }
}