package org.bibletranslationtools.docscanner

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.SlideTransition
import org.bibletranslationtools.docscanner.ui.screens.splash.SplashScreen
import org.bibletranslationtools.docscanner.ui.theme.DocScannerTheme

@Composable
fun App() {
    DocScannerTheme {
        Navigator(SplashScreen()) { navigator ->
            SlideTransition(navigator)
        }
    }
}