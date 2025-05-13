package org.bibletranslationtools.docscanner.ui.viewmodel

import android.content.Context
import android.net.Uri
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import docscanner.composeapp.generated.resources.Res
import docscanner.composeapp.generated.resources.confirm_generate_ssh_keys
import docscanner.composeapp.generated.resources.creating_project
import docscanner.composeapp.generated.resources.delete_project_confirm
import docscanner.composeapp.generated.resources.deleting_project
import docscanner.composeapp.generated.resources.loading_projects
import docscanner.composeapp.generated.resources.logged_out
import docscanner.composeapp.generated.resources.logging_in
import docscanner.composeapp.generated.resources.logging_out
import docscanner.composeapp.generated.resources.login_failed
import docscanner.composeapp.generated.resources.login_needed
import docscanner.composeapp.generated.resources.not_authorized
import docscanner.composeapp.generated.resources.push_rejected
import docscanner.composeapp.generated.resources.push_success
import docscanner.composeapp.generated.resources.registering_ssh_keys
import docscanner.composeapp.generated.resources.share_project_failed
import docscanner.composeapp.generated.resources.sharing_project
import docscanner.composeapp.generated.resources.unknown_error
import docscanner.composeapp.generated.resources.uploading_project
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import org.bibletranslationtools.docscanner.api.HtrLogin
import org.bibletranslationtools.docscanner.api.HtrUser
import org.bibletranslationtools.docscanner.api.TranscriberApi
import org.bibletranslationtools.docscanner.api.toGogsUser
import org.bibletranslationtools.docscanner.data.git.PushProject
import org.bibletranslationtools.docscanner.data.git.RegisterSSHKeys
import org.bibletranslationtools.docscanner.data.models.Alert
import org.bibletranslationtools.docscanner.data.models.Book
import org.bibletranslationtools.docscanner.data.models.Language
import org.bibletranslationtools.docscanner.data.models.Level
import org.bibletranslationtools.docscanner.data.models.Progress
import org.bibletranslationtools.docscanner.data.models.Project
import org.bibletranslationtools.docscanner.data.models.getName
import org.bibletranslationtools.docscanner.data.models.getRepo
import org.bibletranslationtools.docscanner.data.repository.BookRepository
import org.bibletranslationtools.docscanner.data.repository.DirectoryProvider
import org.bibletranslationtools.docscanner.data.repository.LanguageRepository
import org.bibletranslationtools.docscanner.data.repository.LevelRepository
import org.bibletranslationtools.docscanner.data.repository.ProjectRepository
import org.bibletranslationtools.docscanner.ui.common.ConfirmAction
import org.bibletranslationtools.docscanner.utils.FileUtils
import org.bibletranslationtools.docscanner.utils.deleteRecursively
import org.jetbrains.compose.resources.getString

data class HomeState(
    val user: HtrUser? = null,
    val projects: List<Project> = emptyList(),
    val project: Project? = null,
    val confirmAction: ConfirmAction? = null,
    val alert: Alert? = null,
    val progress: Progress? = null,
    val languages: List<Language> = emptyList(),
    val books: List<Book> = emptyList(),
    val levels: List<Level> = emptyList()
)

sealed class HomeEvent {
    data object Idle : HomeEvent()
    data class CreateProject(val project: Project): HomeEvent()
    data class UpdateProject(val project: Project?) : HomeEvent()
    data class UploadProject(val project: Project): HomeEvent()
    data class DeleteProject(val project: Project): HomeEvent()
    data class ShareProject(val project: Project, val context: Context): HomeEvent()
    data class ProjectShared(val uri: Uri): HomeEvent()
    data class Login(val username: String, val password: String) : HomeEvent()
    data object LoginRequest : HomeEvent()
    data object Logout : HomeEvent()
}

