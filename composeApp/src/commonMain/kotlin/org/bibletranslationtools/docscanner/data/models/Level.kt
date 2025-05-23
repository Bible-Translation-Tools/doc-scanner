package org.bibletranslationtools.docscanner.data.models

import kotlinx.serialization.Serializable
import org.bibletranslationtools.database.LevelEntity

@Serializable
data class Level(
    val id: Long = 0,
    val slug: String,
    val name: String
) : java.io.Serializable {
    override fun toString(): String {
        return name
    }
}

fun Level.toEntity(): LevelEntity {
    return LevelEntity(
        id = id,
        slug = slug,
        name = name
    )
}

fun LevelEntity.toModel(): Level {
    return Level(
        id = id,
        slug = slug,
        name = name
    )
}