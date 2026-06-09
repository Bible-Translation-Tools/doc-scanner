package org.bibletranslationtools.docscanner.utils

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.number

fun String.toLocalDateTime(): LocalDateTime {
    return LocalDateTime.parse(this)
}

fun LocalDateTime.format(): String {
    val builder = StringBuilder()
    builder.append(year)
    builder.append("-")
    builder.append(day.toString().padStart(2, '0'))
    builder.append("-")
    builder.append(month.number.toString().padStart(2, '0'))
    builder.append(" ")
    builder.append(hour.toString().padStart(2, '0'))
    builder.append(":")
    builder.append(minute.toString().padStart(2, '0'))
    builder.append(":")
    builder.append(second.toString().padStart(2, '0'))

    return builder.toString()
}

fun String.trimMultiline(): String {
    return this.trimIndent().replace("\n", "")
}

inline fun <T : CharSequence> T?.ifNotNullOrEmpty(block: (T) -> String): String {
    return if (!this.isNullOrEmpty()) block(this) else ""
}