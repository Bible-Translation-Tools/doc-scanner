package org.bibletranslationtools.docscanner.data

import kotlinx.serialization.json.Json

val JsonLenient = Json {
    ignoreUnknownKeys = true
    isLenient = true
    encodeDefaults = true
}