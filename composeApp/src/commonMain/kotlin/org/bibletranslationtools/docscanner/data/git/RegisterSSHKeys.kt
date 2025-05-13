package org.bibletranslationtools.docscanner.data.git

import docscanner.composeapp.generated.resources.Res
import docscanner.composeapp.generated.resources.gogs_public_key_name
import docscanner.composeapp.generated.resources.gogs_user_agent
import docscanner.composeapp.generated.resources.pref_default_gogs_api
import io.github.oshai.kotlinlogging.KotlinLogging
import org.bibletranslationtools.docscanner.OnProgressListener
import org.bibletranslationtools.docscanner.data.Settings
import org.bibletranslationtools.docscanner.data.repository.DirectoryProvider
import org.bibletranslationtools.docscanner.data.repository.PreferenceRepository
import org.bibletranslationtools.docscanner.data.repository.getPref
import org.bibletranslationtools.docscanner.utils.identificator
import org.bibletranslationtools.docscanner.utils.readString
import org.bibletranslationtools.docscanner.utils.trimMultiline
import org.jetbrains.compose.resources.getString
import org.unfoldingword.gogsclient.GogsAPI
import org.unfoldingword.gogsclient.PublicKey
import org.unfoldingword.gogsclient.User
import java.io.IOException

class RegisterSSHKeys(
    private val directoryProvider: DirectoryProvider,
    private val prefRepository: PreferenceRepository
) {
    private val max = 100
    private val logger = KotlinLogging.logger {}

    suspend fun execute(
        force: Boolean,
        user: User,
        progressListener: OnProgressListener? = null
    ): Boolean {
        progressListener?.onProgress(-1, max, "Authenticating")

        val keyName = getString(Res.string.gogs_public_key_name) + " " + identificator()

        val api = GogsAPI(
            prefRepository.getPref(
                Settings.KEY_PREF_GOGS_API,
                getString(Res.string.pref_default_gogs_api)
            ),
            getString(Res.string.gogs_user_agent)
        )

        if (!directoryProvider.hasSSHKeys() || force) {
            directoryProvider.generateSSHKeys()
        }
        val keyString: String?
        try {
            keyString = directoryProvider.publicKey.readString().trim()
        } catch (e: IOException) {
            logger.error(e) { "Failed to retrieve the public key" }
            return false
        }

        val keyTemplate = PublicKey(keyName, keyString)

        // delete old key
        val keys = api.listPublicKeys(user)
        for (k in keys) {
            if (k.title == keyTemplate.title) {
                api.deletePublicKey(k, user)
                break
            }
        }

        // create new key
        val key = api.createPublicKey(keyTemplate, user)
        if (key != null) {
            return true
        } else {
            val response = api.lastResponse
            logger.warn {
                """
                    Failed to register the public key.
                    Gogs responded with ${response.code}: ${response.data}
                """.trimMultiline()
            }
            response.exception?.printStackTrace()
        }

        return false
    }
}