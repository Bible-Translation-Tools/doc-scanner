package org.bibletranslationtools.docscanner.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "levels")
data class Level(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val slug: String,
    val name: String
) : java.io.Serializable