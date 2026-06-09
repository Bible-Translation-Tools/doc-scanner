package org.bibletranslationtools.docscanner.platform

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts.StartIntentSenderForResult
import androidx.core.content.FileProvider
import androidx.core.graphics.createBitmap
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.RESULT_FORMAT_PDF
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.SCANNER_MODE_FULL
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import de.jonasbroeckmann.kzip.Zip
import de.jonasbroeckmann.kzip.compressFrom
import de.jonasbroeckmann.kzip.open
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.io.asOutputStream
import kotlinx.io.asSource
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import org.bibletranslationtools.docscanner.data.models.Image
import org.bibletranslationtools.docscanner.data.repository.DirectoryProvider
import java.io.File

private val logger = KotlinLogging.logger {}

@Composable
actual fun rememberDocumentScannerLauncher(
    directoryProvider: DirectoryProvider,
    onResult: (Path?) -> Unit
): DocumentScannerLauncher {
    val activity = LocalActivity.current
    val context = LocalContext.current

    val scanner = remember {
        GmsDocumentScanning.getClient(
            GmsDocumentScannerOptions.Builder()
                .setPageLimit(1000)
                .setGalleryImportAllowed(true)
                .setResultFormats(RESULT_FORMAT_PDF)
                .setScannerMode(SCANNER_MODE_FULL)
                .build()
        )
    }

    val launcher = rememberLauncherForActivityResult(StartIntentSenderForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val scanningResult = GmsDocumentScanningResult.fromActivityResultIntent(result.data)
            val pdfUri = scanningResult?.pdf?.uri
            if (pdfUri != null) {
                val tempPath = copyUriToCache(context, pdfUri, directoryProvider)
                onResult(tempPath)
            } else {
                onResult(null)
            }
        } else {
            onResult(null)
        }
    }

    return remember(activity) {
        object : DocumentScannerLauncher {
            override fun launch() {
                val act = activity ?: return onResult(null)
                scanner.getStartScanIntent(act)
                    .addOnSuccessListener { intentSender ->
                        launcher.launch(IntentSenderRequest.Builder(intentSender).build())
                    }
                    .addOnFailureListener { e ->
                        logger.error(e) { "Failed to start document scanner" }
                        onResult(null)
                    }
            }
        }
    }
}

actual fun isDocumentScannerAvailable(): Boolean = true

private fun copyUriToCache(
    context: android.content.Context,
    uri: Uri,
    directoryProvider: DirectoryProvider
): Path? {
    return try {
        val tempPath = directoryProvider.createTempFile("scan", ".pdf")
        context.contentResolver.openInputStream(uri)?.use { input ->
            SystemFileSystem.sink(tempPath).buffered().use { sink ->
                input.asSource().buffered().transferTo(sink)
            }
        }
        tempPath
    } catch (e: Exception) {
        logger.error(e) { "Failed to copy scanned PDF to cache" }
        null
    }
}

actual fun renderPdfToImages(
    pdfPath: Path,
    directoryProvider: DirectoryProvider
): List<Image> {
    val images = mutableListOf<Image>()
    val pdfFile = File(pdfPath.toString())

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

                    val imageFile = directoryProvider.createTempFile("image", ".jpg")
                    SystemFileSystem.sink(imageFile).buffered().use { sink ->
                        sink.asOutputStream().use { out ->
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
                        }
                    }
                    images.add(Image(path = imageFile.toString(), chapter = 1))
                    bitmap.recycle()
                }
            }
        }
    }

    return images
}

@Composable
actual fun rememberFileSharer(): FileSharer {
    val context = LocalContext.current
    return remember {
        object : FileSharer {
            override fun share(path: Path) {
                val uri = uriFor(context, path)
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = mimeFor(path)
                    putExtra(Intent.EXTRA_STREAM, uri)
                    flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                }
                context.startActivity(Intent.createChooser(intent, "share"))
            }

            override fun open(path: Path) {
                val uri = uriFor(context, path)
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, mimeFor(path))
                    flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                }
                context.startActivity(intent)
            }
        }
    }
}

private fun uriFor(context: android.content.Context, path: Path): Uri {
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.provider",
        File(path.toString())
    )
}

private fun mimeFor(path: Path): String {
    return when {
        path.name.endsWith(".pdf", true) -> "application/pdf"
        path.name.endsWith(".zip", true) -> "application/zip"
        else -> "*/*"
    }
}

actual fun zipDirectory(sourceDir: Path, zipFile: Path) {
    Zip.open(
        path = zipFile,
        mode = Zip.Mode.Write,
        level = Zip.CompressionLevel.BetterCompression
    ).use { zip ->
        zip.compressFrom(sourceDir)
    }
}

@Composable
actual fun ExitOnBackHandler() {
    val activity = LocalActivity.current
    BackHandler {
        activity?.finishAffinity()
        kotlin.system.exitProcess(0)
    }
}
