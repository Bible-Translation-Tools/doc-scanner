package org.bibletranslationtools.docscanner.data.repository

import android.app.Application
import org.bibletranslationtools.docscanner.data.local.DocScanDatabase
import org.bibletranslationtools.docscanner.data.models.Book

interface BookRepository {
    fun getAllBooks(): List<Book>
    fun getOtBooks(): List<Book>
    fun getNtBooks(): List<Book>
    suspend fun deleteAll(): Int
    suspend fun insert(book: Book): Long
    suspend fun delete(book: Book): Int
    suspend fun update(book: Book): Int
}

class BookRepositoryImpl(application: Application) : BookRepository {
    private val bookDao = DocScanDatabase.getInstance(application).bookDao

    override fun getAllBooks() = bookDao.getAllBooks()

    override fun getOtBooks() = bookDao.getOtBooks()

    override fun getNtBooks() = bookDao.getNtBooks()

    override suspend fun deleteAll() = bookDao.deleteAll()

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