package org.bibletranslationtools.docscanner.data.local

import android.content.Context
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import org.bibletranslationtools.docscanner.utils.deleteRecursively
import org.bibletranslationtools.docscanner.utils.identificator

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
     * Returns the directory in which the ssh keys are stored
     */
    val sshKeysDir: Path

    /**
     * Returns the public key file
     */
    val publicKey: Path

    /**
     * Returns the private key file
     */
    val privateKey: Path

    /**
     * Returns the path to the share directory
     */
    val sharedDir: Path

    /**
     * Checks if the ssh keys have already been generated
     * @return Boolean
     */
    fun hasSSHKeys(): Boolean

    /**
     * Generates a new RSA key pair for use with ssh
     */
    fun generateSSHKeys()

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

class DirectoryProviderImpl (private val context: Context) : DirectoryProvider {
    companion object {
        const val TAG = "DirectoryProvider"
    }

    override val internalAppDir: Path
        get() = Path(context.filesDir.absolutePath)

    override val externalAppDir: Path
        get() {
            val dir = context.getExternalFilesDir(null)
                ?: throw NullPointerException("External storage is currently unavailable.")
            return Path(dir.absolutePath)
        }

    override val cacheDir: Path
        get() = Path(context.cacheDir.absolutePath)

    override val projectsDir: Path
        get() {
            val path = Path(externalAppDir, "projects")
            if (!SystemFileSystem.exists(path)) {
                SystemFileSystem.createDirectories(path)
            }
            return path
        }

    override val sshKeysDir: Path
        get() {
            val dir = Path(internalAppDir, "ssh")
            if (!SystemFileSystem.exists(dir)) {
                SystemFileSystem.createDirectories(dir)
            }
            return dir
        }

    override val publicKey: Path
        get() = Path(sshKeysDir, "id_rsa.pub")

    override val privateKey: Path
        get() = Path(sshKeysDir, "id_rsa")

    override val sharedDir: Path
        get() {
            val dir = Path(externalAppDir, "shared")
            if (!SystemFileSystem.exists(dir)) {
                SystemFileSystem.createDirectories(dir)
            }
            return dir
        }

    override fun hasSSHKeys(): Boolean {
        return SystemFileSystem.exists(privateKey) && SystemFileSystem.exists(publicKey)
    }

    override fun generateSSHKeys() {
        try {
            // Create a platform-specific key generator using JSch
            // Uses java.io.File for JSch compatibility
            val privateKeyFile = java.io.File(privateKey.toString())
            val publicKeyFile = java.io.File(publicKey.toString())

            val jsch = com.jcraft.jsch.JSch()
            val type = com.jcraft.jsch.KeyPair.RSA

            try {
                val keyPair = com.jcraft.jsch.KeyPair.genKeyPair(jsch, type)
                privateKeyFile.createNewFile()
                keyPair.writePrivateKey(privateKeyFile.absolutePath)
                publicKeyFile.createNewFile()
                keyPair.writePublicKey(publicKeyFile.absolutePath, identificator())
                keyPair.dispose()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

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
            e.printStackTrace()
        }
    }
}