package org.bibletranslationtools.docscanner.ui.screens.home

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import docscanner.composeapp.generated.resources.login
import docscanner.composeapp.generated.resources.logout
import docscanner.composeapp.generated.resources.no_project_found
import org.bibletranslationtools.docscanner.ui.common.AlertDialog
import org.bibletranslationtools.docscanner.ui.common.ConfirmDialog
import org.bibletranslationtools.docscanner.ui.common.ErrorScreen
import org.bibletranslationtools.docscanner.ui.common.ExtraAction
import org.bibletranslationtools.docscanner.ui.common.PageType
import org.bibletranslationtools.docscanner.ui.common.ProgressDialog
import org.bibletranslationtools.docscanner.platform.ExitOnBackHandler
import org.bibletranslationtools.docscanner.platform.rememberFileSharer
import org.bibletranslationtools.docscanner.ui.common.TopNavigationBar
import org.bibletranslationtools.docscanner.ui.screens.home.components.CreateProjectDialog
import org.bibletranslationtools.docscanner.ui.screens.home.components.LoginDialog
import org.bibletranslationtools.docscanner.ui.screens.home.components.ProjectLayout
import org.bibletranslationtools.docscanner.ui.screens.project.ProjectScreen
import org.bibletranslationtools.docscanner.ui.viewmodel.HomeEvent
import org.bibletranslationtools.docscanner.ui.viewmodel.HomeViewModel
import org.jetbrains.compose.resources.stringResource

class HomeScreen : Screen {
    @Composable
    override fun Content() {
        val viewModel: HomeViewModel = koinScreenModel()
        val navigator = LocalNavigator.currentOrThrow

        val state by viewModel.state.collectAsStateWithLifecycle()
        val event by viewModel.event.collectAsStateWithLifecycle(HomeEvent.Idle)
        var expandedItemId by remember { mutableStateOf<Long?>(null) }

        var showCreateProjectDialog by remember { mutableStateOf(false) }
        var showLoginDialog by remember { mutableStateOf(false) }

        val fileSharer = rememberFileSharer()

        LaunchedEffect(event) {
            when (event) {
                is HomeEvent.ProjectShared -> {
                    fileSharer.share((event as HomeEvent.ProjectShared).path)
                    viewModel.onEvent(HomeEvent.Idle)
                }
                else -> {}
            }
        }

        ExitOnBackHandler()

        Scaffold(
            topBar = {
                val extraActions = mutableListOf<ExtraAction>()
                extraActions.add(
                    if (state.user != null) {
                        ExtraAction(
                            title = stringResource(Res.string.logout),
                            icon = Icons.AutoMirrored.Filled.Logout,
                            onClick = { viewModel.onEvent(HomeEvent.Logout) }
                        )
                    } else {
                        ExtraAction(
                            title = stringResource(Res.string.login),
                            icon = Icons.AutoMirrored.Filled.Login,
                            onClick = { showLoginDialog = true }
                        )
                    }
                )

                TopNavigationBar(
                    title = stringResource(Res.string.app_name),
                    user = state.user,
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
                                navigator.push(ProjectScreen(project, state.user))
                            },
                            onMoreClick = {
                                expandedItemId = if (expandedItemId != project.id) {
                                    project.id
                                } else null
                            },
                            onShareClick = {
                                viewModel.onEvent(HomeEvent.ShareProject(project))
                            },
                            onDeleteClick = {
                                viewModel.onEvent(HomeEvent.DeleteProject(project))
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
                    onCreate = { viewModel.onEvent(HomeEvent.CreateProject(it)) },
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