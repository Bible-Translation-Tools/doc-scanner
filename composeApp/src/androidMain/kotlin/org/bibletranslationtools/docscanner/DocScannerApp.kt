package org.bibletranslationtools.docscanner

import android.app.Application
import org.bibletranslationtools.docscanner.di.initKoin
import org.koin.android.ext.koin.androidContext

class DocScannerApp : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidContext(applicationContext)
        }
    }
}