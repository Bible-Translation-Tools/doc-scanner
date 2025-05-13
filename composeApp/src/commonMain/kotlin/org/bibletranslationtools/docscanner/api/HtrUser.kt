package org.bibletranslationtools.docscanner.api

import kotlinx.serialization.Serializable
import org.json.JSONObject
import org.unfoldingword.gogsclient.User

@Serializable
data class HtrUser(
    val wacsUserId: Int,
    val wacsLogin: String,
    val wacsUserEmail: String,
    val wacsUserAvatarUrl: String,
    val wacsUserLanguage: String,
    val wacsUsername: String,
    val tokenId: Int,
    val tokenName: String,
    val tokenSha1: String,
    val tokenLastEight: String,
    val tokenScopes: List<String>?,
) : java.io.Serializable

fun HtrUser.toGogsUser(): User {
    val json = JSONObject()
    json.put("id", this.wacsUserId)
    json.put("login_name", this.wacsLogin)
    json.put("email", this.wacsUserEmail)
    json.put("avatar_url", this.wacsUserAvatarUrl)
    json.put("username", this.wacsUsername)
    json.put("token", this.tokenSha1)
    return User.fromJSON(json)
}
