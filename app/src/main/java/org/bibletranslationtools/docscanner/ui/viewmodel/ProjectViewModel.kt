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
import org.bibletranslationtools.docscanner.utils.FileUtilities
import org.bibletranslationtools.docscanner.data.local.DirectoryProvider
import org.bibletranslationtools.docscanner.data.models.Pdf
import org.bibletranslationtools.docscanner.data.models.Project
import org.bibletranslationtools.docscanner.data.models.ProjectWithData
import org.bibletranslationtools.docscanner.data.repository.PdfRepository
import java.util.Date

class ProjectViewModel(
    private val project: ProjectWithData,
    private val directoryProvider: DirectoryProvider,
    private val pdfRepository: PdfRepository
) : ScreenModel {

    var loadingDialog by mutableStateOf(false)

    private val _pdfState = MutableStateFlow<List<Pdf>>(arrayListOf())
    val pdfState: StateFlow<List<Pdf>>
        get() = _pdfState

    init {
        screenModelScope.launch(Dispatchers.IO) {
            pdfRepository.getProjectPdfs(project.project).catch {
                it.printStackTrace()
            }.collect {
                _pdfState.emit(it)
            }
        }
    }

    fun insertPdf(pdf: Pdf) {
        screenModelScope.launch(Dispatchers.IO) {
            pdfRepository.insert(pdf)
        }
    }

    fun deletePdf(pdf: Pdf) {
        screenModelScope.launch(Dispatchers.IO) {
            if (FileUtilities.deletePdf(directoryProvider, pdf.name, project)) {
                pdfRepository.delete(pdf)
            }
        }
    }

    fun renamePdf(pdf: Pdf, newName: String) {
        screenModelScope.launch(Dispatchers.IO) {
            if (!pdf.name.equals(newName, true)) {
                FileUtilities.renamePdf(
                    directoryProvider,
                    pdf.name,
                    newName,
                    project
                )
                val updatePdf = pdf.copy(
                    name = newName,
                    lastModified = Date()
                )
                updatePdf(updatePdf)
            }
        }
    }

    private fun updatePdf(pdf: Pdf) {
        screenModelScope.launch(Dispatchers.IO) {
            pdfRepository.update(pdf)
        }
    }
}