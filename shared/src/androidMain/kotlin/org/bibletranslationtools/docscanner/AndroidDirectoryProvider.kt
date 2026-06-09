package org.bibletranslationtools.docscanner

import android.content.Context
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import org.bibletranslationtools.docscanner.data.repository.DirectoryProvider
import org.bibletranslationtools.docscanner.utils.deleteRecursively

class AndroidDirectoryProvider (private val context: Context) : DirectoryProvider {
    companion object {
        const val TAG = "DirectoryProvider"
    }

    private val logger = KotlinLogging.logger {}

    override val internalAppDir: Path
        get() = kotlinx.io.files.Path(context.filesDir.absolutePath)

    override val externalAppDir: Path
        get() {
            val dir = context.getExternalFilesDir(null)
                ?: throw NullPointerException("External storage is currently unavailable.")
            return kotlinx.io.files.Path(dir.absolutePath)
        }

    override val cacheDir: Path
        get() = kotlinx.io.files.Path(context.cacheDir.absolutePath)

    override val projectsDir: Path
        get() {
            val path = Path(externalAppDir, "projects")
            if (!SystemFileSystem.exists(path)) {
                SystemFileSystem.createDirectories(path)
            }
            return path
        }

    override val sharedDir: Path
        get() {
            val dir = Path(externalAppDir, "shared")
            if (!SystemFileSystem.exists(dir)) {
                SystemFileSystem.createDirectories(dir)
            }
            return dir
        }

    override val logFile: Path
        get() = Path(externalAppDir, "log.txt")

    override fun createTempDir(name: String?): Path {
        val tempName = name ?: System.currentTimeMillis().toString()
        val tempDir = Path(cacheDir, tempName)
        SystemFileSystem.createDirectories(tempDir)
        return tempDir
    }

    override fun createTempFile(prefix: String, suffix: String?, dir: Path?): Path {
        val directory = dir ?: cacheDir
        val actualSuffix = suffix ?: ""
        val fileName = "$prefix-${System.currentTimeMillis()}$actualSuffix"
        val tempFile = Path(directory, fileName)
        SystemFileSystem.sink(tempFile).buffered().use { /* create empty file */ }
        return tempFile
    }

    override fun clearCache() {
        try {
            SystemFileSystem.list(cacheDir).forEach { path ->
                SystemFileSystem.deleteRecursively(path)
            }
        } catch (e: Exception) {
            logger.error(e) { "Error clearing cache" }
        }
    }
}