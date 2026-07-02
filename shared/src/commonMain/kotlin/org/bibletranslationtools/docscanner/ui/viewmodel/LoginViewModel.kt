package org.bibletranslationtools.docscanner.ui.viewmodel

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import docscanner.composeapp.generated.resources.Res
import docscanner.composeapp.generated.resources.login_failed
import docscanner.composeapp.generated.resources.logging_in
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.bibletranslationtools.docscanner.api.HtrLogin
import org.bibletranslationtools.docscanner.data.models.Alert
import org.bibletranslationtools.docscanner.data.models.Progress
import org.jetbrains.compose.resources.getString

data class LoginState(
    val progress: Progress? = null,
    val alert: Alert? = null
)

sealed class LoginEvent {
    data object Idle : LoginEvent()
    data class Login(val username: String, val password: String) : LoginEvent()
    data object LoginSuccess : LoginEvent()
}

class LoginViewModel(
    private val htrLogin: HtrLogin
) : ScreenModel {

    private var _state = MutableStateFlow(LoginState())
    val state: StateFlow<LoginState> = _state

    private val _event: Channel<LoginEvent> = Channel()
    val event = _event.receiveAsFlow()

    private val logger = KotlinLogging.logger {}

    fun onEvent(event: LoginEvent) {
        when (event) {
            is LoginEvent.Login -> login(event.username, event.password)
            else -> Unit
        }
    }

    private fun login(username: String, password: String) {
        screenModelScope.launch(Dispatchers.Default) {
            updateProgress(Progress(-1f, getString(Res.string.logging_in)))

            val loginError = getString(Res.string.login_failed)

            try {
                val result = htrLogin.execute(username, password)
                if (result.user != null) {
                    updateProgress(null)
                    _event.send(LoginEvent.LoginSuccess)
                    return@launch
                } else {
                    throw IllegalStateException("User is null")
                }
            } catch (e: Exception) {
                logger.error(e) { loginError }
                updateAlert(Alert(loginError) { updateAlert(null) })
            }

            updateProgress(null)
        }
    }

    private fun updateProgress(progress: Progress?) {
        _state.update { it.copy(progress = progress) }
    }

    private fun updateAlert(alert: Alert?) {
        _state.update { it.copy(alert = alert) }
    }
}
