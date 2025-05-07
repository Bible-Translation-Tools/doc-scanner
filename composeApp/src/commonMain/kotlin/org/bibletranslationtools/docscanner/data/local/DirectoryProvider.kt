package org.bibletranslationtools.docscanner.data.local

import android.content.Context
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
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
        get() = context.filesDir.absolutePath.toPath()

    override val externalAppDir: Path
        get() {
            val dir = context.getExternalFilesDir(null)
                ?: throw NullPointerException("External storage is currently unavailable.")
            return dir.absolutePath.toPath()
        }

    override val cacheDir: Path
        get() = context.cacheDir.absolutePath.toPath()

    override val projectsDir: Path
        get() {
            val path = externalAppDir / "projects"
            if (!FileSystem.SYSTEM.exists(path)) {
                FileSystem.SYSTEM.createDirectories(path)
            }
            return path
        }

    override val sshKeysDir: Path
        get() {
            val dir = internalAppDir / "ssh"
            if (!FileSystem.SYSTEM.exists(dir)) {
                FileSystem.SYSTEM.createDirectories(dir)
            }
            return dir
        }

    override val publicKey: Path
        get() = sshKeysDir / "id_rsa.pub"

    override val privateKey: Path
        get() = sshKeysDir / "id_rsa"

    override fun hasSSHKeys(): Boolean {
        return FileSystem.SYSTEM.exists(privateKey) && FileSystem.SYSTEM.exists(publicKey)
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
        val tempDir = cacheDir / tempName
        FileSystem.SYSTEM.createDirectories(tempDir)
        return tempDir
    }

    override fun createTempFile(prefix: String, suffix: String?, dir: Path?): Path {
        val directory = dir ?: cacheDir
        val actualSuffix = suffix ?: ""
        val fileName = "$prefix-${System.currentTimeMillis()}$actualSuffix"
        val tempFile = directory / fileName
        FileSystem.SYSTEM.write(tempFile) { /* create empty file */ }
        return tempFile
    }

    override fun clearCache() {
        try {
            FileSystem.SYSTEM.list(cacheDir).forEach { path ->
                if (FileSystem.SYSTEM.metadata(path).isDirectory) {
                    FileSystem.SYSTEM.deleteRecursively(path)
                } else {
                    FileSystem.SYSTEM.delete(path)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}