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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.kanyidev.searchable_dropdown.LargeSearchableDropdownMenu
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
    onDismissRequest: () -> Unit
) {
    var language by remember { mutableStateOf<Language?>(null) }
    var book by remember { mutableStateOf<Book?>(null) }
    var level by remember { mutableStateOf<Level?>(null) }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.padding(20.dp),
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
                LargeSearchableDropdownMenu(
                    options = languages,
                    selectedOption = language,
                    onItemSelected = { language = it },
                    placeholder = stringResource(Res.string.select_language),
                    textStyle = MaterialTheme.typography.bodyMedium,
                    fieldLabelTextStyle = MaterialTheme.typography.bodyMedium,
                    placeholderTextStyle = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                LargeSearchableDropdownMenu(
                    options = books,
                    selectedOption = book,
                    onItemSelected = { book = it },
                    placeholder = stringResource(Res.string.select_book),
                    textStyle = MaterialTheme.typography.bodyMedium,
                    fieldLabelTextStyle = MaterialTheme.typography.bodyMedium,
                    placeholderTextStyle = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                LargeSearchableDropdownMenu(
                    options = levels,
                    selectedOption = level,
                    onItemSelected = { level = it },
                    placeholder = stringResource(Res.string.select_level),
                    textStyle = MaterialTheme.typography.bodyMedium,
                    fieldLabelTextStyle = MaterialTheme.typography.bodyMedium,
                    placeholderTextStyle = MaterialTheme.typography.bodyMedium
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
                            language?.let { ln ->
                                book?.let { bk ->
                                    level?.let { lv ->
                                        val now = Clock.System.now()
                                            .toLocalDateTime(TimeZone.currentSystemDefault())
                                        val project = Project(
                                            language = ln,
                                            book = bk,
                                            level = lv,
                                            created = now.toString(),
                                            modified = now.toString()
                                        )
                                        onCreate(project)
                                        onDismissRequest()
                                    }
                                }
                            }
                        },
                        enabled = language != null && book != null && level != null
                    ) {
                        Text(stringResource(Res.string.ok))
                    }
                }
            }
        }
    }
}