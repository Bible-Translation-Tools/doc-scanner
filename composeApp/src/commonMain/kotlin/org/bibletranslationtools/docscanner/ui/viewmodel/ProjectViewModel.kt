package org.bibletranslationtools.docscanner.ui.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import androidx.core.graphics.createBitmap
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import docscanner.composeapp.generated.resources.Res
import docscanner.composeapp.generated.resources.create_pdf_failed
import docscanner.composeapp.generated.resources.creating_pdf
import docscanner.composeapp.generated.resources.delete_pdf_confirm
import docscanner.composeapp.generated.resources.delete_pdf_failed
import docscanner.composeapp.generated.resources.deleting_pdf
import docscanner.composeapp.generated.resources.loading_pdfs
import docscanner.composeapp.generated.resources.preparing_images
import docscanner.composeapp.generated.resources.rename_pdf_failed
import docscanner.composeapp.generated.resources.renaming_pdf
import docscanner.composeapp.generated.resources.upload_images_failed
import docscanner.composeapp.generated.resources.upload_images_success
import docscanner.composeapp.generated.resources.uploading_images
import io.github.oshai.kotlinlogging.KotlinLogging
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
import kotlinx.io.asOutputStream
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readByteArray
import org.bibletranslationtools.docscanner.api.HtrUser
import org.bibletranslationtools.docscanner.api.ImageRequest
import org.bibletranslationtools.docscanner.api.TranscriberApi
import org.bibletranslationtools.docscanner.data.models.Alert
import org.bibletranslationtools.docscanner.data.models.Image
import org.bibletranslationtools.docscanner.data.models.Pdf
import org.bibletranslationtools.docscanner.data.models.Progress
import org.bibletranslationtools.docscanner.data.models.Project
import org.bibletranslationtools.docscanner.data.models.getName
import org.bibletranslationtools.docscanner.data.models.getRepo
import org.bibletranslationtools.docscanner.data.repository.DirectoryProvider
import org.bibletranslationtools.docscanner.data.repository.PdfRepository
import org.bibletranslationtools.docscanner.ui.common.ConfirmAction
import org.bibletranslationtools.docscanner.ui.screens.project.components.UploadStatus
import org.bibletranslationtools.docscanner.utils.FileUtils
import org.jetbrains.compose.resources.getString
import java.io.File
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

data class ProjectState(
    val user: HtrUser? = null,
    val pdfs: List<Pdf> = emptyList(),
    val confirmAction: ConfirmAction? = null,
    val alert: Alert? = null,
    val progress: Progress? = null,
    val uploadStatus: UploadStatus? = null
)

sealed class ProjectEvent {
    data object Idle : ProjectEvent()
    data class CreatePdf(val pdfUri: Uri, val context: Context) : ProjectEvent()
    data class RenamePdf(val pdf: Pdf, val newName: String) : ProjectEvent()
    data class DeletePdf(val pdf: Pdf) : ProjectEvent()
    data class OpenPdf(val pdf: Pdf, val context: Context) : ProjectEvent()
    data class PdfOpened(val uri: Uri) : ProjectEvent()
    data class UploadImages(val images: List<Image>) : ProjectEvent()
    data class ExtractImages(val pdf: Pdf): ProjectEvent()
    data class ImagesExtracted(val images: List<Image>): ProjectEvent()
}

