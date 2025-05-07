package org.bibletranslationtools.docscanner.data.models

import kotlinx.serialization.Serializable
import org.bibletranslationtools.database.PdfEntity

@Serializable
data class Pdf(
    val id: Int = 0,
    val name: String,
    val size: String,
    val created: String,
    val modified: String,
    val projectId: Int
) : java.io.Serializable

fun Pdf.toEntity() = PdfEntity(
    id = id.toLong(),
    name = name,
    size = size,
    projectId = projectId.toLong(),
    created = created,
    modified = modified
)

fun PdfEntity.toModel() = Pdf(
    id = id.toInt(),
    name = name,
    size = size,
    created = created,
    modified = modified,
    projectId = projectId.toInt(),
)
