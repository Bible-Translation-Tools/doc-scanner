package org.bibletranslationtools.docscanner.data.local.network

import org.unfoldingword.gogsclient.Response
import org.unfoldingword.gogsclient.User

interface RequestAPI {
    fun get(path: String, userAuth: User): Response
    fun post(path: String, userAuth: User, postData: String): Response
    fun delete(path: String, userAuth: User): Response
    fun put(path: String, userAuth: User, postData: String): Response
}
