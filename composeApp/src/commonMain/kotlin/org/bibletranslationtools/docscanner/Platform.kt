package org.bibletranslationtools.docscanner

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform