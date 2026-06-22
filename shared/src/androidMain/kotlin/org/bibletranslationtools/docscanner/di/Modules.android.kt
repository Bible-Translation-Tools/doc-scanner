package org.bibletranslationtools.docscanner.di

import android.content.Context
import androidx.preference.PreferenceManager
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings
import org.bibletranslationtools.database.MainDatabase
import org.bibletranslationtools.docscanner.AndroidDirectoryProvider
import org.bibletranslationtools.docscanner.data.DB_NAME
import org.bibletranslationtools.docscanner.data.repository.DirectoryProvider
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

actual val platformModule: Module = module {
    singleOf(::AndroidDirectoryProvider).bind<DirectoryProvider>()
    single<Settings> { provideSettings(get()) }
    singleOf(::provideDatabaseDriver)
}

private fun provideSettings(context: Context): Settings {
    return SharedPreferencesSettings(
        PreferenceManager.getDefaultSharedPreferences(context)
    )
}

private fun provideDatabaseDriver(context: Context): SqlDriver {
    return AndroidSqliteDriver(
        MainDatabase.Schema,
        context,
        DB_NAME
    )
}
