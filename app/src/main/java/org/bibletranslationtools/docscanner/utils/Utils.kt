package org.bibletranslationtools.docscanner.utils

import android.content.Context
import android.os.Build
import android.widget.Toast

fun Context.showToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

fun identificator(): String {
    return Build.MODEL.lowercase().replace(" ", "_")
}