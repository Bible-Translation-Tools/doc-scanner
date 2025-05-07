package org.bibletranslationtools.docscanner.di

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import org.bibletranslationtools.database.MainDatabase
import org.bibletranslationtools.docscanner.data.local.DB_NAME
import org.bibletranslationtools.docscanner.data.local.DirectoryProvider
import org.bibletranslationtools.docscanner.data.local.DirectoryProviderImpl
import org.bibletranslationtools.docscanner.data.local.git.CreateRepository
import org.bibletranslationtools.docscanner.data.local.git.GetRepository
import org.bibletranslationtools.docscanner.data.local.git.GogsLogin
import org.bibletranslationtools.docscanner.data.local.git.GogsLogout
import org.bibletranslationtools.docscanner.data.local.git.PushProject
import org.bibletranslationtools.docscanner.data.local.git.RegisterSSHKeys
import org.bibletranslationtools.docscanner.data.local.git.SearchGogsRepositories
import org.bibletranslationtools.docscanner.data.models.Project
import org.bibletranslationtools.docscanner.data.repository.BookRepository
import org.bibletranslationtools.docscanner.data.repository.BookRepositoryImpl
import org.bibletranslationtools.docscanner.data.repository.LanguageRepository
import org.bibletranslationtools.docscanner.data.repository.LanguageRepositoryImpl
import org.bibletranslationtools.docscanner.data.repository.LevelRepository
import org.bibletranslationtools.docscanner.data.repository.LevelRepositoryImpl
import org.bibletranslationtools.docscanner.data.repository.PdfRepository
import org.bibletranslationtools.docscanner.data.repository.PdfRepositoryImpl
import org.bibletranslationtools.docscanner.data.repository.PreferenceRepository
import org.bibletranslationtools.docscanner.data.repository.PreferenceRepositoryImpl
import org.bibletranslationtools.docscanner.data.repository.ProjectRepository
import org.bibletranslationtools.docscanner.data.repository.ProjectRepositoryImpl
import org.bibletranslationtools.docscanner.ui.viewmodel.HomeViewModel
import org.bibletranslationtools.docscanner.ui.viewmodel.ProjectViewModel
import org.bibletranslationtools.docscanner.ui.viewmodel.SplashViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val sharedModule = module {
    single { MainDatabase(get()) }
    singleOf(::DirectoryProviderImpl).bind<DirectoryProvider>()
    singleOf(::PreferenceRepositoryImpl).bind<PreferenceRepository>()
    singleOf(::provideDatabaseDriver)

    // Database repositories
    singleOf(::ProjectRepositoryImpl).bind<ProjectRepository>()
    singleOf(::PdfRepositoryImpl).bind<PdfRepository>()
    singleOf(::LanguageRepositoryImpl).bind<LanguageRepository>()
    singleOf(::BookRepositoryImpl).bind<BookRepository>()
    singleOf(::LevelRepositoryImpl).bind<LevelRepository>()

    // View models
    factoryOf(::SplashViewModel)
    factoryOf(::HomeViewModel)
    factory { (project: Project) -> ProjectViewModel(project, get(), get()) }

    // Git dependencies
    factoryOf(::GetRepository)
    factoryOf(::CreateRepository)
    factoryOf(::SearchGogsRepositories)
    factoryOf(::PushProject)
    factoryOf(::GogsLogin)
    factoryOf(::GogsLogout)
    factoryOf(::RegisterSSHKeys)
}

private fun provideDatabaseDriver(context: Context): SqlDriver {
    return AndroidSqliteDriver(
        MainDatabase.Schema,
        context,
        DB_NAME
    )
}