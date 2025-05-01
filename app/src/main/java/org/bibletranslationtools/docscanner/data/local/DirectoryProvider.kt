package org.bibletranslationtools.docscanner.data.local

import android.content.Context
import org.bibletranslationtools.docscanner.FileUtilities
import java.io.File

interface DirectoryProvider {

    /**
     * Returns the path to the internal files directory accessible by the app only.
     * This directory is not accessible by other applications and file managers.
     * It's good for storing private data, such as ssh keys.
     * Files saved in this directory will be removed when the application is uninstalled
     */
    val internalAppDir: File

    /**
     * Returns the path to the external files directory accessible by the app only.
     * This directory can be accessed by file managers.
     * It's good for storing user-created data, such as translations and backups.
     * Files saved in this directory will be removed when the application is uninstalled
     */
    val externalAppDir: File

    /**
     * Returns the absolute path to the application specific cache directory on the filesystem.
     */
    val cacheDir: File

    /**
     * Returns the path to the documents directory.
     */
    val documentsDir: File

    /**
     * Creates a temporary directory in the cache directory.
     * @param name The optional name of the directory.
     */
    fun createTempDir(name: String?): File

    /**
     * Creates a temporary file in the cache directory.
     * @param prefix The optional prefix of the file.
     * @param suffix The optional suffix of the file.
     * @param dir The optional directory to create the file in.
     */
    fun createTempFile(prefix: String, suffix: String?, dir: File? = null): File

    /**
     * Clear the cache directory
     */
    fun clearCache()
}

class DirectoryProviderImpl (private val context: Context) : DirectoryProvider {
    companion object {
        const val TAG = "DirectoryProvider"
    }

    override val internalAppDir: File
        get() = context.filesDir

    override val externalAppDir: File
        get() = context.getExternalFilesDir(null)
            ?: throw NullPointerException("External storage is currently unavailable.")

    override val cacheDir: File
        get() = context.cacheDir

    override val documentsDir: File
        get() {
            val path = File(externalAppDir, "documents")
            if (!path.exists()) path.mkdirs()
            return path
        }

    override fun createTempDir(name: String?): File {
        val tempName = name ?: System.currentTimeMillis().toString()
        val tempDir = File(cacheDir, tempName)
        tempDir.mkdirs()
        return tempDir
    }

    override fun createTempFile(prefix: String, suffix: String?, dir: File?): File {
        return File.createTempFile(prefix, suffix, dir ?: cacheDir)
    }

    override fun clearCache() {
        cacheDir.listFiles()?.forEach {
            FileUtilities.deleteRecursive(it)
        }
    }
}