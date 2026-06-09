package org.bibletranslationtools.docscanner.di

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.russhwolf.settings.NSUserDefaultsSettings
import com.russhwolf.settings.Settings
import org.bibletranslationtools.database.MainDatabase
import org.bibletranslationtools.docscanner.IosDirectoryProvider
import org.bibletranslationtools.docscanner.data.DB_NAME
import org.bibletranslationtools.docscanner.data.repository.DirectoryProvider
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import platform.Foundation.NSUserDefaults

actual val platformModule: Module = module {
    singleOf(::IosDirectoryProvider).bind<DirectoryProvider>()
    single<Settings> { NSUserDefaultsSettings(NSUserDefaults.standardUserDefaults) }
    single<SqlDriver> { NativeSqliteDriver(MainDatabase.Schema, DB_NAME) }
}
