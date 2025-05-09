package org.bibletranslationtools.docscanner.utils

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import de.jonasbroeckmann.kzip.Zip
import de.jonasbroeckmann.kzip.compressFrom
import de.jonasbroeckmann.kzip.open
import docscanner.composeapp.generated.resources.Res
import io.ktor.util.sha1
import kotlinx.io.asSource
import kotlinx.io.buffered
import kotlinx.io.files.FileSystem
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readByteArray
import kotlinx.io.readString
import org.bibletranslationtools.docscanner.data.models.Project
import org.bibletranslationtools.docscanner.data.models.getName
import org.bibletranslationtools.docscanner.data.repository.DirectoryProvider
import java.io.File

object FileUtils {
    fun writeUriToPath(
        context: Context,
        fileUri: Uri,
        path: Path
    ) {
        context.contentResolver.openInputStream(fileUri)?.use { inputStream ->
            try {
                path.parent?.let {
                    SystemFileSystem.createDirectories(it)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            SystemFileSystem.sink(path).buffered().use { sink ->
                inputStream.asSource().buffered().transferTo(sink)
            }
        }
    }

    fun getFileSize(path: Path): String {
        val fileSizeBytes = SystemFileSystem.metadataOrNull(path)?.size ?: 0
        val fileSizeKB = fileSizeBytes / 1024
        return if (fileSizeKB > 1024) {
            val fileSizeMB = fileSizeKB / 1024
            "$fileSizeMB MB"
        } else {
            "$fileSizeKB KB"
        }
    }

    fun getPdfUri(
        context: Context,
        directoryProvider: DirectoryProvider,
        fileName: String,
        project: Project
    ): Uri {
        val projectDir = Path(directoryProvider.projectsDir, project.getName())
        val file = Path(projectDir, fileName)
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            File(file.toString())
        )
    }

    fun getPathUri(
        context: Context,
        path: Path
    ): Uri {
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            File(path.toString())
        )
    }

    fun renamePath(src: Path, dest: Path) {
        SystemFileSystem.atomicMove(src, dest)
    }

    suspend fun loadAsset(name: String): String {
        val readBytes = Res.readBytes("files/$name")
        return String(readBytes)
    }

    fun zipProject(
        project: Project,
        directoryProvider: DirectoryProvider
    ): Path {
        val projectDir = Path(directoryProvider.projectsDir, project.getName())
        val zipFile = Path(directoryProvider.sharedDir, "${project.getName()}.zip")

        Zip.open(
            path = zipFile,
            mode = Zip.Mode.Write,
            level = Zip.CompressionLevel.BetterCompression
        ).use { zip ->
            zip.compressFrom(projectDir)
        }

        return zipFile
    }
}

fun Path.readString(): String {
    return SystemFileSystem.source(this).buffered().use {
        it.readString()
    }
}

fun FileSystem.deleteRecursively(path: Path, mustExist: Boolean = false) {
    val isDirectory = metadataOrNull(path)?.isDirectory ?: false
    if (isDirectory) {
        for (child in list(path)) {
            deleteRecursively(child, mustExist)
        }
    }
    delete(path, mustExist)
}