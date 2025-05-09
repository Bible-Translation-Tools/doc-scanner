package org.bibletranslationtools.docscanner.api

class HtrLogin(
    private val transcriberApi: TranscriberApi
) {
    suspend fun execute(username: String, password: String): LoginResult {
        val loginResponse = transcriberApi.login(username, password)
        return LoginResult(transcriberApi.getUser(), loginResponse)
    }

    data class LoginResult(
        val user: HtrUser?,
        val response: LoginResponse?
    )
}