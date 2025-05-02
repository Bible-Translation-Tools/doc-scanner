package org.bibletranslationtools.docscanner.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.bibletranslationtools.docscanner.data.local.DirectoryProvider
import org.bibletranslationtools.docscanner.data.local.git.Repo
import java.io.File
import java.io.Serializable

@Entity(tableName = "projects")
data class Project(
    @PrimaryKey(autoGenerate = false)
    val id: String,
    val language: String,
    val book: String,
    val level: String
) : Serializable

fun Project.getRepo(directoryProvider: DirectoryProvider): Repo {
    val dir = File(directoryProvider.projectsDir, getName())
    return Repo(dir.absolutePath)
}

fun Project.getName(): String {
    return "${language}_${book}_$level"
}