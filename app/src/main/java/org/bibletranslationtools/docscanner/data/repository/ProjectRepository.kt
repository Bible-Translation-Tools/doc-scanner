package org.bibletranslationtools.docscanner.data.repository

import android.app.Application
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import org.bibletranslationtools.docscanner.data.local.DocScanDatabase
import org.bibletranslationtools.docscanner.data.models.Project
import org.bibletranslationtools.docscanner.data.models.ProjectWithData

interface ProjectRepository {
    fun getProjects(): Flow<List<ProjectWithData>>
    suspend fun insert(project: Project): Long
    suspend fun delete(project: Project): Int
    suspend fun update(project: Project): Int
}

class ProjectRepositoryImpl(application: Application) : ProjectRepository {
    private val projectDao = DocScanDatabase.getInstance(application).projectDao

    override fun getProjects() = projectDao.getAllProjects().flowOn(Dispatchers.IO)

    override suspend fun insert(project: Project): Long {
        return projectDao.insert(project)
    }

    override suspend fun delete(project: Project): Int {
        return projectDao.delete(project)
    }

    override suspend fun update(project: Project): Int {
        return projectDao.update(project)
    }
}