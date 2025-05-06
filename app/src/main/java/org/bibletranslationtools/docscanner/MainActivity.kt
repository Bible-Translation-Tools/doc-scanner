package org.bibletranslationtools.docscanner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.SlideTransition
import org.bibletranslationtools.docscanner.ui.screens.splash.SplashScreen
import org.bibletranslationtools.docscanner.ui.theme.DocScannerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DocScannerTheme {
                Navigator(SplashScreen()) { navigator ->
                    SlideTransition(navigator)
                }
            }
        }
    }
}
