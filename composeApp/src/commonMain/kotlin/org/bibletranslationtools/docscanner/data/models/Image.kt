package org.bibletranslationtools.docscanner.data.models

import kotlinx.serialization.Serializable
import org.bibletranslationtools.database.ImageEntity

@Serializable
data class Image(
    val id: Long = 0,
    val name: String,
    val size: String,
    val created: Long,
    val pdfId: Long
) : java.io.Serializable

fun Image.toEntity() = ImageEntity(
    id = id,
    name = name,
    size = size,
    created = created,
    pdfId = pdfId
)

fun ImageEntity.toModel() = Image(
    id = id,
    name = name,
    size = size,
    created = created,
    pdfId = pdfId
)
