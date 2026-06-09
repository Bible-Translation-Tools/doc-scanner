package org.bibletranslationtools.docscanner

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import org.bibletranslationtools.docscanner.data.repository.DirectoryProvider
import org.bibletranslationtools.docscanner.utils.deleteRecursively
import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSDate
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSLibraryDirectory
import platform.Foundation.NSSearchPathDirectory
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask
import platform.Foundation.timeIntervalSince1970

class IosDirectoryProvider : DirectoryProvider {

    private val logger = KotlinLogging.logger {}

    private fun systemDir(directory: NSSearchPathDirectory): Path {
        val paths = NSSearchPathForDirectoriesInDomains(directory, NSUserDomainMask, true)
        val dir = paths.firstOrNull() as? String
            ?: throw IllegalStateException("Could not resolve system directory $directory")
        return Path(dir)
    }

    override val internalAppDir: Path
        get() = systemDir(NSLibraryDirectory)

    override val externalAppDir: Path
        get() = systemDir(NSDocumentDirectory)

    override val cacheDir: Path
        get() = systemDir(NSCachesDirectory)

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

    private fun timestamp(): Long =
        (NSDate().timeIntervalSince1970 * 1000).toLong()

    override fun createTempDir(name: String?): Path {
        val tempName = name ?: timestamp().toString()
        val tempDir = Path(cacheDir, tempName)
        SystemFileSystem.createDirectories(tempDir)
        return tempDir
    }

    override fun createTempFile(prefix: String, suffix: String?, dir: Path?): Path {
        val directory = dir ?: cacheDir
        val actualSuffix = suffix ?: ""
        val fileName = "$prefix-${timestamp()}$actualSuffix"
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
