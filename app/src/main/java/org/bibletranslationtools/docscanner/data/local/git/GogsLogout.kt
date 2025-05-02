package org.bibletranslationtools.docscanner.data.local.git

import android.content.Context
import org.bibletranslationtools.docscanner.R
import org.bibletranslationtools.docscanner.data.local.Settings
import org.bibletranslationtools.docscanner.data.local.network.OkHttpRequest
import org.bibletranslationtools.docscanner.data.local.network.RequestAPI
import org.bibletranslationtools.docscanner.data.repository.PreferenceRepository
import org.bibletranslationtools.docscanner.data.repository.getPref
import org.json.JSONArray
import org.json.JSONException
import org.unfoldingword.gogsclient.User

class GogsLogout(
    private val context: Context,
    private val prefs: PreferenceRepository
) {
    fun execute(profile: Profile) {
        // local user (non-server account)
        val user = profile.gogsUser ?: return
        val tokenName = user.token.name
        val tokenSha1 = user.token.toString()

        // uses Basic authorization scheme, token should be null
        user.password = tokenSha1
        user.token = null

        val apiUrl = prefs.getPref(
            Settings.KEY_PREF_GOGS_API,
            context.resources.getString(R.string.pref_default_gogs_api)
        )
        val requester = OkHttpRequest(apiUrl)
        val tokenId = getTokenId(user, requester, tokenName)
        if (tokenId < 0) {
            return
        }
        deleteToken(user, tokenId, requester)
    }

    private fun getTokenId(
        user: User,
        requester: RequestAPI,
        tokenName: String
    ): Int {
        var tokenId = -1
        val requestPath = String.format("/users/%s/tokens", user.username)

        val tokenResponse = requester.get(requestPath, user)
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

    private fun deleteToken(
        user: User,
        tokenId: Int,
        requester: RequestAPI
    ) {
        val path = String.format("/users/%s/tokens/%s", user.username, tokenId)
        val response = requester.delete(path, user)

        if (response.code != 200 || response.code != 204) {
            println("delete access token - gogs api responded with code " + response.code)
            response.exception?.printStackTrace()
        }
    }
}