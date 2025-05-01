package org.bibletranslationtools.docscanner.di

import org.bibletranslationtools.docscanner.data.local.DirectoryProvider
import org.bibletranslationtools.docscanner.data.local.DirectoryProviderImpl
import org.bibletranslationtools.docscanner.data.repository.PdfRepository
import org.bibletranslationtools.docscanner.data.repository.PdfRepositoryImpl
import org.bibletranslationtools.docscanner.ui.viewmodel.PdfViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val sharedModule = module {
    singleOf(::DirectoryProviderImpl).bind<DirectoryProvider>()
    singleOf(::PdfRepositoryImpl).bind<PdfRepository>()
    factoryOf(::PdfViewModel)
}