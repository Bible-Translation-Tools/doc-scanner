package org.bibletranslationtools.docscanner.data.local.git

import okio.FileSystem
import org.bibletranslationtools.docscanner.data.local.DirectoryProvider
import org.bibletranslationtools.docscanner.data.repository.PreferenceRepository
import org.bibletranslationtools.docscanner.data.repository.setPref
import org.json.JSONException
import org.json.JSONObject
import org.unfoldingword.gogsclient.Token
import org.unfoldingword.gogsclient.User

/**
 * Represents a single user profile
 */
class Profile(
    private val prefs: PreferenceRepository,
    private val directoryProvider: DirectoryProvider
) {
    /**
     * Returns the name of the translator.
     * The name from their gogs account will be used if it exists
     * @return
     */
    var fullName: String? = null
        get() = gogsUser?.fullName ?: field

    /**
     * Returns the gogs user associated with this profile
     */
    var gogsUser: User? = null

    val currentUser: String
        get() {
            var userName: String? = null
            if (gogsUser != null) {
                userName = gogsUser!!.username
            }
            if (userName == null) {
                userName = fullName
            }

            if (userName == null) {
                userName = ""
            }
            return userName
        }

    /**
     * Returns the version of the terms of use accepted last time
     */
    var termsOfUseLastAccepted: Int = 0
        set(value) {
            field = value
            saveProfile()
        }

    /**
     * Returns true if the user is logged in
     */
    val loggedIn: Boolean
        get() = fullName.isNullOrEmpty().not()

    /**
     * Returns the profile represented as a json object
     * @return
     */
    @Throws(JSONException::class)
    fun toJSON(): JSONObject {
        val json = JSONObject()
        json.put("serial_version_uid", SERIAL_VERSION_UID)
        gogsUser?.let {
            json.put("gogs_user", it.toJSON())
            json.put("gogs_token", it.token.toJSON())
        } ?: run {
            json.put("full_name", fullName)
        }
        json.put("terms_last_accepted", termsOfUseLastAccepted)
        return json
    }

    fun login(name: String?, user: User? = null) {
        fullName = name
        gogsUser = user
        saveProfile()
    }

    /**
     * Logs the local user out of their account
     */
    fun logout() {
        fullName = null
        gogsUser = null
        termsOfUseLastAccepted = 0
        deleteProfile()
    }

    /**
     * Save profile to the preferences
     */
    private fun saveProfile() {
        val profileString = this.toJSON().toString()
        prefs.setPref("profile", profileString)
    }

    /**
     * Deletes the profile from the preferences
     */
    private fun deleteProfile() {
        prefs.setPref<String>("profile", null)
        FileSystem.SYSTEM.deleteRecursively(directoryProvider.sshKeysDir)
    }

    companion object {
        private const val SERIAL_VERSION_UID = 0L

        /**
         * Loads the user profile from json
         * @param json
         * @return
         * @throws Exception
         */
        @Throws(Exception::class)
        fun fromJSON(
            prefs: PreferenceRepository,
            directoryProvider: DirectoryProvider,
            json: JSONObject?
        ): Profile {
            var name: String? = null
            var user: User? = null
            var gogsToken: Token? = null
            var termsLastAccepted = 0

            json?.let { jsonString ->
                val versionUID = jsonString.getLong("serial_version_uid")
                if (versionUID != SERIAL_VERSION_UID) {
                    throw Exception("Unsupported profile version $versionUID. Expected $SERIAL_VERSION_UID")
                }
                if (jsonString.has("full_name")) {
                    name = jsonString.getString("full_name")
                }
                if (jsonString.has("gogs_user")) {
                    user = User.fromJSON(jsonString.getJSONObject("gogs_user"))
                }
                if (jsonString.has("gogs_token")) {
                    gogsToken = Token.fromJSON(jsonString.getJSONObject("gogs_token"))
                }
                if (jsonString.has("terms_last_accepted")) {
                    termsLastAccepted = jsonString.getInt("terms_last_accepted")
                }
            }

            user?.let {
                name = it.fullName
                it.token = gogsToken
            }

            return Profile(prefs, directoryProvider).apply {
                fullName = name
                gogsUser = user
                termsOfUseLastAccepted = termsLastAccepted
            }
        }
    }
}
