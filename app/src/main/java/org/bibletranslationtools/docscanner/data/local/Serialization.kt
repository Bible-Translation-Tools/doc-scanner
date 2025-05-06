package org.bibletranslationtools.docscanner.data.local

import kotlinx.serialization.json.Json

val JsonLenient = Json {
    ignoreUnknownKeys = true
    isLenient = true
}