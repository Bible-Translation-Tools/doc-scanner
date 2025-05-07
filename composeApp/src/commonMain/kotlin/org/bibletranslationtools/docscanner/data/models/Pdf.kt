package org.bibletranslationtools.docscanner.data.models

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import org.bibletranslationtools.database.PdfEntity

@Serializable
data class Pdf(
    val id: Int = 0,
    val name: String,
    val size: String,
    val created: LocalDateTime,
    val modified: LocalDateTime,
    val projectId: Int
)

fun Pdf.toEntity() = PdfEntity(
    id = id.toLong(),
    name = name,
    size = size,
    projectId = projectId.toLong(),
    created = created.toString(),
    modified = modified.toString()
)

fun PdfEntity.toModel() = Pdf(
    id = id.toInt(),
    name = name,
    size = size,
    created = LocalDateTime.parse(created),
    modified = LocalDateTime.parse(modified),
    projectId = projectId.toInt(),
)
