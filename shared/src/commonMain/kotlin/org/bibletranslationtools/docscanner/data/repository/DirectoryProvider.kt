package org.bibletranslationtools.docscanner.data.repository

import kotlinx.io.files.Path

interface DirectoryProvider {

    /**
     * Returns the path to the internal files directory accessible by the app only.
     * This directory is not accessible by other applications and file managers.
     * It's good for storing private data, such as ssh keys.
     * Files saved in this directory will be removed when the application is uninstalled
     */
    val internalAppDir: Path

    /**
     * Returns the path to the external files directory accessible by the app only.
     * This directory can be accessed by file managers.
     * It's good for storing user-created data, such as translations and backups.
     * Files saved in this directory will be removed when the application is uninstalled
     */
    val externalAppDir: Path

    /**
     * Returns the absolute path to the application specific cache directory on the filesystem.
     */
    val cacheDir: Path

    /**
     * Returns the path to the projects directory.
     */
    val projectsDir: Path

    /**
     * Returns the path to the share directory
     */
    val sharedDir: Path

    /**
     * Returns the path to the log file
     */
    val logFile: Path

    /**
     * Creates a temporary directory in the cache directory.
     * @param name The optional name of the directory.
     */
    fun createTempDir(name: String?): Path

    /**
     * Creates a temporary file in the cache directory.
     * @param prefix The optional prefix of the file.
     * @param suffix The optional suffix of the file.
     * @param dir The optional directory to create the file in.
     */
    fun createTempFile(prefix: String, suffix: String?, dir: Path? = null): Path

    /**
     * Clear the cache directory
     */
    fun clearCache()
}
