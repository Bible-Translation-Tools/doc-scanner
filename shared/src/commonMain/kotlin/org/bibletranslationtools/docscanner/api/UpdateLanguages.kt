package org.bibletranslationtools.docscanner.api

import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readByteArray
import org.bibletranslationtools.docscanner.data.JsonLenient
import org.bibletranslationtools.docscanner.data.models.Language
import org.bibletranslationtools.docscanner.data.repository.LanguageRepository

class UpdateLanguages(
    private val transcriberApi: TranscriberApi,
    private val languageRepository: LanguageRepository
) {
    /**
     * Downloads the latest language list from [TranscriberApi.LANGNAMES_URL] and merges it in.
     * @return number of newly added languages
     */
    suspend fun fromUrl(): Int {
        return merge(transcriberApi.fetchLanguages())
    }

    /**
     * Imports a language list from a locally picked langnames.json file and merges it in.
     * @return number of newly added languages
     */
    suspend fun fromFile(path: Path): Int {
        val bytes = SystemFileSystem.source(path).buffered().use { it.readByteArray() }
        val languages = JsonLenient.decodeFromString<List<Language>>(bytes.decodeToString())
        return merge(languages)
    }

    /**
     * Existing languages are updated in place (by slug) so their id is preserved,
     * since Projects reference languages by id. Languages no longer present
     * upstream are left untouched rather than deleted, to avoid orphaning Projects.
     */
    private suspend fun merge(languages: List<Language>): Int {
        return languageRepository.upsertAll(languages)
    }
}
