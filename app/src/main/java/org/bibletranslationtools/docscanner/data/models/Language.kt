package org.bibletranslationtools.docscanner.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "languages")
data class Language(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
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