class ProjectViewModel(
    private val project: Project,
    private val directoryProvider: DirectoryProvider,
    private val pdfRepository: PdfRepository,
    private val transcriberApi: TranscriberApi
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

    private val logger = KotlinLogging.logger {}

    fun onEvent(event: ProjectEvent) {
        when (event) {
            is ProjectEvent.CreatePdf -> createPdf(event.pdfUri, event.context)
            is ProjectEvent.RenamePdf -> renamePdf(event.pdf, event.newName)
            is ProjectEvent.DeletePdf -> deletePdf(event.pdf)
            is ProjectEvent.OpenPdf -> openPdf(event.pdf, event.context)
            is ProjectEvent.UploadImages -> uploadImages(event.images)
            is ProjectEvent.ExtractImages -> extractImages(event.pdf)
            else -> resetChannel()
        }
    }

    private fun initialize() {
        screenModelScope.launch(Dispatchers.IO) {
            updateProgress(Progress(-1f, getString(Res.string.loading_pdfs)))

            updateUser(transcriberApi.getUser())

            loadPdfs()
            updateProgress(null)
        }
    }

    private fun loadPdfs() {
        updatePdfs(pdfRepository.getAll(project))
    }

    private fun createPdf(pdfUri: Uri, context: Context) {
        screenModelScope.launch(Dispatchers.IO) {
            updateProgress(Progress(-1f, getString(Res.string.creating_pdf)))

            val lastPdfId = pdfRepository.lastId()
            val newPdfId = lastPdfId + 1

            val date = Clock.System.now()
            val localDate = date.toLocalDateTime(TimeZone.currentSystemDefault())
            val pdfName = "${project.getName()}_$newPdfId"
            val pdfFileName = "$pdfName.pdf"
            val projectDir = Path(directoryProvider.projectsDir, project.getName())
            val pdfFile = Path(projectDir, pdfFileName)

            FileUtils.writeUriToPath(context, pdfUri, pdfFile)

            val repo = project.getRepo(directoryProvider)
            _state.value.user?.let { user ->
                repo.setAuthor(user.wacsUsername, user.wacsUserEmail)
            }
            repo.commit()

            val pdf = Pdf(
                name = pdfFileName,
                size = FileUtils.getFileSize(pdfFile),
                created = localDate.toString(),
                modified = localDate.toString(),
                projectId = project.id
            )

            try {
                pdfRepository.insert(pdf)
            } catch (e: Exception) {
                val error = getString(Res.string.create_pdf_failed)
                logger.error(e) { error }

                updateAlert(
                    Alert(error) { updateAlert(null) }
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

    @OptIn(ExperimentalEncodingApi::class, ExperimentalUuidApi::class)
    private fun uploadImages(images: List<Image>) {
        screenModelScope.launch {
            if (images.isEmpty()) return@launch

            updateProgress(Progress(0f, getString(Res.string.uploading_images)))

            val total = images.size
            var uploaded = 0

            try {
                images.forEachIndexed { index, image ->
                    val path = Path(image.path)
                    SystemFileSystem.source(path).buffered().use { source ->
                        val bytes = source.readByteArray()
                        val base64 = "data:image/jpeg;base64,${Base64.Default.encode(bytes)}"
                        val imageId = Uuid.random().toString()
                        val timestampPattern = "\\d+".toRegex()
                        val created = timestampPattern
                            .find(path.name)?.value?.toLong() ?: Clock.System.now().epochSeconds

                        val imageRequest = ImageRequest(
                            image = base64,
                            imageId = imageId,
                            filename = path.name,
                            languageCode = project.language.slug,
                            bookCode = project.book.slug,
                            chapter = image.chapter,
                            created = created
                        )
                        val response = transcriberApi.uploadImage(imageRequest)

                        if (response?.success == true) {
                            uploaded++
                            logger.info { "Image uploaded: ${path.name}" }
                        } else {
                            logger.warn { "Image upload failed: ${path.name}" }
                        }
                    }

                    updateProgress(
                        Progress(
                            (index+1).toFloat() / total,
                            getString(Res.string.uploading_images)
                        )
                    )
                }
            } catch (e: Exception) {
                val error = getString(Res.string.upload_images_failed)
                logger.error(e) { error }

                updateAlert(
                    Alert(error) { updateAlert(null) }
                )
            }

            updateProgress(null)

            val message: String
            val url: String?
            if (uploaded == total) {
                message = getString(Res.string.upload_images_success)
                url = TranscriberApi.BASE_URL
            } else {
                message = getString(Res.string.upload_images_failed)
                url = null
            }

            updateUploadStatus(
                UploadStatus(
                    message = message,
                    url = url,
                    onDismiss = { updateUploadStatus(null) }
                )
            )
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
            val error = getString(Res.string.delete_pdf_failed)
            logger.error(e) { error }

            updateAlert(
                Alert(error) { updateAlert(null) }
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
                    val projectDir = Path(directoryProvider.projectsDir, project.getName())

                    val oldFile = Path(projectDir, pdf.name)
                    val newFile = Path(projectDir, newNameNormalized)
                    FileUtils.renamePath(oldFile, newFile)

                    val now = Clock.System.now()
                        .toLocalDateTime(TimeZone.currentSystemDefault())

                    val newPdf = pdf.copy(
                        name = newNameNormalized,
                        modified = now.toString()
                    )
                    updatePdf(newPdf)
                } catch (e: Exception) {
                    val error = getString(Res.string.rename_pdf_failed)
                    logger.error(e) { error }

                    updateAlert(
                        Alert(error) { updateAlert(null) }
                    )
                }

                updateProgress(Progress(-1f, getString(Res.string.loading_pdfs)))
                loadPdfs()
                updateProgress(null)
            }
        }
    }

    private fun extractImages(pdf: Pdf) {
        screenModelScope.launch(Dispatchers.IO) {
            updateProgress(Progress(-1f, getString(Res.string.preparing_images)))

            val projectDir = Path(directoryProvider.projectsDir, project.getName())
            val pdfPath = Path(projectDir, pdf.name)
            val pdfFile = File(pdfPath.toString())

            val images = mutableListOf<Image>()

            ParcelFileDescriptor.open(
                pdfFile,
                ParcelFileDescriptor.MODE_READ_ONLY
            ).use { descriptor ->
                PdfRenderer(descriptor).use { renderer ->
                    for (index in 0 until renderer.pageCount) {
                        renderer.openPage(index).use { page ->
                            val bitmap = createBitmap(page.width, page.height)
                            page.render(
                                bitmap,
                                null,
                                null,
                                PdfRenderer.Page.RENDER_MODE_FOR_PRINT
                            )

                            val imageFile = directoryProvider.createTempFile(
                                "image",
                                ".jpg"
                            )
                            SystemFileSystem.sink(imageFile).buffered().use {
                                it.asOutputStream().use { out ->
                                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
                                }
                            }
                            images.add(
                                Image(path = imageFile.toString(), chapter = 1)
                            )
                            bitmap.recycle()
                        }
                    }
                }
            }

            updateProgress(null)

            _event.send(ProjectEvent.ImagesExtracted(images))
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

    private fun updateUser(user: HtrUser?) {
        _state.update {
            it.copy(user = user)
        }
    }

    private fun updateUploadStatus(status: UploadStatus?) {
        _state.update {
            it.copy(uploadStatus = status)
        }
    }

    private fun resetChannel() {
        screenModelScope.launch {
            _event.send(ProjectEvent.Idle)
        }
    }
}