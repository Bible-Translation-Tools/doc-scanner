package org.bibletranslationtools.docscanner.utils

import docscanner.composeapp.generated.resources.Res
import kotlinx.io.buffered
import kotlinx.io.files.FileSystem
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readString
import org.bibletranslationtools.docscanner.data.models.Project
import org.bibletranslationtools.docscanner.data.models.getName
import org.bibletranslationtools.docscanner.data.repository.DirectoryProvider
import org.bibletranslationtools.docscanner.platform.zipDirectory

object FileUtils {
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

    fun renamePath(src: Path, dest: Path) {
        SystemFileSystem.atomicMove(src, dest)
    }

    suspend fun loadAsset(name: String): String {
        val readBytes = Res.readBytes("files/$name")
        return readBytes.decodeToString()
    }

    fun zipProject(
        project: Project,
        directoryProvider: DirectoryProvider
    ): Path {
        val projectDir = Path(directoryProvider.projectsDir, project.getName())
        val zipFile = Path(directoryProvider.sharedDir, "${project.getName()}.zip")

        zipDirectory(projectDir, zipFile)

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
