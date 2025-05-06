package org.bibletranslationtools.docscanner.ui.screens.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import org.bibletranslationtools.docscanner.R
import org.bibletranslationtools.docscanner.data.models.Book
import org.bibletranslationtools.docscanner.data.models.Language
import org.bibletranslationtools.docscanner.data.models.Level
import org.bibletranslationtools.docscanner.data.models.Project
import org.bibletranslationtools.docscanner.data.models.ProjectWithData
import org.example.dropdown.data.DefaultDropdownItem
import org.example.dropdown.data.DropdownConfig
import org.example.dropdown.data.search.SearchSettings
import org.example.dropdown.data.selection.SingleItemContentConfig
import org.example.project.ui.SearchableDropdown
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateProjectDialog(
    languages: List<Language>,
    books: List<Book>,
    levels: List<Level>,
    onCreate: (ProjectWithData) -> Unit,
    onDismissRequest: () -> Unit,
) {
    var language = remember { mutableStateOf<Language?>(null) }
    var book = remember { mutableStateOf<Book?>(null) }
    var level = remember { mutableStateOf<Level?>(null) }

    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            shape = MaterialTheme.shapes.medium
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    stringResource(R.string.create_project),
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(modifier = Modifier.height(8.dp))
                SearchableDropdown(
                    items = languages,
                    searchSettings = SearchSettings(
                        searchProperties = listOf(
                            Language::slug,
                            Language::name,
                            Language::angName
                        ),
                    ),
                    dropdownConfig = DropdownConfig(
                        horizontalPadding = 8.dp,
                        shape = RoundedCornerShape(8.dp),
                        headerPlaceholder = { Text(stringResource(R.string.select_language)) },
                        headerBackgroundColor = MaterialTheme.colorScheme.background,
                        contentBackgroundColor = MaterialTheme.colorScheme.background
                    ),
                    itemContentConfig = SingleItemContentConfig.Custom(
                        content = { language, _ ->
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                                    .height(32.dp)
                            ) {
                                Text(
                                    text = language.name,
                                    style = MaterialTheme.typography.titleLarge
                                )
                                Text(
                                    text = language.slug,
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        }
                    ),
                    selectedItem = language
                )
                Spacer(modifier = Modifier.height(8.dp))
                SearchableDropdown(
                    items = books,
                    searchSettings = SearchSettings(
                        searchProperties = listOf(
                            Book::slug,
                            Book::name
                        )
                    ),
                    dropdownConfig = DropdownConfig(
                        horizontalPadding = 8.dp,
                        shape = RoundedCornerShape(8.dp),
                        headerPlaceholder = { Text(stringResource(R.string.select_book)) },
                        headerBackgroundColor = MaterialTheme.colorScheme.background
                    ),
                    itemContentConfig = SingleItemContentConfig.Default(
                        defaultItem = DefaultDropdownItem(
                            title = Book::name,
                            subtitle = Book::slug,
                            withIcon = false
                        )
                    ),
                    selectedItem = book
                )
                Spacer(modifier = Modifier.height(8.dp))
                SearchableDropdown(
                    items = levels,
                    searchSettings = SearchSettings(
                        searchEnabled = false
                    ),
                    dropdownConfig = DropdownConfig(
                        horizontalPadding = 8.dp,
                        shape = RoundedCornerShape(8.dp),
                        headerPlaceholder = { Text(stringResource(R.string.select_level)) },
                        headerBackgroundColor = MaterialTheme.colorScheme.background
                    ),
                    itemContentConfig = SingleItemContentConfig.Default(
                        defaultItem = DefaultDropdownItem(
                            title = Level::name,
                            subtitle = Level::slug,
                            withIcon = false
                        )
                    ),
                    selectedItem = level
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row {
                    Spacer(Modifier.width(0.dp))

                    Button(onClick = onDismissRequest) {
                        Text(stringResource(R.string.cancel))
                    }
                    Spacer(Modifier.width(6.dp))
                    Button(
                        onClick = {
                            language.value?.let { language ->
                                book.value?.let { book ->
                                    level.value?.let { level ->
                                        val project = Project(
                                            id = UUID.randomUUID().toString(),
                                            languageId = language.id,
                                            bookId = book.id,
                                            levelId = level.id
                                        )
                                        val projectWitData = ProjectWithData(
                                            project = project,
                                            language = language,
                                            book = book,
                                            level = level
                                        )
                                        onCreate(projectWitData)
                                        onDismissRequest()
                                    }
                                }
                            }
                        },
                        enabled = language.value != null
                                && book.value != null
                                && level.value != null
                    ) {
                        Text(stringResource(R.string.ok))
                    }
                }
            }
        }
    }
}