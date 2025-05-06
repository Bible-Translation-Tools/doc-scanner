package org.bibletranslationtools.docscanner.data.repository

import android.app.Application
import org.bibletranslationtools.docscanner.data.local.DocScanDatabase
import org.bibletranslationtools.docscanner.data.models.Level

interface LevelRepository {
    fun getAllLevels(): List<Level>
    suspend fun deleteAll(): Int
    suspend fun insert(level: Level): Long
    suspend fun delete(level: Level): Int
    suspend fun update(level: Level): Int
}

class LevelRepositoryImpl(application: Application) : LevelRepository {
    private val levelDao = DocScanDatabase.getInstance(application).levelDao

    override fun getAllLevels() = levelDao.getAllLevels()

    override suspend fun deleteAll() = levelDao.deleteAll()

    override suspend fun insert(level: Level): Long {
        return levelDao.insert(level)
    }

    override suspend fun delete(level: Level): Int {
        return levelDao.delete(level)
    }

    override suspend fun update(level: Level): Int {
        return levelDao.update(level)
    }
}