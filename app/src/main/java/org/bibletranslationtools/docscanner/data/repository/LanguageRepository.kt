package org.bibletranslationtools.docscanner.data.repository

import android.app.Application
import org.bibletranslationtools.docscanner.data.local.DocScanDatabase
import org.bibletranslationtools.docscanner.data.models.Language

interface LanguageRepository {
    fun getAllLanguages(): List<Language>
    fun getGlLanguages(): List<Language>
    fun getHeartLanguages(): List<Language>
    fun deleteAll(): Int
    suspend fun insert(language: Language): Long
    suspend fun delete(language: Language): Int
    suspend fun update(language: Language): Int
}

class LanguageRepositoryImpl(application: Application) : LanguageRepository {
    private val languageDao = DocScanDatabase.getInstance(application).languageDao

    override fun getAllLanguages() = languageDao.getAllLanguages()

    override fun getGlLanguages() = languageDao.getGlLanguages()

    override fun getHeartLanguages() = languageDao.getHeartLanguages()

    override fun deleteAll() = languageDao.deleteAll()

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