class HomeViewModel(
    private val projectRepository: ProjectRepository,
    private val directoryProvider: DirectoryProvider,
    private val pushProject: PushProject,
    private val htrLogin: HtrLogin,
    private val registerSSHKeys: RegisterSSHKeys,
    private val languageRepository: LanguageRepository,
    private val bookRepository: BookRepository,
    private val levelRepository: LevelRepository,
    private val transcriberApi: TranscriberApi
) : ScreenModel {

    private var _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state
        .onStart { initialize() }
        .stateIn(
            scope = screenModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = HomeState()
        )

    private val _event: Channel<HomeEvent> = Channel()
    val event = _event.receiveAsFlow()

    private fun initialize() {
        screenModelScope.launch(Dispatchers.IO) {
            updateProgress(Progress(-1f, getString(Res.string.loading_projects)))

            updateUser(transcriberApi.getUser())

            updateLanguages(languageRepository.getAll())
            updateBooks(bookRepository.getAll())
            updateLevels(levelRepository.getAll())

            loadProjects()

            updateProgress(null)
        }
    }

    private fun loadProjects() {
        updateProjects(projectRepository.getAll())
    }

    fun onEvent(event: HomeEvent) {
        when (event) {
            is HomeEvent.CreateProject -> createProject(event.project)
            is HomeEvent.UploadProject -> uploadProject(event.project)
            is HomeEvent.UpdateProject -> updateProject(event.project)
            is HomeEvent.DeleteProject -> deleteProject(event.project)
            is HomeEvent.ShareProject -> shareProject(event.project, event.context)
            is HomeEvent.Login -> login(event.username, event.password)
            is HomeEvent.LoginRequest -> requestLogin()
            is HomeEvent.Logout -> logout()
            else -> resetChannel()
        }
    }

    private fun createProject(project: Project) {
        screenModelScope.launch(Dispatchers.IO) {
            updateProgress(Progress(-1f, getString(Res.string.creating_project)))

            // initialize git repo
            project.getRepo(directoryProvider)
            projectRepository.insert(project)

            updateProgress(Progress(-1f, getString(Res.string.loading_projects)))
            loadProjects()
            updateProgress(null)
        }
    }

    private fun shareProject(project: Project, context: Context) {
        screenModelScope.launch(Dispatchers.IO) {
            updateProgress(Progress(-1f, getString(Res.string.sharing_project)))

            try {
                val zip = FileUtils.zipProject(project, directoryProvider)
                val fileUri = FileUtils.getPathUri(context, zip)
                _event.send(HomeEvent.ProjectShared(fileUri))
            } catch (e: Exception) {
                e.printStackTrace()
                updateAlert(
                    Alert(getString(Res.string.share_project_failed)) {
                        updateAlert(null)
                    }
                )
            }

            updateProgress(null)
        }
    }

    private fun deleteProject(project: Project) {
        screenModelScope.launch {
            updateConfirmAction(
                ConfirmAction(
                    message = getString(Res.string.delete_project_confirm),
                    onConfirm = {
                        screenModelScope.launch {
                            updateProgress(Progress(-1f, getString(Res.string.deleting_project)))
                            doDeleteProject(project)

                            updateProgress(Progress(-1f, getString(Res.string.loading_projects)))
                            loadProjects()

                            updateProgress(null)
                        }
                    },
                    onCancel = {
                        updateConfirmAction(null)
                    }
                )
            )
        }
    }

    private suspend fun doDeleteProject(project: Project) {
        val deleted = try {
            SystemFileSystem.deleteRecursively(
                Path(directoryProvider.projectsDir, project.getName())
            )
            true
        } catch (e: Exception) {
            false
        }

        if (deleted) {
            projectRepository.delete(project)
        }
    }

    private fun uploadProject(project: Project) {
        screenModelScope.launch(Dispatchers.IO) {
            _state.value.user?.let {
                doUploadProject(project, it)
            } ?: run {
                updateAlert(
                    Alert(getString(Res.string.not_authorized)) {
                        updateAlert(null)
                    }
                )
            }

            updateProgress(null)
        }
    }

    private suspend fun doUploadProject(
        project: Project,
        user: HtrUser,
    ) {
        updateProgress(Progress(-1f, getString(Res.string.uploading_project)))

        val repo = project.getRepo(directoryProvider)
        repo.setAuthor(user.wacsUsername, user.wacsUserEmail)
        repo.commit()

        val result = pushProject.execute(project, user.toGogsUser())
        when {
            result.status == PushProject.Status.OK -> {
                updateAlert(
                    Alert(getString(Res.string.push_success)) {
                        updateAlert(null)
                    }
                )
            }
            result.status == PushProject.Status.AUTH_FAILURE -> {
                updateConfirmAction(
                    ConfirmAction(
                        message = getString(Res.string.confirm_generate_ssh_keys),
                        onConfirm = {
                            screenModelScope.launch {
                                doRegisterSshKeys(project)
                            }
                        },
                        onCancel = {
                            updateConfirmAction(null)
                        }
                    )
                )
            }
            result.status.isRejected -> {
                updateAlert(
                    Alert(getString(Res.string.push_rejected)) {
                        updateAlert(null)
                    }
                )
            }
            else -> {
                updateAlert(
                    Alert(getString(Res.string.unknown_error)) {
                        updateAlert(null)
                    }
                )
            }
        }
    }

    private suspend fun doRegisterSshKeys(project: Project? = null) {
        updateProgress(Progress(-1f, getString(Res.string.registering_ssh_keys)))

        val registered = _state.value.user?.let {
            registerSSHKeys.execute(true, it.toGogsUser())
        } == true

        if (registered) {
            project?.let { proj ->
                _state.value.user?.let { user ->
                    doUploadProject(proj, user)
                }
            }
        } else {
            updateProgress(null)
            updateAlert(
                Alert(getString(Res.string.login_failed)) {
                    updateAlert(null)
                }
            )
        }
    }

    private fun login(username: String, password: String) {
        screenModelScope.launch(Dispatchers.IO) {
            updateProgress(Progress(-1f, getString(Res.string.logging_in)))

            val result = htrLogin.execute(username, password)

            result.user?.let { user ->
                updateUser(user)
                doRegisterSshKeys()

                _state.value.project?.let { project ->
                    _state.value.user?.let { user ->
                        doUploadProject(project, user)
                    }
                }
            } ?: run {
                updateAlert(
                    Alert(getString(Res.string.login_failed)) {
                        updateAlert(null)
                    }
                )
            }

            updateProgress(null)
        }
    }

    private fun requestLogin() {
        screenModelScope.launch {
            updateAlert(
                Alert(getString(Res.string.login_needed)) {
                    updateAlert(null)
                }
            )
        }
    }

    private fun logout() {
        screenModelScope.launch(Dispatchers.IO) {
            updateProgress(Progress(-1f, getString(Res.string.logging_out)))

            _state.value.user?.let {
                transcriberApi.logout()
                SystemFileSystem.deleteRecursively(directoryProvider.sshKeysDir)

                updateUser(null)

                updateAlert(
                    Alert(getString(Res.string.logged_out)) {
                        updateAlert(null)
                    }
                )
            }

            updateProgress(null)
        }
    }

    private fun updateProjects(projects: List<Project>) {
        _state.update {
            it.copy(projects = projects)
        }
    }

    private fun updateLanguages(languages: List<Language>) {
        _state.update {
            it.copy(languages = languages)
        }
    }

    private fun updateBooks(books: List<Book>) {
        _state.update {
            it.copy(books = books)
        }
    }

    private fun updateLevels(levels: List<Level>) {
        _state.update {
            it.copy(levels = levels)
        }
    }

    private fun updateUser(user: HtrUser?) {
        _state.update {
            it.copy(user = user)
        }
    }

    private fun updateConfirmAction(confirmAction: ConfirmAction?) {
        _state.update {
            it.copy(confirmAction = confirmAction)
        }
    }

    private fun updateProgress(progress: Progress?) {
        _state.update {
            it.copy(progress = progress)
        }
    }

    private fun updateAlert(alert: Alert?) {
        _state.update {
            it.copy(alert = alert)
        }
    }

    private fun updateProject(project: Project?) {
        _state.update {
            it.copy(project = project)
        }
    }

    private fun resetChannel() {
        screenModelScope.launch {
            _event.send(HomeEvent.Idle)
        }
    }
}