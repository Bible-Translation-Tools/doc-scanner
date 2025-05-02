package org.bibletranslationtools.docscanner.ui.screens.home

import android.content.Context
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.bibletranslationtools.docscanner.R
import org.bibletranslationtools.docscanner.data.local.DirectoryProvider
import org.bibletranslationtools.docscanner.data.local.git.GogsLogin
import org.bibletranslationtools.docscanner.data.local.git.GogsLogout
import org.bibletranslationtools.docscanner.data.local.git.Profile
import org.bibletranslationtools.docscanner.data.local.git.PushProject
import org.bibletranslationtools.docscanner.data.local.git.RegisterSSHKeys
import org.bibletranslationtools.docscanner.data.models.Alert
import org.bibletranslationtools.docscanner.data.models.Progress
import org.bibletranslationtools.docscanner.data.models.Project
import org.bibletranslationtools.docscanner.data.models.getName
import org.bibletranslationtools.docscanner.data.models.getRepo
import org.bibletranslationtools.docscanner.data.repository.PreferenceRepository
import org.bibletranslationtools.docscanner.data.repository.ProjectRepository
import org.bibletranslationtools.docscanner.data.repository.getPref
import org.bibletranslationtools.docscanner.ui.screens.common.ConfirmAction
import org.bibletranslationtools.docscanner.utils.FileUtilities
import org.json.JSONObject

data class HomeState(
    val profile: Profile? = null,
    val projects: List<Project> = emptyList(),
    val project: Project? = null,
    val confirmAction: ConfirmAction? = null,
    val alert: Alert? = null,
    val progress: Progress? = null
)

sealed class HomeEvent {
    data object Idle : HomeEvent()
    data class UpdateProject(val project: Project?) : HomeEvent()
}

class HomeViewModel(
    private val context: Context,
    private val projectRepository: ProjectRepository,
    private val preferenceRepository: PreferenceRepository,
    private val directoryProvider: DirectoryProvider,
    private val pushProject: PushProject,
    private val gogsLogin: GogsLogin,
    private val gogsLogout: GogsLogout,
    private val registerSSHKeys: RegisterSSHKeys
) : ScreenModel {

    private var _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state
        .stateIn(
            scope = screenModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = HomeState()
        )

    private val _event: Channel<HomeEvent> = Channel()
    val event = _event.receiveAsFlow()

    init {
        screenModelScope.launch(Dispatchers.IO) {
            preferenceRepository.getPref<String>("profile")?.let {
                updateProfile(
                    Profile.fromJSON(
                        preferenceRepository,
                        directoryProvider,
                        JSONObject(it)
                    )
                )
            }

            projectRepository.getProjects().catch {
                it.printStackTrace()
            }.collect {
                updateProjects(it)
            }
        }
    }

    fun onEvent(event: HomeEvent) {
        when (event) {
            is HomeEvent.UpdateProject -> updateProject(event.project)
            else -> resetChannel()
        }
    }

    fun createProject(project: Project) {
        screenModelScope.launch(Dispatchers.IO) {
            // initialize git repo
            project.getRepo(directoryProvider)
            projectRepository.insert(project)
        }
    }

    fun deleteProject(project: Project) {
        screenModelScope.launch(Dispatchers.IO) {
            if (
                FileUtilities.deleteProject(
                    directoryProvider,
                    project.getName()
                )
            ) {
                projectRepository.delete(project)
            }
        }
    }

    fun uploadProject(project: Project) {
        screenModelScope.launch(Dispatchers.IO) {
            _state.value.profile?.let {
                doUploadProject(project, it)
            } ?: run {
                updateAlert(
                    Alert(context.getString(R.string.not_authorized)) {
                        updateAlert(null)
                    }
                )
            }

            updateProgress(null)
        }
    }

    private fun doUploadProject(
        project: Project,
        profile: Profile
    ) {
        updateProgress(Progress(-1f, context.getString(R.string.uploading_project)))
        val result = pushProject.execute(project, profile)
        when {
            result.status == PushProject.Status.OK -> {
                updateAlert(
                    Alert(context.getString(R.string.push_success)) {
                        updateAlert(null)
                    }
                )
            }
            result.status == PushProject.Status.AUTH_FAILURE -> {
                updateConfirmAction(
                    ConfirmAction(
                        message = context.getString(R.string.confirm_generate_ssh_keys),
                        onConfirm = {
                            doRegisterSshKeys(project)
                        },
                        onCancel = {
                            updateConfirmAction(null)
                        }
                    )
                )
            }
            result.status.isRejected -> {
                updateAlert(
                    Alert(context.getString(R.string.push_rejected)) {
                        updateAlert(null)
                    }
                )
            }
            else -> {
                updateAlert(
                    Alert(context.getString(R.string.unknown_error)) {
                        updateAlert(null)
                    }
                )
            }
        }
    }

    private fun doRegisterSshKeys(project: Project? = null) {
        updateProgress(Progress(-1f, context.getString(R.string.registering_ssh_keys)))

        val registered = _state.value.profile?.let {
            registerSSHKeys.execute(true, it)
        } == true

        if (registered) {
            project?.let { proj ->
                _state.value.profile?.let { profile ->
                    doUploadProject(proj, profile)
                }
            }
        } else {
            updateProgress(null)
            updateAlert(
                Alert(context.getString(R.string.login_failed)) {
                    updateAlert(null)
                }
            )
        }
    }

    fun login(username: String, password: String) {
        updateProgress(Progress(-1f, context.getString(R.string.logging_in)))

        screenModelScope.launch(Dispatchers.IO) {
            val result = gogsLogin.execute(username, password)
            result.user?.let { user ->
                val profileString = preferenceRepository.getPref<String>("profile")
                updateProfile(
                    Profile.fromJSON(
                        preferenceRepository,
                        directoryProvider,
                        profileString?.let { JSONObject(it) }
                    ).also {
                        it.login(result.user.fullName, user)
                    }
                )
                doRegisterSshKeys()

                _state.value.project?.let { project ->
                    _state.value.profile?.let { profile ->
                        doUploadProject(project, profile)
                    }
                }
            }

            updateProgress(null)
        }
    }

    fun logout() {
        updateProgress(Progress(-1f, context.getString(R.string.logging_out)))

        screenModelScope.launch(Dispatchers.IO) {
            _state.value.profile?.let {
                gogsLogout.execute(it)
                it.logout()
                updateProfile(null)

                updateAlert(
                    Alert(context.getString(R.string.logged_out)) {
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

    private fun updateProfile(profile: Profile?) {
        _state.update {
            it.copy(profile = profile)
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