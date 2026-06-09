package org.bibletranslationtools.docscanner.api

import kotlinx.serialization.Serializable
import org.bibletranslationtools.docscanner.platform.CommonSerializable

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
) : CommonSerializable
