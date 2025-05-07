package org.bibletranslationtools.docscanner.data.repository

import org.bibletranslationtools.database.MainDatabase
import org.bibletranslationtools.docscanner.data.models.Project
import org.bibletranslationtools.docscanner.data.models.toEntity
import org.bibletranslationtools.docscanner.data.models.toModel

interface ProjectRepository {
    fun getAll(): List<Project>
    suspend fun insert(project: Project)
    suspend fun delete(project: Project)
    suspend fun update(project: Project)
}

class ProjectRepositoryImpl(db: MainDatabase) : ProjectRepository {
    private val queries = db.projectQueries

    override fun getAll() = queries.projectWithData().executeAsList()
        .map { it.toModel() }

    override suspend fun insert(project: Project) {
        val entity = project.toEntity()
        return queries.add(
            entity.languageId,
            entity.bookId,
            entity.levelId
        )
    }

    override suspend fun delete(project: Project) {
        val entity = project.toEntity()
        return queries.delete(entity.id)
    }

    override suspend fun update(project: Project) {
        val entity = project.toEntity()
        return queries.update(
            entity.id,
            entity.languageId,
            entity.bookId,
            entity.levelId,
            entity.created,
            entity.modified
        )
    }
}