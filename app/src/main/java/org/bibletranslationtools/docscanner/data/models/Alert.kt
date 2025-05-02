package org.bibletranslationtools.docscanner.data.models

data class Alert(
    val message: String,
    val onClosed: () -> Unit = {}
)