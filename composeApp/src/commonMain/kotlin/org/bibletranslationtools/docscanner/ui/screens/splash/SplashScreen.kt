package org.bibletranslationtools.docscanner.ui.screens.splash

import android.annotation.SuppressLint
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import org.bibletranslationtools.docscanner.R
import org.bibletranslationtools.docscanner.ui.common.AlertDialog
import org.bibletranslationtools.docscanner.ui.common.PageType
import org.bibletranslationtools.docscanner.ui.common.ProgressDialog
import org.bibletranslationtools.docscanner.ui.common.TopNavigationBar
import org.bibletranslationtools.docscanner.ui.screens.home.HomeScreen
import org.bibletranslationtools.docscanner.ui.viewmodel.HomeEvent
import org.bibletranslationtools.docscanner.ui.viewmodel.SplashEvent
import org.bibletranslationtools.docscanner.ui.viewmodel.SplashViewModel

class SplashScreen : Screen {
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @Composable
    override fun Content() {
        val viewModel: SplashViewModel = koinScreenModel()
        val navigator = LocalNavigator.currentOrThrow

        val state by viewModel.state.collectAsStateWithLifecycle()
        val event by viewModel.event.collectAsStateWithLifecycle(HomeEvent.Idle)

        LaunchedEffect(event) {
            when (event) {
                is SplashEvent.InitializationComplete -> {
                    navigator.push(HomeScreen())
                }
                else -> Unit
            }
        }

        Scaffold(
            topBar = {
                TopNavigationBar(
                    title = stringResource(R.string.app_name),
                    user = null,
                    page = PageType.SPLASH
                )
            }
        ) {
            state.progress?.let {
                ProgressDialog(it)
            }

            state.alert?.let {
                AlertDialog(it.message, it.onClosed)
            }
        }
    }
}