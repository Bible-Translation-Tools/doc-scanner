package org.bibletranslationtools.docscanner.ui.viewmodel

import android.content.Context
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
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
import org.bibletranslationtools.docscanner.R
import org.bibletranslationtools.docscanner.data.local.JsonLenient
import org.bibletranslationtools.docscanner.data.local.Settings
import org.bibletranslationtools.docscanner.data.models.Alert
import org.bibletranslationtools.docscanner.data.models.Book
import org.bibletranslationtools.docscanner.data.models.Language
import org.bibletranslationtools.docscanner.data.models.Level
import org.bibletranslationtools.docscanner.data.models.Progress
import org.bibletranslationtools.docscanner.data.repository.BookRepository
import org.bibletranslationtools.docscanner.data.repository.LanguageRepository
import org.bibletranslationtools.docscanner.data.repository.LevelRepository
import org.bibletranslationtools.docscanner.data.repository.PreferenceRepository
import org.bibletranslationtools.docscanner.data.repository.getPref
import org.bibletranslationtools.docscanner.data.repository.setPref

data class SplashState(
    val alert: Alert? = null,
    val progress: Progress? = null
)

sealed class SplashEvent {
    data object InitializationComplete : SplashEvent()
}

class SplashViewModel(
    private val context: Context,
    private val preferenceRepository: PreferenceRepository,
    private val languageRepository: LanguageRepository,
    private val bookRepository: BookRepository,
    private val levelRepository: LevelRepository
) : ScreenModel {

    private var _state = MutableStateFlow(SplashState())
    val state: StateFlow<SplashState> = _state
        .onStart { initializeApp() }
        .stateIn(
            scope = screenModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SplashState()
        )

    private val _event: Channel<SplashEvent> = Channel()
    val event = _event.receiveAsFlow()

    private fun initializeApp() {
        screenModelScope.launch(Dispatchers.IO) {
            val initialized = preferenceRepository.getPref(Settings.KEY_PREF_INITIALIZED, false)

            if (!initialized) {
                updateProgress(Progress(-1f, context.getString(R.string.initializing_languages)))
                try {
                    languageRepository.deleteAll()
                    context.assets.open("langnames.json").use {
                        val jsonString = it.bufferedReader().use { it.readText() }
                        val languages = JsonLenient.decodeFromString<List<Language>>(jsonString)
                        languages.forEach {
                            languageRepository.insert(it)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    updateAlert(
                        Alert(context.getString(R.string.initialization_failed)) {
                            updateAlert(null)
                        }
                    )
                    updateProgress(null)
                    return@launch
                }

                updateProgress(Progress(-1f, context.getString(R.string.initializing_books)))
                try {
                    bookRepository.deleteAll()
                    context.assets.open("books.json").use {
                        val jsonString = it.bufferedReader().use { it.readText() }
                        val books = JsonLenient.decodeFromString<List<Book>>(jsonString)
                        books.forEach {
                            bookRepository.insert(it)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    updateAlert(
                        Alert(context.getString(R.string.initialization_failed)) {
                            updateAlert(null)
                        }
                    )
                    updateProgress(null)
                    return@launch
                }

                updateProgress(Progress(-1f, context.getString(R.string.initializing_levels)))
                try {
                    levelRepository.deleteAll()
                    context.assets.open("levels.json").use {
                        val jsonString = it.bufferedReader().use { it.readText() }
                        val levels = JsonLenient.decodeFromString<List<Level>>(jsonString)
                        levels.forEach {
                            levelRepository.insert(it)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    updateAlert(
                        Alert(context.getString(R.string.initialization_failed)) {
                            updateAlert(null)
                        }
                    )
                    updateProgress(null)
                    return@launch
                }

                preferenceRepository.setPref(Settings.KEY_PREF_INITIALIZED, true)
                _event.send(SplashEvent.InitializationComplete)
            } else {
                _event.send(SplashEvent.InitializationComplete)
            }
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
}