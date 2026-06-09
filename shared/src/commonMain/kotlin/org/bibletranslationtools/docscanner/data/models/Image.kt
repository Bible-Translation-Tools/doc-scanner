package org.bibletranslationtools.docscanner.data.models

import kotlinx.serialization.Serializable

@Serializable
data class Image(
    val path: String,
    val chapter: Int
)
