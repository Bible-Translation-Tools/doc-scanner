package org.bibletranslationtools.docscanner.data.models

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import org.bibletranslationtools.database.ProjectEntity
import org.bibletranslationtools.database.ProjectWithData
import org.bibletranslationtools.docscanner.data.local.DirectoryProvider
import org.bibletranslationtools.docscanner.data.local.git.Repo

@Serializable
data class Project(
    val id: Int = 0,
    val language: Language,
    val book: Book,
    val level: Level,
    val created: LocalDateTime,
    val modified: LocalDateTime
)

fun Project.getRepo(directoryProvider: DirectoryProvider): Repo {
    val dir = directoryProvider.projectsDir / getName()
    return Repo(dir.toString())
}

fun Project.getName(): String {
    return "${language.slug}_${book.slug}_${level.slug}"
}

fun Project.toEntity(): ProjectEntity {
    return ProjectEntity(
        id = id.toLong(),
        languageId = language.id.toLong(),
        bookId = book.id.toLong(),
        levelId = level.id.toLong(),
        created = created.toString(),
        modified = modified.toString()
    )
}

fun ProjectWithData.toModel(): Project {
    return Project(
        id = id.toInt(),
        language = Language(
            id = languageId.toInt(),
            slug = langSlug!!,
            name = langName!!,
            angName = langAngName!!,
            direction = langDirection!!,
            gw = langGw == 1L
        ),
        book = Book(
            id = bookId.toInt(),
            slug = bookSlug!!,
            name = bookName!!,
            anthology = bookAnthology!!,
            sort = bookSort!!.toInt()
        ),
        level = Level(
            id = levelId.toInt(),
            slug = levelSlug!!,
            name = levelName!!
        ),
        created = LocalDateTime.parse(created),
        modified = LocalDateTime.parse(modified)
    )
}