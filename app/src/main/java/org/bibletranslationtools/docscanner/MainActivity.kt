package org.bibletranslationtools.docscanner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.viewModelFactory
import org.bibletranslationtools.docscanner.ui.screens.home.HomeScreen
import org.bibletranslationtools.docscanner.ui.theme.DocScannerTheme
import org.bibletranslationtools.docscanner.ui.viewmodel.PdfViewModel

class MainActivity : ComponentActivity() {
    private val pdfViewModel by viewModels<PdfViewModel> {
        viewModelFactory {
            addInitializer(PdfViewModel::class) {
                PdfViewModel(application)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DocScannerTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    HomeScreen(pdfViewModel)
                }
            }
        }
    }
}
