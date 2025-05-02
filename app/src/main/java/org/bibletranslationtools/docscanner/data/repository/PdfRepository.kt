package org.bibletranslationtools.docscanner.data.repository

import android.app.Application
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import org.bibletranslationtools.docscanner.data.local.DocScanDatabase
import org.bibletranslationtools.docscanner.data.models.Pdf
import org.bibletranslationtools.docscanner.data.models.Project

interface PdfRepository {
    fun getProjectPdfs(project: Project): Flow<List<Pdf>>
    suspend fun insert(pdf: Pdf): Long
    suspend fun delete(pdf: Pdf): Int
    suspend fun update(pdf: Pdf): Int
}

class PdfRepositoryImpl(application: Application): PdfRepository {
    private val pdfDao = DocScanDatabase.getInstance(application).pdfDao

    override fun getProjectPdfs(project: Project) =
        pdfDao.getProjectPdfs(project.id).flowOn(Dispatchers.IO)

    override suspend fun insert(pdf: Pdf): Long {
        return pdfDao.insert(pdf)
    }

    override suspend fun delete(pdf: Pdf): Int {
        return pdfDao.delete(pdf)
    }

    override suspend fun update(pdf: Pdf): Int {
        return pdfDao.update(pdf)
    }
}