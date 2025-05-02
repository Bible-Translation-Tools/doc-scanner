package org.bibletranslationtools.docscanner.data.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.io.Serializable
import java.util.Date

@Entity(
    tableName = "pdfs",
    foreignKeys = [
        ForeignKey(
            entity = Project::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("project_id"),
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE
        )]
)
data class Pdf(
    @PrimaryKey(autoGenerate = false)
    val id: String,
    val name: String,
    val size: String,
    @ColumnInfo(name = "last_modified")
    val lastModified: Date,
    @ColumnInfo(name = "project_id")
    val projectId: String
) : Serializable
