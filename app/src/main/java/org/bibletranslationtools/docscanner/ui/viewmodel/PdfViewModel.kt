package org.bibletranslationtools.docscanner.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import org.bibletranslationtools.docscanner.FileUtilities
import org.bibletranslationtools.docscanner.data.local.DirectoryProvider
import org.bibletranslationtools.docscanner.data.models.PdfEntity
import org.bibletranslationtools.docscanner.data.repository.PdfRepository
import java.util.Date

class PdfViewModel(
    private val directoryProvider: DirectoryProvider,
    private val pdfRepository: PdfRepository
) : ScreenModel {

    var loadingDialog by mutableStateOf(false)

    private val _pdfStateFlow = MutableStateFlow<List<PdfEntity>>(arrayListOf())

    val pdfStateFlow: StateFlow<List<PdfEntity>>
        get() = _pdfStateFlow

    init {
        screenModelScope.launch(Dispatchers.IO) {
            pdfRepository.getPdfList().catch {
                it.printStackTrace()
            }.collect {
                _pdfStateFlow.emit(it)
            }
        }
    }

    fun insertPdf(pdfEntity: PdfEntity) {
        screenModelScope.launch(Dispatchers.IO) {
            pdfRepository.insertPdf(pdfEntity)
        }
    }

    fun uploadPdf(pdfEntity: PdfEntity) {
        println("upload")
    }

    fun deletePdf(pdfEntity: PdfEntity) {
        screenModelScope.launch(Dispatchers.IO) {
            if (FileUtilities.deleteFile(directoryProvider, pdfEntity.name)) {
                pdfRepository.deletePdf(pdfEntity)
            }
        }
    }

    fun renamePdf(pdfEntity: PdfEntity, newName: String) {
        screenModelScope.launch(Dispatchers.IO) {
            if (!pdfEntity.name.equals(newName, true)) {
                FileUtilities.renameFile(
                    directoryProvider,
                    pdfEntity.name,
                    newName
                )
                val updatePdf = pdfEntity.copy(
                    name = newName,
                    lastModifiedTime = Date()
                )
                updatePdf(updatePdf)
            }
        }
    }

    private fun updatePdf(pdfEntity: PdfEntity) {
        screenModelScope.launch(Dispatchers.IO) {
            pdfRepository.updatePdf(pdfEntity)
        }
    }
}