package org.bibletranslationtools.docscanner.data.repository

import android.app.Application
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import org.bibletranslationtools.docscanner.data.local.PdfDatabase
import org.bibletranslationtools.docscanner.data.models.PdfEntity

interface PdfRepository {
    fun getPdfList(): Flow<List<PdfEntity>>
    suspend fun insertPdf(pdfEntity: PdfEntity): Long
    suspend fun deletePdf(pdfEntity: PdfEntity): Int
    suspend fun updatePdf(pdfEntity: PdfEntity): Int
}

class PdfRepositoryImpl(application: Application): PdfRepository {
    private val pdfDao = PdfDatabase.getInstance(application).pdfDao

    override fun getPdfList() = pdfDao.getAllPdfs().flowOn(Dispatchers.IO)

    override suspend fun insertPdf(pdfEntity: PdfEntity): Long {
        return pdfDao.insertPdf(pdfEntity)
    }

    override suspend fun deletePdf(pdfEntity: PdfEntity): Int {
        return pdfDao.deletePdf(pdfEntity)
    }

    override suspend fun updatePdf(pdfEntity: PdfEntity): Int {
        return pdfDao.updatePdf(pdfEntity)
    }
}