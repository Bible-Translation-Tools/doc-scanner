package org.bibletranslationtools.docscanner.data.repository

import org.bibletranslationtools.database.MainDatabase
import org.bibletranslationtools.docscanner.data.models.Book
import org.bibletranslationtools.docscanner.data.models.toEntity
import org.bibletranslationtools.docscanner.data.models.toModel

interface BookRepository {
    fun getAll(): List<Book>
    suspend fun deleteAll()
    suspend fun insert(book: Book)
    suspend fun delete(book: Book)
    suspend fun update(book: Book)
}

class BookRepositoryImpl(db: MainDatabase) : BookRepository {
    private val queries = db.bookQueries

    override fun getAll() = queries.getAll().executeAsList().map { it.toModel() }

    override suspend fun deleteAll() = queries.deleteAll()

    override suspend fun insert(book: Book) {
        val entity = book.toEntity()
        return queries.add(
            entity.slug,
            entity.name,
            entity.anthology,
            entity.sort
        )
    }

    override suspend fun delete(book: Book) {
        val entity = book.toEntity()
        return queries.delete(entity.id)
    }

    override suspend fun update(book: Book) {
        val entity = book.toEntity()
        return queries.update(
            entity.id,
            entity.slug,
            entity.name,
            entity.anthology,
            entity.sort
        )
    }
}