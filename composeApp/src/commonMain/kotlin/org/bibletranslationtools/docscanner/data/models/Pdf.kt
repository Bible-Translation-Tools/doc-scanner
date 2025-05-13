package org.bibletranslationtools.docscanner.data.models

import kotlinx.serialization.Serializable
import org.bibletranslationtools.database.PdfEntity

@Serializable
data class Pdf(
    val id: Long = 0,
    val name: String,
    val size: String,
    val created: String,
    val modified: String,
    val projectId: Long
) : java.io.Serializable

fun Pdf.toEntity() = PdfEntity(
    id = id,
    name = name,
    size = size,
    projectId = projectId,
    created = created,
    modified = modified
)

fun PdfEntity.toModel() = Pdf(
    id = id,
    name = name,
    size = size,
    created = created,
    modified = modified,
    projectId = projectId
)
