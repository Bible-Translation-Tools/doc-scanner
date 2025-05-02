package org.bibletranslationtools.docscanner.data.local.git

import android.content.Context
import org.bibletranslationtools.docscanner.R
import org.bibletranslationtools.docscanner.data.local.DirectoryProvider
import org.bibletranslationtools.docscanner.data.local.OnProgressListener
import org.bibletranslationtools.docscanner.data.local.Settings
import org.bibletranslationtools.docscanner.data.repository.PreferenceRepository
import org.bibletranslationtools.docscanner.data.repository.getPref
import org.bibletranslationtools.docscanner.utils.FileUtilities
import org.bibletranslationtools.docscanner.utils.identificator
import org.unfoldingword.gogsclient.GogsAPI
import org.unfoldingword.gogsclient.PublicKey
import java.io.IOException
import javax.inject.Inject

class RegisterSSHKeys @Inject constructor(
    private val context: Context,
    private val directoryProvider: DirectoryProvider,
    private val prefRepository: PreferenceRepository
) {
    private val max = 100

    fun execute(
        force: Boolean,
        profile: Profile,
        progressListener: OnProgressListener? = null
    ): Boolean {
        progressListener?.onProgress(-1, max, "Authenticating")

        val keyName = context.resources.getString(R.string.gogs_public_key_name) + " " + identificator()

        val api = GogsAPI(
            prefRepository.getPref(
                Settings.KEY_PREF_GOGS_API,
                context.getString(R.string.pref_default_gogs_api)
            ),
            context.getString(R.string.gogs_user_agent)
        )

        if (profile.gogsUser != null) {
            if (!directoryProvider.hasSSHKeys() || force) {
                directoryProvider.generateSSHKeys()
            }
            val keyString: String?
            try {
                keyString = FileUtilities.readFileToString(directoryProvider.publicKey).trim()
            } catch (e: IOException) {
                println("Failed to retrieve the public key")
                e.printStackTrace()
                return false
            }

            val keyTemplate = PublicKey(keyName, keyString)

            // delete old key
            val keys = api.listPublicKeys(profile.gogsUser)
            for (k in keys) {
                if (k.title == keyTemplate.title) {
                    api.deletePublicKey(k, profile.gogsUser)
                    break
                }
            }

            // create new key
            val key = api.createPublicKey(keyTemplate, profile.gogsUser)
            if (key != null) {
                return true
            } else {
                val response = api.lastResponse
                println("Failed to register the public key. Gogs responded with " + response.code + ": " + response.data)
                response.exception?.printStackTrace()
            }
        }

        return false
    }
}