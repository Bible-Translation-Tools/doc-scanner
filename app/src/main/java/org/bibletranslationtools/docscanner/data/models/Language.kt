package org.bibletranslationtools.docscanner.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "languages")
data class Language(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val slug: String,
    val name: String,
    val angName: String,
    val direction: String,
    val gw: Boolean
) : Serializable