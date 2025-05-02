package org.bibletranslationtools.docscanner.data.local

import android.content.Context
import com.jcraft.jsch.JSch
import com.jcraft.jsch.KeyPair
import org.bibletranslationtools.docscanner.utils.identificator
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
     * Returns the path to the projects directory.
     */
    val projectsDir: File

    /**
     * Returns the directory in which the ssh keys are stored
     */
    val sshKeysDir: File

    /**
     * Returns the public key file
     */
    val publicKey: File

    /**
     * Returns the private key file
     */
    val privateKey: File

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

    override val projectsDir: File
        get() {
            val path = File(externalAppDir, "projects")
            if (!path.exists()) path.mkdirs()
            return path
        }

    override val sshKeysDir: File
        get() = run {
            val dir = File(internalAppDir, "ssh")
            if (!dir.exists()) dir.mkdirs()
            dir
        }

    override val publicKey: File
        get() = File(sshKeysDir, "id_rsa.pub")

    override val privateKey: File
        get() = File(sshKeysDir, "id_rsa")

    override fun hasSSHKeys(): Boolean {
        return privateKey.exists() && publicKey.exists()
    }

    override fun generateSSHKeys() {
        val jsch = JSch()
        val type = KeyPair.RSA

        try {
            val keyPair = KeyPair.genKeyPair(jsch, type)
            File(privateKey.absolutePath).createNewFile()
            keyPair.writePrivateKey(privateKey.absolutePath)
            File(publicKey.absolutePath).createNewFile()
            keyPair.writePublicKey(publicKey.absolutePath, identificator())
            keyPair.dispose()
        } catch (e: Exception) {
            e.printStackTrace()
        }
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
            it.deleteRecursively()
        }
    }
}