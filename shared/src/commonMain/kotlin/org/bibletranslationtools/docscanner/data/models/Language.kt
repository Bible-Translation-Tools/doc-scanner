package org.bibletranslationtools.docscanner.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bibletranslationtools.database.LanguageEntity

@Serializable
data class Language(
    val id: Long = 0,
    @SerialName("lc")
    val slug: String,
    @SerialName("ln")
    val name: String,
    @SerialName("ang")
    val angName: String,
    @SerialName("ld")
    val direction: String,
    val gw: Boolean
) : java.io.Serializable {

    override fun toString(): String {
        val language = if (name != angName) {
            "$name ($angName)"
        } else name
        return "[$slug] $language"
    }
}

fun Language.toEntity(): LanguageEntity {
    return LanguageEntity(
        id = id,
        slug = slug,
        name = name,
        angName = angName,
        direction = direction,
        gw = if (gw) 1 else 0
    )
}

fun LanguageEntity.toModel(): Language {
    return Language(
        id = id,
        slug = slug,
        name = name,
        angName = angName,
        direction = direction,
        gw = gw == 1L
    )
}