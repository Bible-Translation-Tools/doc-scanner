package org.bibletranslationtools.docscanner.data.models

import kotlinx.io.files.Path
import kotlinx.serialization.Serializable
import org.bibletranslationtools.database.ProjectEntity
import org.bibletranslationtools.database.ProjectWithData
import org.bibletranslationtools.docscanner.data.repository.DirectoryProvider
import org.bibletranslationtools.docscanner.data.git.Repo

@Serializable
data class Project(
    val id: Long = 0,
    val language: Language,
    val book: Book,
    val level: Level,
    val created: String,
    val modified: String
) : java.io.Serializable

fun Project.getRepo(directoryProvider: DirectoryProvider): Repo {
    val dir = Path(directoryProvider.projectsDir, getName())
    return Repo(dir.toString())
}

fun Project.getName(): String {
    return "${language.slug}_${book.slug}_${level.slug}"
}

fun Project.getTitle(): String {
    return "${language.name} - ${book.name} - ${level.name}"
}

fun Project.toEntity(): ProjectEntity {
    return ProjectEntity(
        id = id,
        languageId = language.id,
        bookId = book.id,
        levelId = level.id,
        created = created,
        modified = modified
    )
}

fun ProjectWithData.toModel(): Project {
    return Project(
        id = id,
        language = Language(
            id = languageId,
            slug = langSlug!!,
            name = langName!!,
            angName = langAngName!!,
            direction = langDirection!!,
            gw = langGw == 1L
        ),
        book = Book(
            id = bookId,
            slug = bookSlug!!,
            name = bookName!!,
            anthology = bookAnthology!!,
            sort = bookSort!!
        ),
        level = Level(
            id = levelId,
            slug = levelSlug!!,
            name = levelName!!
        ),
        created = created,
        modified = modified
    )
}