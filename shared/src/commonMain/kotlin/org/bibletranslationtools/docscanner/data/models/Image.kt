package org.bibletranslationtools.docscanner.data.models

import java.io.Serializable

data class Image(
    val path: String,
    val chapter: Int
): Serializable
