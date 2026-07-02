package org.bibletranslationtools.docscanner.data.repository

import org.bibletranslationtools.database.MainDatabase
import org.bibletranslationtools.docscanner.data.models.Level
import org.bibletranslationtools.docscanner.data.models.toEntity
import org.bibletranslationtools.docscanner.data.models.toModel

interface LevelRepository {
    fun getAll(): List<Level>
    suspend fun deleteAll()
    suspend fun insert(level: Level)
    suspend fun delete(level: Level)
    suspend fun update(level: Level)

    /** Inserts all [levels] in a single database transaction. */
    suspend fun insertAll(levels: List<Level>)
}

class LevelRepositoryImpl(private val db: MainDatabase) : LevelRepository {
    private val queries = db.levelQueries

    override fun getAll() = queries.getAll().executeAsList().map { it.toModel() }

    override suspend fun deleteAll() {
        queries.deleteAll()
    }

    override suspend fun insert(level: Level) {
        val entity = level.toEntity()
        queries.add(
            entity.slug,
            entity.name
        )
    }

    override suspend fun delete(level: Level) {
        val entity = level.toEntity()
        queries.delete(entity.id)
    }

    override suspend fun update(level: Level) {
        val entity = level.toEntity()
        queries.update(
            entity.id,
            entity.slug,
            entity.name
        )
    }

    override suspend fun insertAll(levels: List<Level>) {
        db.transaction {
            levels.forEach { level ->
                val entity = level.toEntity()
                queries.add(
                    entity.slug,
                    entity.name
                )
            }
        }
    }
}