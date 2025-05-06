package org.bibletranslationtools.docscanner.data.models

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.Relation
import kotlinx.serialization.Serializable
import org.bibletranslationtools.docscanner.data.local.DirectoryProvider
import org.bibletranslationtools.docscanner.data.local.git.Repo
import java.io.File

@Serializable
@Entity(
    tableName = "projects",
    foreignKeys = [
        ForeignKey(
            entity = Language::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("language_id"),
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Book::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("book_id"),
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Level::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("level_id"),
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Project(
    @PrimaryKey(autoGenerate = false)
    val id: String,
    @ColumnInfo(name = "language_id")
    val languageId: Int,
    @ColumnInfo(name = "book_id")
    val bookId: Int,
    @ColumnInfo(name = "level_id")
    val levelId: Int
) : java.io.Serializable

data class ProjectWithData(
    @Embedded
    val project: Project,
    @Relation(
        parentColumn = "language_id",
        entityColumn = "id"
    )
    val language: Language,
    @Relation(
        parentColumn = "book_id",
        entityColumn = "id"
    )
    val book: Book,
    @Relation(
        parentColumn = "level_id",
        entityColumn = "id"
    )
    val level: Level
)

fun ProjectWithData.getRepo(directoryProvider: DirectoryProvider): Repo {
    val dir = File(directoryProvider.projectsDir, getName())
    return Repo(dir.absolutePath)
}

fun ProjectWithData.getName(): String {
    return "${language.slug}_${book.slug}_${level.slug}"
}