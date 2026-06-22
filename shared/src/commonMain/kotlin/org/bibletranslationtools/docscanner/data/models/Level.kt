package org.bibletranslationtools.docscanner.data.models

import kotlinx.serialization.Serializable
import org.bibletranslationtools.database.LevelEntity
import org.bibletranslationtools.docscanner.platform.CommonSerializable

@Serializable
data class Level(
    val id: Long = 0,
    val slug: String,
    val name: String
) : CommonSerializable {
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