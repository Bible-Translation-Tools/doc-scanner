package org.bibletranslationtools.docscanner.data.models

import kotlinx.serialization.Serializable
import org.bibletranslationtools.database.LevelEntity

@Serializable
data class Level(
    val id: Int = 0,
    val slug: String,
    val name: String
) : java.io.Serializable

fun Level.toEntity(): LevelEntity {
    return LevelEntity(
        id = id.toLong(),
        slug = slug,
        name = name
    )
}

fun LevelEntity.toModel(): Level {
    return Level(
        id = id.toInt(),
        slug = slug,
        name = name
    )
}