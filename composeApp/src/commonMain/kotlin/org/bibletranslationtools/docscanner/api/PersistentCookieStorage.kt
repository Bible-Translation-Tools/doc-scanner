package org.bibletranslationtools.docscanner.api

import io.ktor.client.plugins.cookies.CookiesStorage
import io.ktor.http.Cookie
import io.ktor.http.CookieEncoding
import io.ktor.http.Url
import io.ktor.http.decodeCookieValue
import io.ktor.http.encodeCookieValue
import org.bibletranslationtools.docscanner.data.repository.PreferenceRepository
import org.bibletranslationtools.docscanner.data.repository.getPref
import org.bibletranslationtools.docscanner.data.repository.setPref

private enum class CookieType(val value: String) {
    TOKEN("transcriber_api_token"),
    LOCALE("i18next")
}

class PersistentCookieStorage(
    private val prefs: PreferenceRepository
) : CookiesStorage {

    override suspend fun get(requestUrl: Url) = mutableListOf<Cookie>().apply {
        CookieType.entries.forEach { cookie ->
            prefs.getPref<String>(cookie.value)?.let {
                val base64Decoded = decodeCookieValue(it, CookieEncoding.BASE64_ENCODING)
                val uriDecoded = decodeCookieValue(base64Decoded, CookieEncoding.URI_ENCODING)
                add(Cookie(cookie.value, uriDecoded))
            }
        }
    }

    override suspend fun addCookie(requestUrl: Url, cookie: Cookie) {
        if (CookieType.entries.any { it.value == cookie.name }) {
            val encodedCookie = encodeCookieValue(cookie.value, CookieEncoding.BASE64_ENCODING)
            prefs.setPref(cookie.name, encodedCookie)
        }
    }

    override fun close() {
    }

    fun getSession(): String? {
        return prefs.getPref<String>(CookieType.TOKEN.value)?.let { cookie ->
            val base64Decoded = decodeCookieValue(cookie, CookieEncoding.BASE64_ENCODING)
            decodeCookieValue(base64Decoded, CookieEncoding.URI_ENCODING)
        }
    }

    fun cleanup() {
        CookieType.entries.forEach { cookie ->
            prefs.setPref<String>(cookie.value, null)
        }
    }
}