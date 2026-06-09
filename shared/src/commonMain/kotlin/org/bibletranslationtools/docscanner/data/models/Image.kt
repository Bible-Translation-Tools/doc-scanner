package org.bibletranslationtools.docscanner.data.models

import kotlinx.serialization.Serializable
import org.bibletranslationtools.docscanner.platform.CommonSerializable

@Serializable
data class Image(
    val path: String,
    val chapter: Int
) : CommonSerializable

