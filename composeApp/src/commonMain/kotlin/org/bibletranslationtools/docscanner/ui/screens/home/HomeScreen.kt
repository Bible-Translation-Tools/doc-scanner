package org.bibletranslationtools.docscanner.ui.screens.home

import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
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
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import docscanner.composeapp.generated.resources.Res
import docscanner.composeapp.generated.resources.app_name
import docscanner.composeapp.generated.resources.logout
import docscanner.composeapp.generated.resources.no_project_found
import org.bibletranslationtools.docscanner.ui.common.AlertDialog
import org.bibletranslationtools.docscanner.ui.common.ConfirmDialog
import org.bibletranslationtools.docscanner.ui.common.ErrorScreen
import org.bibletranslationtools.docscanner.ui.common.ExtraAction
import org.bibletranslationtools.docscanner.ui.common.PageType
import org.bibletranslationtools.docscanner.ui.common.ProgressDialog
import org.bibletranslationtools.docscanner.ui.common.TopNavigationBar
import org.bibletranslationtools.docscanner.ui.screens.home.components.CreateProjectDialog
import org.bibletranslationtools.docscanner.ui.screens.home.components.LoginDialog
import org.bibletranslationtools.docscanner.ui.screens.home.components.ProjectLayout
import org.bibletranslationtools.docscanner.ui.screens.project.ProjectScreen
import org.bibletranslationtools.docscanner.ui.viewmodel.HomeEvent
import org.bibletranslationtools.docscanner.ui.viewmodel.HomeViewModel
import org.jetbrains.compose.resources.stringResource
import kotlin.system.exitProcess

class HomeScreen : Screen {
    @Composable
    override fun Content() {
        val viewModel: HomeViewModel = koinScreenModel()
        val navigator = LocalNavigator.currentOrThrow

        val state by viewModel.state.collectAsStateWithLifecycle()
        var expandedItemId by remember { mutableStateOf<Int?>(null) }

        var showCreateProjectDialog by remember { mutableStateOf(false) }
        var showLoginDialog by remember { mutableStateOf(false) }

        val activity = LocalActivity.current

        BackHandler {
            activity?.finishAffinity()
            exitProcess(0)
        }

        Scaffold(
            topBar = {
                val extraActions = mutableListOf<ExtraAction>()
                if (state.profile != null) {
                    extraActions.add(
                        ExtraAction(
                            title = stringResource(Res.string.logout),
                            icon = Icons.AutoMirrored.Filled.Logout,
                            onClick = { viewModel.onEvent(HomeEvent.Logout) }
                        )
                    )
                }
                TopNavigationBar(
                    title = stringResource(Res.string.app_name),
                    profile = state.profile,
                    page = PageType.HOME,
                    extraAction = extraActions.toTypedArray()
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    modifier = Modifier.offset(0.dp, 0.dp),
                    onClick = {
                        showCreateProjectDialog = true
                    },
                    content = {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Create"
                        )
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                )
            }
        ) { paddingValue ->

            if (state.projects.isEmpty()) {
                ErrorScreen(message = stringResource(Res.string.no_project_found))
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValue)
                ) {
                    items(items = state.projects, key = { it.id }) { project ->
                        ProjectLayout(
                            project = project,
                            menuShown = expandedItemId == project.id,
                            onCardClick = {
                                navigator.push(ProjectScreen(project))
                            },
                            onMoreClick = {
                                expandedItemId = if (expandedItemId != project.id) {
                                    project.id
                                } else null
                            },
                            onUploadClick = {
                                if (state.profile != null) {
                                    viewModel.uploadProject(project)
                                } else {
                                    viewModel.onEvent(HomeEvent.UpdateProject(project))
                                    showLoginDialog = true
                                }
                            },
                            onShareClick = {
//                                val fileUri = FileUtilities.getFileUri(
//                                    context,
//                                    directoryProvider,
//                                    pdfEntity.name
//                                )
//                                val shareIntent = Intent(Intent.ACTION_SEND)
//                                shareIntent.type = "application/pdf"
//                                shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri)
//                                shareIntent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
//                                context.startActivity(
//                                    Intent.createChooser(shareIntent, "share")
//                                )
                            },
                            onDeleteClick = {
                                viewModel.deleteProject(project)
                            },
                            onDismissRequest = { expandedItemId = null }
                        )
                    }
                }
            }

            if (showCreateProjectDialog) {
                CreateProjectDialog(
                    languages = state.languages,
                    books = state.books,
                    levels = state.levels,
                    onCreate = { viewModel.createProject(it) },
                    onDismissRequest = { showCreateProjectDialog = false }
                )
            }

            if (showLoginDialog) {
                LoginDialog(
                    onLogin = { username, password ->
                        viewModel.onEvent(HomeEvent.Login(username, password))
                    },
                    onDismissRequest = { showLoginDialog = false }
                )
            }

            state.confirmAction?.let {
                ConfirmDialog(
                    message = it.message,
                    onConfirm = it.onConfirm,
                    onCancel = it.onCancel,
                    onDismiss = it.onCancel
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