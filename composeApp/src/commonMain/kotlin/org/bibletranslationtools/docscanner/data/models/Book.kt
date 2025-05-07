package org.bibletranslationtools.docscanner.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bibletranslationtools.database.BookEntity

@Serializable
data class Book(
    val id: Int = 0,
    val slug: String,
    val name: String,
    @SerialName("anth")
    val anthology: String,
    @SerialName("num")
    val sort: Int
)

fun Book.toEntity(): BookEntity {
    return BookEntity(
        id = id.toLong(),
        slug = slug,
        name = name,
        anthology = anthology,
        sort = sort.toLong()
    )
}

fun BookEntity.toModel(): Book {
    return Book(
        id = id.toInt(),
        slug = slug,
        name = name,
        anthology = anthology,
        sort = sort.toInt()
    )
}