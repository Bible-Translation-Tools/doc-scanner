package org.bibletranslationtools.docscanner.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "books")
data class Book(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val slug: String,
    val name: String,
    @SerialName("anth")
    val anthology: String,
    @SerialName("num")
    val sort: Int
) : java.io.Serializable