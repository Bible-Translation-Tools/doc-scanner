package org.bibletranslationtools.docscanner.utils

import android.content.Context
import android.os.Build
import android.widget.Toast
import kotlinx.datetime.LocalDateTime

fun Context.showToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

fun String.toLocalDateTime(): LocalDateTime {
    return LocalDateTime.parse(this)
}

fun LocalDateTime.format(): String {
    val builder = StringBuilder()
    builder.append(year)
    builder.append("-")
    builder.append(dayOfMonth.toString().padStart(2, '0'))
    builder.append("-")
    builder.append(monthNumber.toString().padStart(2, '0'))
    builder.append(" ")
    builder.append(hour.toString().padStart(2, '0'))
    builder.append(":")
    builder.append(minute.toString().padStart(2, '0'))
    builder.append(":")
    builder.append(second.toString().padStart(2, '0'))

    return builder.toString()
}

fun identificator(): String {
    return Build.MODEL.lowercase().replace(" ", "_")
}