package org.bibletranslationtools.docscanner.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import docscanner.composeapp.generated.resources.Res
import docscanner.composeapp.generated.resources.ai_settings
import docscanner.composeapp.generated.resources.default_model
import docscanner.composeapp.generated.resources.default_model_subtitle
import docscanner.composeapp.generated.resources.download_languages
import docscanner.composeapp.generated.resources.download_languages_subtitle
import docscanner.composeapp.generated.resources.import_languages
import docscanner.composeapp.generated.resources.import_languages_subtitle
import docscanner.composeapp.generated.resources.languages_section
import docscanner.composeapp.generated.resources.process_immediately
import docscanner.composeapp.generated.resources.process_immediately_subtitle
import docscanner.composeapp.generated.resources.settings
import org.bibletranslationtools.docscanner.api.HtrUser
import org.bibletranslationtools.docscanner.data.repository.DirectoryProvider
import org.bibletranslationtools.docscanner.platform.rememberFilePicker
import org.bibletranslationtools.docscanner.ui.common.AlertDialog
import org.bibletranslationtools.docscanner.ui.common.PageType
import org.bibletranslationtools.docscanner.ui.common.ProgressDialog
import org.bibletranslationtools.docscanner.ui.common.SettingsClickableItem
import org.bibletranslationtools.docscanner.ui.common.SettingsSection
import org.bibletranslationtools.docscanner.ui.common.SettingsToggleItem
import org.bibletranslationtools.docscanner.ui.common.TopNavigationBar
import org.bibletranslationtools.docscanner.ui.screens.settings.components.ModelPickerDialog
import org.bibletranslationtools.docscanner.ui.viewmodel.SettingsEvent
import org.bibletranslationtools.docscanner.ui.viewmodel.SettingsViewModel
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

data class SettingsScreen(private val user: HtrUser?) : Screen {
    @Composable
    override fun Content() {
        val viewModel: SettingsViewModel = koinScreenModel()
        val state by viewModel.state.collectAsStateWithLifecycle()

        var showModelPicker by remember { mutableStateOf(false) }

        val directoryProvider = koinInject<DirectoryProvider>()
        val filePicker = rememberFilePicker(directoryProvider) { path ->
            if (path != null) {
                viewModel.onEvent(SettingsEvent.ImportLanguages(path))
            }
        }

        Scaffold(
            containerColor = MaterialTheme.colorScheme.surface,
            topBar = {
                TopNavigationBar(
                    title = stringResource(Res.string.settings),
                    user = user,
                    page = PageType.SETTINGS
                )
            }
        ) { paddingValue ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValue),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                SettingsSection(
                    title = stringResource(Res.string.ai_settings),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    SettingsClickableItem(
                        icon = Icons.Default.SmartToy,
                        title = stringResource(Res.string.default_model),
                        subtitle = stringResource(Res.string.default_model_subtitle),
                        actionText = state.model.value,
                        onClick = { showModelPicker = true }
                    )
                    SettingsToggleItem(
                        icon = Icons.Default.Bolt,
                        title = stringResource(Res.string.process_immediately),
                        subtitle = stringResource(Res.string.process_immediately_subtitle),
                        checked = state.processImmediately,
                        onCheckedChange = {
                            viewModel.onEvent(SettingsEvent.SetProcessImmediately(it))
                        }
                    )
                }

                HorizontalDivider()

                SettingsSection(title = stringResource(Res.string.languages_section)) {
                    SettingsClickableItem(
                        icon = Icons.Default.Download,
                        title = stringResource(Res.string.download_languages),
                        subtitle = stringResource(Res.string.download_languages_subtitle),
                        onClick = { viewModel.onEvent(SettingsEvent.DownloadLanguages) }
                    )
                    SettingsClickableItem(
                        icon = Icons.Default.UploadFile,
                        title = stringResource(Res.string.import_languages),
                        subtitle = stringResource(Res.string.import_languages_subtitle),
                        onClick = { filePicker.launch() }
                    )
                }
            }

            if (showModelPicker) {
                ModelPickerDialog(
                    selected = state.model,
                    onSelect = { viewModel.onEvent(SettingsEvent.SelectModel(it)) },
                    onDismissRequest = { showModelPicker = false }
                )
            }

            state.progress?.let {
                ProgressDialog(it)
            }

            state.alert?.let {
                AlertDialog(it.message, it.onClosed)
            }
        }
    }
}
