package org.bibletranslationtools.docscanner.data.repository

import org.bibletranslationtools.database.MainDatabase
import org.bibletranslationtools.docscanner.data.models.Image
import org.bibletranslationtools.docscanner.data.models.Pdf
import org.bibletranslationtools.docscanner.data.models.Project
import org.bibletranslationtools.docscanner.data.models.toEntity
import org.bibletranslationtools.docscanner.data.models.toModel

interface PdfRepository {
    fun getAll(project: Project): List<Pdf>
    suspend fun insert(pdf: Pdf)
    suspend fun delete(pdf: Pdf)
    suspend fun update(pdf: Pdf)
    suspend fun lastId(): Long
    suspend fun insertImage(image: Image)
    suspend fun deleteImage(image: Image)
    suspend fun updateImage(image: Image)
}

class PdfRepositoryImpl(db: MainDatabase): PdfRepository {
    private val queries = db.pdfQueries
    private val imageQueries = db.imageQueries

    override fun getAll(project: Project): List<Pdf> {
        return queries.getAll(project.id.toLong()).executeAsList().map { pdf ->
            val images = imageQueries.getAll(pdf.id).executeAsList()
            pdf.toModel(images.map { it.toModel() })
        }
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

    override suspend fun insertImage(image: Image) {
        val entity = image.toEntity()
        return imageQueries.add(
            entity.name,
            entity.size,
            entity.created,
            entity.pdfId
        )
    }

    override suspend fun deleteImage(image: Image) {
        val entity = image.toEntity()
        return imageQueries.delete(entity.id)
    }

    override suspend fun updateImage(image: Image) {
        val entity = image.toEntity()
        return imageQueries.update(
            entity.id,
            entity.name,
            entity.size,
            entity.created,
            entity.pdfId
        )
    }
}