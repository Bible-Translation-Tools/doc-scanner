package org.bibletranslationtools.docscanner.data.repository

import android.app.Application
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import org.bibletranslationtools.docscanner.data.local.DocScanDatabase
import org.bibletranslationtools.docscanner.data.models.Level

interface LevelRepository {
    fun getAllLevels(): Flow<List<Level>>
    suspend fun insert(level: Level): Long
    suspend fun delete(level: Level): Int
    suspend fun update(level: Level): Int
}

class LevelRepositoryImpl(application: Application) : LevelRepository {
    private val levelDao = DocScanDatabase.getInstance(application).levelDao

    override fun getAllLevels() = levelDao.getAllLevels().flowOn(Dispatchers.IO)

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