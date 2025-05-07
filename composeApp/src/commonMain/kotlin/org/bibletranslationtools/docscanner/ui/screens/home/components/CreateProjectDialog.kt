package org.bibletranslationtools.docscanner.ui.screens.home.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import docscanner.composeapp.generated.resources.Res
import docscanner.composeapp.generated.resources.cancel
import docscanner.composeapp.generated.resources.create_project
import docscanner.composeapp.generated.resources.ok
import docscanner.composeapp.generated.resources.select_book
import docscanner.composeapp.generated.resources.select_language
import docscanner.composeapp.generated.resources.select_level
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.bibletranslationtools.docscanner.data.models.Book
import org.bibletranslationtools.docscanner.data.models.Language
import org.bibletranslationtools.docscanner.data.models.Level
import org.bibletranslationtools.docscanner.data.models.Project
import org.jetbrains.compose.resources.stringResource

@Composable
fun CreateProjectDialog(
    languages: List<Language>,
    books: List<Book>,
    levels: List<Level>,
    onCreate: (Project) -> Unit,
    onDismissRequest: () -> Unit,
) {
    val language = remember { mutableStateOf<Language?>(null) }
    val book = remember { mutableStateOf<Book?>(null) }
    val level = remember { mutableStateOf<Level?>(null) }

    LaunchedEffect(language.value) {
        println(language.value?.name)
    }

    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            shape = MaterialTheme.shapes.medium
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    stringResource(Res.string.create_project),
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(modifier = Modifier.height(8.dp))
                SearchableComboBox(
                    items = languages,
                    selected = language,
                    properties = listOf(
                        Language::slug,
                        Language::name,
                        Language::angName
                    ),
                    titleProperty = Language::name,
                    subtitleProperty = Language::slug,
                    placeHolderText = stringResource(Res.string.select_language)
                )
                Spacer(modifier = Modifier.height(8.dp))
                SearchableComboBox(
                    items = books,
                    selected = book,
                    properties = listOf(
                        Book::slug,
                        Book::name
                    ),
                    titleProperty = Book::name,
                    subtitleProperty = Book::slug,
                    placeHolderText = stringResource(Res.string.select_book)
                )
                Spacer(modifier = Modifier.height(8.dp))
                SearchableComboBox(
                    items = levels,
                    selected = level,
                    searchEnabled = false,
                    titleProperty = Level::name,
                    subtitleProperty = Level::slug,
                    placeHolderText = stringResource(Res.string.select_level),
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row {
                    Spacer(Modifier.width(0.dp))

                    Button(onClick = onDismissRequest) {
                        Text(stringResource(Res.string.cancel))
                    }
                    Spacer(Modifier.width(6.dp))
                    Button(
                        onClick = {
                            language.value?.let { language ->
                                book.value?.let { book ->
                                    level.value?.let { level ->
                                        val now = Clock.System.now()
                                            .toLocalDateTime(TimeZone.currentSystemDefault())
                                        val project = Project(
                                            language = language,
                                            book = book,
                                            level = level,
                                            created = now.toString(),
                                            modified = now.toString()
                                        )
                                        onCreate(project)
                                        onDismissRequest()
                                    }
                                }
                            }
                        },
                        enabled = language.value != null
                                && book.value != null
                                && level.value != null
                    ) {
                        Text(stringResource(Res.string.ok))
                    }
                }
            }
        }
    }
}