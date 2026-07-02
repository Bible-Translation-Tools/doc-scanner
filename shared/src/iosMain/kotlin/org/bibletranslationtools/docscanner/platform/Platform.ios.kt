@file:OptIn(ExperimentalForeignApi::class)

package org.bibletranslationtools.docscanner.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.useContents
import kotlinx.cinterop.usePinned
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import org.bibletranslationtools.docscanner.data.models.Image
import org.bibletranslationtools.docscanner.data.repository.DirectoryProvider
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGSizeMake
import platform.Foundation.NSData
import platform.Foundation.NSError
import platform.Foundation.NSFileCoordinator
import platform.Foundation.NSFileCoordinatorReadingForUploading
import platform.Foundation.NSFileManager
import platform.Foundation.NSMutableData
import platform.Foundation.NSURL
import platform.PDFKit.PDFDocument
import platform.PDFKit.kPDFDisplayBoxMediaBox
import platform.QuickLook.QLPreviewController
import platform.QuickLook.QLPreviewControllerDataSourceProtocol
import platform.QuickLook.QLPreviewItemProtocol
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIGraphicsBeginPDFContextToData
import platform.UIKit.UIGraphicsBeginPDFPageWithInfo
import platform.UIKit.UIGraphicsEndPDFContext
import platform.UIKit.UIImageJPEGRepresentation
import platform.UIKit.UIViewController
import platform.UIKit.UIWindow
import platform.VisionKit.VNDocumentCameraScan
import platform.VisionKit.VNDocumentCameraViewController
import platform.VisionKit.VNDocumentCameraViewControllerDelegateProtocol
import platform.darwin.NSObject
import platform.posix.memcpy

private val logger = KotlinLogging.logger {}

// ---- Helpers ----

private fun topViewController(): UIViewController? {
    val window: UIWindow? = UIApplication.sharedApplication.windows
        .filterIsInstance<UIWindow>()
        .firstOrNull { it.isKeyWindow() }
        ?: UIApplication.sharedApplication.windows.firstOrNull() as? UIWindow

    var top = window?.rootViewController
    while (top?.presentedViewController != null) {
        top = top.presentedViewController
    }
    return top
}

// ---- Document scanner (VisionKit) ----

private var retainedScannerDelegate: NSObject? = null

private class DocumentScannerDelegate(
    private val directoryProvider: DirectoryProvider,
    private val onResult: (Path?) -> Unit
) : NSObject(), VNDocumentCameraViewControllerDelegateProtocol {

    override fun documentCameraViewController(
        controller: VNDocumentCameraViewController,
        didFinishWithScan: VNDocumentCameraScan
    ) {
        val pdfPath = buildPdf(didFinishWithScan, directoryProvider)
        controller.dismissViewControllerAnimated(true) {
            retainedScannerDelegate = null
            onResult(pdfPath)
        }
    }

    override fun documentCameraViewControllerDidCancel(
        controller: VNDocumentCameraViewController
    ) {
        controller.dismissViewControllerAnimated(true) {
            retainedScannerDelegate = null
            onResult(null)
        }
    }

    override fun documentCameraViewController(
        controller: VNDocumentCameraViewController,
        didFailWithError: NSError
    ) {
        logger.error { "Document scanner failed: ${didFailWithError.localizedDescription}" }
        controller.dismissViewControllerAnimated(true) {
            retainedScannerDelegate = null
            onResult(null)
        }
    }
}

private fun buildPdf(scan: VNDocumentCameraScan, directoryProvider: DirectoryProvider): Path? {
    return try {
        val pageCount = scan.pageCount.toInt()
        if (pageCount == 0) return null

        val pdfPath = directoryProvider.createTempFile("scan", ".pdf")
        val data = NSMutableData()

        UIGraphicsBeginPDFContextToData(data, CGRectMake(0.0, 0.0, 0.0, 0.0), null)
        for (i in 0 until pageCount) {
            val image = scan.imageOfPageAtIndex(i.toULong())
            val size = image.size
            val width = size.useContents { width }
            val height = size.useContents { height }
            UIGraphicsBeginPDFPageWithInfo(CGRectMake(0.0, 0.0, width, height), null)
            image.drawInRect(CGRectMake(0.0, 0.0, width, height))
        }
        UIGraphicsEndPDFContext()

        writeNSData(data, pdfPath)
        pdfPath
    } catch (e: Exception) {
        logger.error(e) { "Failed to build PDF from scan" }
        null
    }
}

private fun writeNSData(data: NSData, path: Path) {
    val size = data.length.toInt()
    val bytes = ByteArray(size)
    if (size > 0) {
        bytes.usePinned { pinned ->
            memcpy(pinned.addressOf(0), data.bytes, data.length)
        }
    }
    SystemFileSystem.sink(path).buffered().use { sink ->
        sink.write(bytes)
    }
}

