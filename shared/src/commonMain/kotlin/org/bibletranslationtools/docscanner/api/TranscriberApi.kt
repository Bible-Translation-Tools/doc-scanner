package org.bibletranslationtools.docscanner.api

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.UserAgent
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.parameters
import io.ktor.serialization.kotlinx.KotlinxSerializationConverter
import kotlinx.serialization.Serializable
import org.bibletranslationtools.docscanner.api.TranscriberApi.Companion.DEFAULT_PROMPT
import org.bibletranslationtools.docscanner.api.TranscriberApi.Companion.DEFAULT_SYSTEM_PROMPT
import org.bibletranslationtools.docscanner.data.JsonLenient
import org.bibletranslationtools.docscanner.data.models.Language
import org.bibletranslationtools.docscanner.data.repository.PreferenceRepository

typealias SyncData = Map<String, SyncItem>

@Serializable
data class ImageRequest(
    val image: String,
    val imageId: String,
    val filename: String,
    val languageCode: String,
    val bookCode: String,
    val chapter: Int = 1,
    val model: String = Model.OPENAI.value,
    val prompt: String = DEFAULT_PROMPT,
    val systemPrompt: String = DEFAULT_SYSTEM_PROMPT,
    val created: Long,
    val processImmediately: Boolean
)

@Serializable
data class ImageResponse(
    val imageId: String,
    val transcription: String,
    val success: Boolean,
    val error: String? = null
)

@Serializable
data class LoginResponse(
    val userId: Int? = null,
    val syncData: SyncData? = null,
    val error: String? = null
)

@Serializable
data class SyncItem(
    val id: String,
    val userId: Int,
    val userDeleted: Boolean,
    val filePath: String,
    val fileName: String,
    val languageCode: String,
    val bookCode: String,
    val chapter: Int,
    val verseStart: Int,
    val verseEnd: Int,
    val transcription: String,
    val created: Long,
    val data: String? = null,
    val filename: String
)

enum class Model(val value: String) {
    OPENAI("openai"),
    GEMINI("gemini"),
    PIXTRAL("pixtral")
}

class TranscriberApi(preferenceRepository: PreferenceRepository) {

    private val cookieStorage = PersistentCookieStorage(preferenceRepository)
    private val logger = KotlinLogging.logger {}

    private val client = HttpClient {
        install(HttpCookies) {
            storage = cookieStorage
        }
        install(HttpRequestRetry) {
            retryOnServerErrors(maxRetries = 5)
            exponentialDelay()
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 60000
            connectTimeoutMillis = 60000
            socketTimeoutMillis = 60000
        }
        install(ContentNegotiation) {
            register(
                ContentType.Application.Json,
                KotlinxSerializationConverter(JsonLenient)
            )
        }
        install(UserAgent) {
            agent = "btt-writer-android" //"btt-doc-scanner"
        }
    }

    suspend fun login(username: String, password: String): LoginResponse? {
        val response = client.submitForm(
            url = "$BASE_API_URL/auth/login",
            formParameters = parameters {
                append("username", username)
                append("password", password)
            }
        ) {
            contentType(ContentType.Application.Json)
        }

        return if (response.status.value == 200) {
            response.body<LoginResponse>()
        } else {
            logger.error { "Login failed with status code ${response.status.value}" }
            null
        }
    }

    fun logout() = cookieStorage.cleanup()

    fun getUser(): HtrUser? {
        val session = cookieStorage.getSession() ?: return null
        return try {
            JsonLenient.decodeFromString<HtrUser>(session)
        } catch (e: Exception) {
            logger.error(e) { "Error decoding user session" }
            null
        }
    }

    suspend fun uploadImage(request: ImageRequest): ImageResponse? {
        val response = client.post("$BASE_API_URL/transcriber/") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        return if (response.status.value == 200) {
            response.body<ImageResponse>()
        } else {
            logger.error { "Upload failed with status code ${response.status.value}" }
            null
        }
    }

    suspend fun fetchLanguages(): List<Language> {
        return client.get(LANGNAMES_URL).body()
    }

    internal companion object {
        const val BASE_URL = "https://transcriber.wycliffe-associates-account.workers.dev"
        const val BASE_API_URL = "$BASE_URL/api/v1"
        const val DEFAULT_PROMPT = "Carefully read the text in this image and provide only the transcription in the 'transcription' field of the JSON response."
        const val DEFAULT_SYSTEM_PROMPT = "You are an expert at reading and transcribing text from images (including handwriting, print, and mixed languages). You must return a JSON object with exactly one key, 'transcription', whose value is the transcribed text from the image. Preserve the original language(s), numbers, punctuation, and line breaks as much as possible. Do not add explanations, labels, or metadata. If a small part is unreadable, skip or approximate it rather than inventing new content. Output only valid JSON that matches the expected schema."
        const val LANGNAMES_URL = "https://langnames.bibleineverylanguage.org/langnames.json"
    }
}