package org.bibletranslationtools.docscanner

import androidx.compose.ui.window.ComposeUIViewController
import org.bibletranslationtools.docscanner.di.initKoin
import platform.UIKit.UIViewController

/** Call once from the iOS app at startup (e.g. in the SwiftUI App initializer). */
fun startKoinForIos() {
    initKoin()
}

/** Root Compose view controller hosted by SwiftUI. */
fun MainViewController(): UIViewController = ComposeUIViewController {
    App()
}