@Composable
actual fun rememberDocumentScannerLauncher(
    directoryProvider: DirectoryProvider,
    onResult: (Path?) -> Unit
): DocumentScannerLauncher {
    return remember(directoryProvider) {
        object : DocumentScannerLauncher {
            override fun launch() {
                // Guarded by isDocumentScannerAvailable() in the UI, but stay defensive.
                if (!VNDocumentCameraViewController.isSupported()) {
                    onResult(null)
                    return
                }
                val delegate = DocumentScannerDelegate(directoryProvider, onResult)
                retainedScannerDelegate = delegate
                val scanner = VNDocumentCameraViewController()
                scanner.delegate = delegate
                topViewController()?.presentViewController(scanner, animated = true, completion = null)
            }
        }
    }
}

actual fun isDocumentScannerAvailable(): Boolean =
    VNDocumentCameraViewController.isSupported()

// ---- PDF -> images (CoreGraphics) ----

actual fun renderPdfToImages(
    pdfPath: Path,
    directoryProvider: DirectoryProvider
): List<Image> {
    val images = mutableListOf<Image>()
    val url = NSURL.fileURLWithPath(pdfPath.toString())
    val document = PDFDocument(uRL = url)

    val pageCount = document.pageCount.toInt()
    for (i in 0 until pageCount) {
        val page = document.pageAtIndex(i.toULong()) ?: continue
        val bounds = page.boundsForBox(kPDFDisplayBoxMediaBox)
        val width = bounds.useContents { size.width }
        val height = bounds.useContents { size.height }

        val image = page.thumbnailOfSize(
            CGSizeMake(width, height),
            forBox = kPDFDisplayBoxMediaBox
        )
        val jpeg = UIImageJPEGRepresentation(image, 0.9) ?: continue

        val imageFile = directoryProvider.createTempFile("image", ".jpg")
        writeNSData(jpeg, imageFile)
        images.add(Image(path = imageFile.toString(), chapter = 1))
    }

    return images
}

// ---- Share / open (UIActivity & QuickLook) ----

private var retainedPreviewSource: NSObject? = null

private class PreviewItem(private val url: NSURL) : NSObject(), QLPreviewItemProtocol {
    override fun previewItemURL(): NSURL = url
    override fun previewItemTitle(): String? = url.lastPathComponent
}

private class PreviewDataSource(url: NSURL) : NSObject(), QLPreviewControllerDataSourceProtocol {
    private val item = PreviewItem(url)

    override fun numberOfPreviewItemsInPreviewController(controller: QLPreviewController): Long = 1

    override fun previewController(
        controller: QLPreviewController,
        previewItemAtIndex: Long
    ): QLPreviewItemProtocol = item
}

@Composable
actual fun rememberFileSharer(): FileSharer {
    return remember {
        object : FileSharer {
            override fun share(path: Path) {
                val url = NSURL.fileURLWithPath(path.toString())
                val controller = UIActivityViewController(
                    activityItems = listOf(url),
                    applicationActivities = null
                )
                topViewController()?.presentViewController(controller, animated = true, completion = null)
            }

            override fun open(path: Path) {
                val url = NSURL.fileURLWithPath(path.toString())
                val source = PreviewDataSource(url)
                retainedPreviewSource = source
                val controller = QLPreviewController()
                controller.dataSource = source
                topViewController()?.presentViewController(controller, animated = true, completion = null)
            }
        }
    }
}

// ---- Zip (NSFileCoordinator forUploading) ----

actual fun zipDirectory(sourceDir: Path, zipFile: Path) {
    val coordinator = NSFileCoordinator(filePresenter = null)
    val srcUrl = NSURL.fileURLWithPath(sourceDir.toString())
    val fm = NSFileManager.defaultManager
    val destPath = zipFile.toString()

    memScoped {
        val errorPtr = alloc<ObjCObjectVar<NSError?>>()
        coordinator.coordinateReadingItemAtURL(
            srcUrl,
            options = NSFileCoordinatorReadingForUploading,
            error = errorPtr.ptr
        ) { zippedUrl ->
            if (zippedUrl != null) {
                if (fm.fileExistsAtPath(destPath)) {
                    fm.removeItemAtPath(destPath, null)
                }
                fm.copyItemAtURL(zippedUrl, NSURL.fileURLWithPath(destPath), null)
            }
        }
    }
}

// ---- Back handler (no-op on iOS) ----

@Composable
actual fun ExitOnBackHandler() {
    // iOS manages app lifecycle; nothing to do.
}

actual interface CommonSerializable

