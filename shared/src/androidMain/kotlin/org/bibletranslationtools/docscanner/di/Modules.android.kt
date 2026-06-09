package org.bibletranslationtools.docscanner.di

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import org.bibletranslationtools.database.MainDatabase
import org.bibletranslationtools.docscanner.AndroidDirectoryProvider
import org.bibletranslationtools.docscanner.AndroidPreferenceRepository
import org.bibletranslationtools.docscanner.data.DB_NAME
import org.bibletranslationtools.docscanner.data.repository.DirectoryProvider
import org.bibletranslationtools.docscanner.data.repository.PreferenceRepository
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

actual val platformModule: Module = module {
    singleOf(::AndroidDirectoryProvider).bind<DirectoryProvider>()
    singleOf(::AndroidPreferenceRepository).bind<PreferenceRepository>()
    singleOf(::provideDatabaseDriver)
}

private fun provideDatabaseDriver(context: Context): SqlDriver {
    return AndroidSqliteDriver(
        MainDatabase.Schema,
        context,
        DB_NAME
    )
}
