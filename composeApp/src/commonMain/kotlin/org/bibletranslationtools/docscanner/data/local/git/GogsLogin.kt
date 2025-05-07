package org.bibletranslationtools.docscanner.data.local.git

import android.os.Build
import docscanner.composeapp.generated.resources.Res
import docscanner.composeapp.generated.resources.gogs_token_name
import docscanner.composeapp.generated.resources.gogs_user_agent
import docscanner.composeapp.generated.resources.pref_default_gogs_api
import org.bibletranslationtools.docscanner.data.local.Settings
import org.bibletranslationtools.docscanner.data.local.network.OkHttpRequest
import org.bibletranslationtools.docscanner.data.local.network.RequestAPI
import org.bibletranslationtools.docscanner.data.repository.PreferenceRepository
import org.bibletranslationtools.docscanner.data.repository.getPref
import org.bibletranslationtools.docscanner.utils.identificator
import org.jetbrains.compose.resources.getString
import org.json.JSONArray
import org.json.JSONException
import org.unfoldingword.gogsclient.GogsAPI
import org.unfoldingword.gogsclient.Token
import org.unfoldingword.gogsclient.User

class GogsLogin(
    private val prefRepository: PreferenceRepository
) {
    suspend fun execute(
        username: String,
        password: String,
        fullName: String? = null
    ): LoginResult {
        val apiUrl = prefRepository.getPref(
            Settings.KEY_PREF_GOGS_API,
            getString(Res.string.pref_default_gogs_api)
        )
        val api = GogsAPI(apiUrl, getString(Res.string.gogs_user_agent))
        val authUser = User(username, password)
        val tokenName = getTokenStub()

        // get user
        val user = api.getUser(authUser, authUser)
        if (user != null) {
            val customRequester: RequestAPI = OkHttpRequest(apiUrl)
            val tokenId = getTokenId(tokenName, authUser, customRequester)
            if (tokenId != -1) {
                // Delete (if exists) matching token for this device on server
                deleteToken(tokenId, authUser, customRequester)
            }

            // Create a new token
            val t = Token(tokenName, arrayOf("write:repository", "write:user"))
            user.token = api.createToken(t, authUser)

            // validate access token
            if (user.token == null) {
                val response = api.lastResponse
                println("gogs api responded with " + response.code + ": " + response.toString())
                response.exception?.printStackTrace()
                return LoginResult(null)
            }

            // set missing full_name
            if (user.fullName.isNullOrEmpty() && !fullName.isNullOrEmpty()) {
                user.fullName = fullName
                val updatedUser = api.editUser(user, authUser)
                if (updatedUser == null) {
                    val response = api.lastResponse
                    println("The full_name could not be updated gogs api responded with " + response.code + ": " + response.toString())
                    response.exception?.printStackTrace()
                }
            } else {
                user.fullName = user.username
            }
        }

        return LoginResult(user)
    }

    private suspend fun getTokenStub(): String {
        val defaultTokenName = getString(Res.string.gogs_token_name)
        val androidId = Build.DEVICE.lowercase()
        val nickname = identificator()
        val tokenSuffix = String.format("%s_%s__%s", Build.MANUFACTURER, nickname, androidId)
        return (defaultTokenName + "__" + tokenSuffix).replace(" ", "_")
    }

    private fun getTokenId(tokenName: String, userAuth: User, requester: RequestAPI): Int {
        var tokenId = -1
        val urlPath = String.format("/users/%s/tokens", userAuth.username)
        val tokenResponse = requester.get(urlPath, userAuth)

        if (tokenResponse.code == 200) {
            try {
                val data = JSONArray(tokenResponse.data)
                for (i in 0 until data.length()) {
                    val tkName = data.getJSONObject(i).getString("name")

                    if (tkName == tokenName) {
                        tokenId = data.getJSONObject(i).getInt("id")
                        break
                    }
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }

        return tokenId
    }

    private fun deleteToken(tokenId: Int, userAuth: User, requester: RequestAPI) {
        val urlPath = String.format("/users/%s/tokens/%s", userAuth.username, tokenId)
        val response = requester.delete(urlPath, userAuth)

        if (response.code != 204) {
            println("delete access token - gogs api responded with code " + response.code)
            response.exception?.printStackTrace()
        }
    }

    data class LoginResult(val user: User?)
}