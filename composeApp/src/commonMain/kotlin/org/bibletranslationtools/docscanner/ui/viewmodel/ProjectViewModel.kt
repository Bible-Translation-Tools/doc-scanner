package org.bibletranslationtools.docscanner.ui.viewmodel

import android.content.Context
import android.net.Uri
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import docscanner.composeapp.generated.resources.Res
import docscanner.composeapp.generated.resources.create_pdf_failed
import docscanner.composeapp.generated.resources.creating_pdf
import docscanner.composeapp.generated.resources.delete_pdf_confirm
import docscanner.composeapp.generated.resources.delete_pdf_failed
import docscanner.composeapp.generated.resources.deleting_pdf
import docscanner.composeapp.generated.resources.loading_pdfs
import docscanner.composeapp.generated.resources.rename_pdf_failed
import docscanner.composeapp.generated.resources.renaming_pdf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import org.bibletranslationtools.docscanner.data.local.DirectoryProvider
import org.bibletranslationtools.docscanner.data.local.Settings
import org.bibletranslationtools.docscanner.data.local.git.Profile
import org.bibletranslationtools.docscanner.data.models.Alert
import org.bibletranslationtools.docscanner.data.models.Pdf
import org.bibletranslationtools.docscanner.data.models.Progress
import org.bibletranslationtools.docscanner.data.models.Project
import org.bibletranslationtools.docscanner.data.models.getName
import org.bibletranslationtools.docscanner.data.models.getRepo
import org.bibletranslationtools.docscanner.data.repository.PdfRepository
import org.bibletranslationtools.docscanner.data.repository.PreferenceRepository
import org.bibletranslationtools.docscanner.data.repository.getPref
import org.bibletranslationtools.docscanner.ui.common.ConfirmAction
import org.bibletranslationtools.docscanner.utils.FileUtils
import org.bibletranslationtools.docscanner.utils.format
import org.jetbrains.compose.resources.getString
import org.json.JSONObject

data class ProjectState(
    val profile: Profile? = null,
    val pdfs: List<Pdf> = emptyList(),
    val confirmAction: ConfirmAction? = null,
    val alert: Alert? = null,
    val progress: Progress? = null,
)

sealed class ProjectEvent {
    data object Idle : ProjectEvent()
    data class CreatePdf(val pdfUri: Uri, val context: Context): ProjectEvent()
    data class RenamePdf(val pdf: Pdf, val newName: String) : ProjectEvent()
    data class DeletePdf(val pdf: Pdf): ProjectEvent()
    data class OpenPdf(val pdf: Pdf, val context: Context): ProjectEvent()
    data class PdfOpened(val uri: Uri): ProjectEvent()
}

