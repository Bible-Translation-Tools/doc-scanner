package org.bibletranslationtools.docscanner.ui.screens.project.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import org.bibletranslationtools.docscanner.R
import org.bibletranslationtools.docscanner.data.models.Project
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateProjectDialog(
    onCreate: (Project) -> Unit,
    onDismissRequest: () -> Unit,
) {
    var languageText by remember {
        mutableStateOf<String>("")
    }

    var bookText by remember {
        mutableStateOf<String>("")
    }

    var levelText by remember {
        mutableStateOf<String>("")
    }

    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surface
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
                OutlinedTextField(
                    value = languageText,
                    onValueChange = { languageText = it },
                    label = {
                        Text(stringResource(R.string.language))
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = bookText,
                    onValueChange = { bookText = it },
                    label = {
                        Text(stringResource(R.string.book))
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = levelText,
                    onValueChange = { levelText = it },
                    label = {
                        Text(stringResource(R.string.level))
                    }
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
                            val project = Project(
                                id = UUID.randomUUID().toString(),
                                language = languageText,
                                book = bookText,
                                level = levelText
                            )
                            onCreate(project)
                            onDismissRequest()
                        },
                        enabled = languageText.isNotEmpty() &&
                                bookText.isNotEmpty() &&
                                levelText.isNotEmpty()
                    ) {
                        Text(stringResource(R.string.ok))
                    }
                }
            }
        }
    }
}