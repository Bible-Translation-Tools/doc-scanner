package org.bibletranslationtools.docscanner.data.repository

import android.app.Application
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import org.bibletranslationtools.docscanner.data.local.DocScanDatabase
import org.bibletranslationtools.docscanner.data.models.Language

interface LanguageRepository {
    fun getAllLanguages(): Flow<List<Language>>
    fun getGlLanguages(): Flow<List<Language>>
    fun getHeartLanguages(): Flow<List<Language>>
    suspend fun insert(language: Language): Long
    suspend fun delete(language: Language): Int
    suspend fun update(language: Language): Int
}

class LanguageRepositoryImpl(application: Application) : LanguageRepository {
    private val languageDao = DocScanDatabase.getInstance(application).languageDao

    override fun getAllLanguages() = languageDao.getAllLanguages().flowOn(Dispatchers.IO)

    override fun getGlLanguages() = languageDao.getGlLanguages().flowOn(Dispatchers.IO)

    override fun getHeartLanguages() = languageDao.getHeartLanguages().flowOn(Dispatchers.IO)

    override suspend fun insert(language: Language): Long {
        return languageDao.insert(language)
    }

    override suspend fun delete(language: Language): Int {
        return languageDao.delete(language)
    }

    override suspend fun update(language: Language): Int {
        return languageDao.update(language)
    }
}