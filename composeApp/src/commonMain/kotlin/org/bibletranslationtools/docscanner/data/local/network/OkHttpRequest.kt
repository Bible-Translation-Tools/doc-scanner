package org.bibletranslationtools.docscanner.data.local.network

import android.util.Base64
import android.util.Base64.encodeToString
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.unfoldingword.gogsclient.Response
import org.unfoldingword.gogsclient.User
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.util.concurrent.TimeUnit

class OkHttpRequest(apiUrl: String) : RequestAPI {
    private val client: OkHttpClient
    private val readTimeout = 5000
    private val connectionTimeout = 5000
    private val baseUrl: String

    init {
        client = OkHttpClient.Builder()
            .connectTimeout(connectionTimeout.toLong(), TimeUnit.MILLISECONDS)
            .readTimeout(readTimeout.toLong(), TimeUnit.MILLISECONDS)
            .build()

        this.baseUrl = apiUrl.replace("/+$".toRegex(), "")
    }

    /**
     * @param path the api path
     * @param userAuth the user authentication info. Requires account token or credentials
     * @return
     */
    override fun get(
        path: String,
        userAuth: User
    ): Response {
        var responseCode = 0
        var responseData: String? = null
        var exception: Exception? = null

        val auth = encodeAuthHeader(userAuth)

        val request = Request.Builder()
            .url(baseUrl + path)
            .addHeader("Authorization", auth)
            .get()
            .build()

        try {
            val response = client.newCall(request).execute()
            responseCode = response.code
            responseData = response.body!!.string()
        } catch (ex: IOException) {
            //Logger.w(OkHttpRequest.class.getName(), "Request failed with an exception.", ex);
            ex.printStackTrace()
            exception = ex
        }

        return Response(responseCode, responseData, exception)
    }

    /**
     * @param path the api path
     * @param userAuth the user authentication info. Requires account token or credentials
     * @param postData data (body) to submit
     * @return
     */
    override fun post(
        path: String,
        userAuth: User,
        postData: String
    ): Response {
        var responseCode = 0
        var responseData: String? = null
        var exception: Exception? = null

        val auth = encodeAuthHeader(userAuth)

        val body: RequestBody = postData
            .toRequestBody("application/json; charset=utf-8".toMediaType())

        val request = Request.Builder()
            .url(baseUrl + path)
            .addHeader("Authorization", auth)
            .post(body)
            .build()

        try {
            val response = client.newCall(request).execute()
            responseCode = response.code
            responseData = response.body!!.string()
        } catch (ex: IOException) {
            //Logger.w(OkHttpRequest::class.java.getName(), "Request failed with an exception.", ex)
            ex.printStackTrace()
            exception = ex
        }

        return Response(responseCode, responseData, exception)
    }

    /**
     * @param path the api path
     * @param userAuth the user authentication info. Requires account token or credentials
     * @return
     */
    override fun delete(
        path: String,
        userAuth: User
    ): Response {
        var responseCode = 0
        var exception: Exception? = null

        val auth = encodeAuthHeader(userAuth)

        val request = Request.Builder()
            .url(baseUrl + path)
            .addHeader("Authorization", auth)
            .delete()
            .build()

        try {
            val response = client.newCall(request).execute()
            responseCode = response.code
        } catch (ex: IOException) {
            //Logger.w(OkHttpRequest::class.java.getName(), "Request failed with an exception.", ex)
            ex.printStackTrace()
            exception = ex
        }

        return Response(responseCode, null, exception)
    }

    /**
     * See post() method for more detail
     */
    override fun put(
        path: String,
        userAuth: User,
        postData: String
    ): Response {
        var responseCode = 0
        var responseData: String? = null
        var exception: Exception? = null

        val auth = encodeAuthHeader(userAuth)

        val body: RequestBody = postData
            .toRequestBody("application/json; charset=utf-8".toMediaType())

        val request = Request.Builder()
            .url(baseUrl + path)
            .addHeader("Authorization", auth)
            .put(body)
            .build()

        try {
            val response = client.newCall(request).execute()
            responseCode = response.code
            responseData = response.body!!.string()
        } catch (ex: IOException) {
            //Logger.w(OkHttpRequest::class.java.getName(), "Request failed with an exception.", ex)
            ex.printStackTrace()
            exception = ex
        }

        return Response(responseCode, responseData, exception)
    }


    /**
     * Generates the authentication value from the use token or credentials
     */
    private fun encodeAuthHeader(user: User): String {
        return if (user.token != null) {
            "token " + user.token
        } else if (user.username != null && !user.username
                .isEmpty() && user.password != null && !user.password.isEmpty()
        ) {
            val credentials = user.username + ":" + user.password
            try {
                "Basic " + encodeToString(
                    credentials.toByteArray(charset("UTF-8")), Base64.NO_WRAP
                )
            } catch (e: UnsupportedEncodingException) {
                e.printStackTrace()
                ""
            }
        } else {
            ""
        }
    }
}