package org.bibletranslationtools.docscanner.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bibletranslationtools.database.BookEntity

@Serializable
data class Book(
    val id: Long = 0,
    val slug: String,
    val name: String,
    @SerialName("anth")
    val anthology: String,
    @SerialName("num")
    val sort: Long
) : java.io.Serializable {
    override fun toString(): String {
        return "[$slug] $name"
    }
}

fun Book.toEntity(): BookEntity {
    return BookEntity(
        id = id,
        slug = slug,
        name = name,
        anthology = anthology,
        sort = sort
    )
}

fun BookEntity.toModel(): Book {
    return Book(
        id = id,
        slug = slug,
        name = name,
        anthology = anthology,
        sort = sort
    )
}