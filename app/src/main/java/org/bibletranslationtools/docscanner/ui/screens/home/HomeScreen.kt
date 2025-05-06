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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import org.bibletranslationtools.docscanner.R
import org.bibletranslationtools.docscanner.ui.screens.common.AlertDialog
import org.bibletranslationtools.docscanner.ui.screens.common.ConfirmDialog
import org.bibletranslationtools.docscanner.ui.screens.common.ErrorScreen
import org.bibletranslationtools.docscanner.ui.screens.common.ExtraAction
import org.bibletranslationtools.docscanner.ui.screens.common.PageType
import org.bibletranslationtools.docscanner.ui.screens.common.ProgressDialog
import org.bibletranslationtools.docscanner.ui.screens.common.TopNavigationBar
import org.bibletranslationtools.docscanner.ui.screens.home.components.CreateProjectDialog
import org.bibletranslationtools.docscanner.ui.screens.home.components.LoginDialog
import org.bibletranslationtools.docscanner.ui.screens.home.components.ProjectLayout
import org.bibletranslationtools.docscanner.ui.screens.project.ProjectScreen
import org.bibletranslationtools.docscanner.ui.viewmodel.HomeEvent
import org.bibletranslationtools.docscanner.ui.viewmodel.HomeViewModel
import kotlin.system.exitProcess

class HomeScreen : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val viewModel: HomeViewModel = koinScreenModel()
        val navigator = LocalNavigator.currentOrThrow

        val state by viewModel.state.collectAsStateWithLifecycle()
        var expandedItemId by remember { mutableStateOf<String?>(null) }

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
                            title = stringResource(R.string.logout),
                            icon = Icons.AutoMirrored.Filled.Logout,
                            onClick = viewModel::logout
                        )
                    )
                }
                TopNavigationBar(
                    title = stringResource(R.string.app_name),
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
                    }, content = {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Create"
                        )
                    })
            }
        ) { paddingValue ->

            if (state.projects.isEmpty()) {
                ErrorScreen(message = stringResource(R.string.no_project_found))
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValue)
                ) {
                    items(items = state.projects, key = { it.project.id }) { project ->
                        ProjectLayout(
                            project = project,
                            menuShown = expandedItemId == project.project.id,
                            onCardClick = {
                                navigator.push(ProjectScreen(project))
                            },
                            onMoreClick = {
                                expandedItemId = if (expandedItemId != project.project.id) {
                                    project.project.id
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
                        viewModel.login(username, password)
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