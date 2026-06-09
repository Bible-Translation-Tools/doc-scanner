package org.bibletranslationtools.docscanner.platform

import androidx.compose.runtime.Composable
import kotlinx.io.files.Path
import org.bibletranslationtools.docscanner.data.models.Image
import org.bibletranslationtools.docscanner.data.repository.DirectoryProvider

/**
 * Launches the native document scanner.
 * The result delivered to `onResult` is a temporary PDF [Path] (in the cache dir),
 * or `null` if the scan was canceled or failed.
 */
interface DocumentScannerLauncher {
    fun launch()
}

expect interface CommonSerializable

@Composable
expect fun rememberDocumentScannerLauncher(
    directoryProvider: DirectoryProvider,
    onResult: (Path?) -> Unit
): DocumentScannerLauncher

/**
 * Whether the native document scanner can run on this device.
 * iOS: false on macOS ("Designed for iPad") and the Simulator (no camera).
 * Android: always true.
 */
expect fun isDocumentScannerAvailable(): Boolean

/**
 * Renders each page of the PDF at [pdfPath] to a JPEG temp file
 * (created via [directoryProvider]) and returns the resulting images.
 */
expect fun renderPdfToImages(
    pdfPath: Path,
    directoryProvider: DirectoryProvider
): List<Image>

/**
 * Shares or opens files using native UI (Android Intents / iOS UIActivity & QuickLook).
 */
interface FileSharer {
    fun share(path: Path)
    fun open(path: Path)
}

@Composable
expect fun rememberFileSharer(): FileSharer

/**
 * Compresses the contents of [sourceDir] into the zip archive at [zipFile].
 */
expect fun zipDirectory(sourceDir: Path, zipFile: Path)

/**
 * Android: finishes the activity and exits the process on back press.
 * iOS: no-op (the system handles app lifecycle).
 */
@Composable
expect fun ExitOnBackHandler()