class ProjectViewModel(
    private val project: Project,
    private val directoryProvider: DirectoryProvider,
    private val preferenceRepository: PreferenceRepository,
    private val pdfRepository: PdfRepository
) : ScreenModel {

    private var _state = MutableStateFlow(ProjectState())
    val state: StateFlow<ProjectState> = _state
        .onStart { initialize() }
        .stateIn(
            scope = screenModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ProjectState()
        )

    private val _event: Channel<ProjectEvent> = Channel()
    val event = _event.receiveAsFlow()

    fun onEvent(event: ProjectEvent) {
        when (event) {
            is ProjectEvent.CreatePdf -> createPdf(event.pdfUri, event.context)
            is ProjectEvent.RenamePdf -> renamePdf(event.pdf, event.newName)
            is ProjectEvent.DeletePdf -> deletePdf(event.pdf)
            is ProjectEvent.OpenPdf -> openPdf(event.pdf, event.context)
            else -> resetChannel()
        }
    }

    private fun initialize() {
        screenModelScope.launch(Dispatchers.IO) {
            updateProgress(Progress(-1f, getString(Res.string.loading_pdfs)))

            preferenceRepository.getPref<String>(Settings.KEY_PREF_PROFILE)?.let {
                updateProfile(
                    Profile.fromJSON(JSONObject(it))
                )
            }

            loadPdfs()
            updateProgress(null)
        }
    }

    private fun loadPdfs() {
        updatePdfs(pdfRepository.getAll(project))
    }

    private fun createPdf(
        pdfUri: Uri,
        context: Context
    ) {
        screenModelScope.launch(Dispatchers.IO) {
            updateProgress(Progress(-1f, getString(Res.string.creating_pdf)))

            val date = Clock.System.now()
                .toLocalDateTime(TimeZone.currentSystemDefault())
            val fileName = "${date.format()}.pdf"

            FileUtils.copyPdfFileToAppDirectory(
                context,
                directoryProvider,
                pdfUri,
                fileName,
                project
            )

            val repo = project.getRepo(directoryProvider)
            _state.value.profile?.gogsUser?.let { user ->
                repo.setAuthor(user.username, user.email)
            }
            repo.commit()

            val pdf = Pdf(
                name = fileName,
                size = FileUtils.getFileSize(directoryProvider, fileName, project),
                created = date.toString(),
                modified = date.toString(),
                projectId = project.id
            )

            try {
                pdfRepository.insert(pdf)
            } catch (e: Exception) {
                e.printStackTrace()
                updateAlert(
                    Alert(getString(Res.string.create_pdf_failed)) {
                        updateAlert(null)
                    }
                )
            }

            updateProgress(Progress(-1f, getString(Res.string.loading_pdfs)))
            loadPdfs()
            updateProgress(null)
        }
    }

    private fun openPdf(
        pdf: Pdf,
        context: Context,
    ) {
        screenModelScope.launch {
            val uri = FileUtils.getPdfUri(
                context,
                directoryProvider,
                pdf.name,
                project
            )
            _event.send(ProjectEvent.PdfOpened(uri))
        }
    }

    private fun deletePdf(pdf: Pdf) {
        screenModelScope.launch {
            updateConfirmAction(
                ConfirmAction(
                    message = getString(Res.string.delete_pdf_confirm),
                    onConfirm = {
                        screenModelScope.launch {
                            updateProgress(Progress(-1f, getString(Res.string.deleting_pdf)))
                            doDeletePdf(pdf)

                            updateProgress(Progress(-1f, getString(Res.string.loading_pdfs)))
                            loadPdfs()

                            updateProgress(null)
                        }
                    },
                    onCancel = {
                        updateConfirmAction(null)
                    }
                )
            )
        }
    }

    private suspend fun doDeletePdf(pdf: Pdf) {
        try {
            val file = Path(directoryProvider.projectsDir, project.getName(), pdf.name)
            SystemFileSystem.delete(file)
            pdfRepository.delete(pdf)
        } catch (e: Exception) {
            e.printStackTrace()
            updateAlert(
                Alert(getString(Res.string.delete_pdf_failed)) {
                    updateAlert(null)
                }
            )
        }
    }

    private fun renamePdf(pdf: Pdf, newName: String) {
        screenModelScope.launch(Dispatchers.IO) {
            val newNameNormalized = if (!newName.endsWith(".pdf")) {
                "$newName.pdf"
            } else {
                newName
            }

            if (!pdf.name.equals(newNameNormalized, true)) {
                updateProgress(Progress(-1f, getString(Res.string.renaming_pdf)))

                try {
                    val oldFile = Path(
                        directoryProvider.projectsDir,
                        project.getName(),
                        pdf.name
                    )
                    val newFile = Path(
                        directoryProvider.projectsDir,
                        project.getName(),
                        newNameNormalized
                    )
                    SystemFileSystem.atomicMove(oldFile, newFile)

                    val now = Clock.System.now()
                        .toLocalDateTime(TimeZone.currentSystemDefault())

                    val newPdf = pdf.copy(
                        name = newNameNormalized,
                        modified = now.toString()
                    )
                    updatePdf(newPdf)
                } catch (e: Exception) {
                    e.printStackTrace()
                    updateAlert(
                        Alert(getString(Res.string.rename_pdf_failed)) {
                            updateAlert(null)
                        }
                    )
                }

                updateProgress(Progress(-1f, getString(Res.string.loading_pdfs)))
                loadPdfs()
                updateProgress(null)
            }
        }
    }

    private fun updatePdf(pdf: Pdf) {
        screenModelScope.launch(Dispatchers.IO) {
            pdfRepository.update(pdf)
        }
    }

    private fun updatePdfs(pdfs: List<Pdf>) {
        _state.update {
            it.copy(pdfs = pdfs)
        }
    }

    private fun updateConfirmAction(confirmAction: ConfirmAction?) {
        _state.update {
            it.copy(confirmAction = confirmAction)
        }
    }

    private fun updateProgress(progress: Progress?) {
        _state.update {
            it.copy(progress = progress)
        }
    }

    private fun updateAlert(alert: Alert?) {
        _state.update {
            it.copy(alert = alert)
        }
    }

    private fun updateProfile(profile: Profile?) {
        _state.update {
            it.copy(profile = profile)
        }
    }

    private fun resetChannel() {
        screenModelScope.launch {
            _event.send(ProjectEvent.Idle)
        }
    }
}