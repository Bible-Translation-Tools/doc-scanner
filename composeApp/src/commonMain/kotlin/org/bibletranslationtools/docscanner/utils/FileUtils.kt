package org.bibletranslationtools.docscanner.utils

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import docscanner.composeapp.generated.resources.Res
import okio.FileSystem
import okio.Path
import okio.buffer
import okio.source
import org.bibletranslationtools.docscanner.data.local.DirectoryProvider
import org.bibletranslationtools.docscanner.data.models.Project
import org.bibletranslationtools.docscanner.data.models.getName
import org.jetbrains.compose.resources.ExperimentalResourceApi

object FileUtils {
    fun copyPdfFileToAppDirectory(
        context: Context,
        directoryProvider: DirectoryProvider,
        pdfUri: Uri,
        destinationFileName: String,
        project: Project
    ) {
        context.contentResolver.openInputStream(pdfUri)?.use { inputStream ->
            val projectDir = directoryProvider.projectsDir / project.getName()
            val outputFile = projectDir / destinationFileName

            FileSystem.SYSTEM.createDirectories(projectDir)

            FileSystem.SYSTEM.sink(outputFile).buffer().use { bufferedSink ->
                inputStream.source().buffer().readAll(bufferedSink)
            }
        }
    }

    fun getFileSize(
        directoryProvider: DirectoryProvider,
        fileName: String,
        project: Project
    ): String {
        val projectDir = directoryProvider.projectsDir / project.getName()
        val file = projectDir / fileName
        val fileSizeBytes = FileSystem.SYSTEM.metadata(file).size ?: 0
        val fileSizeKB = fileSizeBytes / 1024
        return if (fileSizeKB > 1024) {
            val fileSizeMB = fileSizeKB / 1024
            "$fileSizeMB MB"
        } else {
            "$fileSizeKB KB"
        }
    }

    fun getFileUri(
        context: Context,
        directoryProvider: DirectoryProvider,
        fileName: String,
        project: Project
    ): Uri {
        val projectDir = directoryProvider.projectsDir / project.getName()
        val file = projectDir / fileName
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file.toFile()
        )
    }

    @OptIn(ExperimentalResourceApi::class)
    suspend fun loadAsset(name: String): String {
        val readBytes = Res.readBytes("files/$name")
        return String(readBytes)
    }
}

fun Path.readString(): String {
    return FileSystem.SYSTEM.read(this) {
        readUtf8()
    }
}