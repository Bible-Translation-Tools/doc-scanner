package org.bibletranslationtools.docscanner.data.repository

import org.bibletranslationtools.database.MainDatabase
import org.bibletranslationtools.docscanner.data.models.Pdf
import org.bibletranslationtools.docscanner.data.models.Project
import org.bibletranslationtools.docscanner.data.models.toEntity
import org.bibletranslationtools.docscanner.data.models.toModel

interface PdfRepository {
    fun getAll(project: Project): List<Pdf>
    fun get(id: Long, withImages: Boolean): Pdf?
    suspend fun insert(pdf: Pdf)
    suspend fun delete(pdf: Pdf)
    suspend fun update(pdf: Pdf)
    suspend fun lastId(): Long
}

class PdfRepositoryImpl(db: MainDatabase): PdfRepository {
    private val queries = db.pdfQueries

    override fun getAll(project: Project): List<Pdf> {
        return queries.getAll(project.id).executeAsList().map { it.toModel() }
    }

    override fun get(id: Long, withImages: Boolean): Pdf? {
        return queries.get(id).executeAsOneOrNull()?.toModel()
    }

    override suspend fun insert(pdf: Pdf) {
        val entity = pdf.toEntity()
        return queries.add(
            entity.name,
            entity.size,
            entity.projectId,
            entity.created,
            entity.modified
        )
    }

    override suspend fun delete(pdf: Pdf) {
        val entity = pdf.toEntity()
        return queries.delete(entity.id)
    }

    override suspend fun update(pdf: Pdf) {
        val entity = pdf.toEntity()
        return queries.update(
            entity.id,
            entity.name,
            entity.size,
            entity.projectId,
            entity.created,
            entity.modified
        )
    }

    override suspend fun lastId() = queries.lastId().executeAsOne().MAX ?: 0
}