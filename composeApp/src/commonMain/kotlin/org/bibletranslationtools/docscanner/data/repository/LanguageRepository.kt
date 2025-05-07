package org.bibletranslationtools.docscanner.data.repository

import org.bibletranslationtools.database.MainDatabase
import org.bibletranslationtools.docscanner.data.models.Language
import org.bibletranslationtools.docscanner.data.models.toEntity
import org.bibletranslationtools.docscanner.data.models.toModel

interface LanguageRepository {
    fun getAll(): List<Language>
    fun deleteAll()
    suspend fun insert(language: Language)
    suspend fun delete(language: Language)
    suspend fun update(language: Language)
}

class LanguageRepositoryImpl(db: MainDatabase) : LanguageRepository {
    private val queries = db.languageQueries

    override fun getAll(): List<Language> {
        return queries.getAll().executeAsList().map { it.toModel() }
    }

    override fun deleteAll() = queries.deleteAll()

    override suspend fun insert(language: Language) {
        val entity = language.toEntity()
        return queries.add(
            entity.slug,
            entity.name,
            entity.angName,
            entity.direction,
            entity.gw
        )
    }

    override suspend fun delete(language: Language) {
        val entity = language.toEntity()
        return queries.delete(entity.id)
    }

    override suspend fun update(language: Language) {
        val entity = language.toEntity()
        return queries.update(
            entity.id,
            entity.slug,
            entity.name,
            entity.angName,
            entity.direction,
            entity.gw,
        )
    }
}