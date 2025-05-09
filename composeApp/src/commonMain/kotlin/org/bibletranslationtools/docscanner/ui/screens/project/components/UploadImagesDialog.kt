package org.bibletranslationtools.docscanner.ui.screens.project.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import docscanner.composeapp.generated.resources.Res
import docscanner.composeapp.generated.resources.cancel
import docscanner.composeapp.generated.resources.ok
import docscanner.composeapp.generated.resources.upload_images
import org.bibletranslationtools.docscanner.data.models.Image
import org.jetbrains.compose.resources.stringResource

@Composable
fun UploadImagesDialog(
    images: List<Image>,
    onUpload: (List<Image>) -> Unit,
    onDismissRequest: () -> Unit,
) {
    var selectedImages by remember(images) {
        mutableStateOf<List<Image>>(emptyList())
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
                    stringResource(Res.string.upload_images),
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyColumn {
                    items(images) { image ->
                        Row(
                            Modifier.clickable {
                                selectedImages = if (selectedImages.contains(image)) {
                                    selectedImages.filterNot { image == it }
                                } else {
                                    selectedImages + image
                                }
                            }
                        ) {
                            Text(
                                text = image.name,
                                color = if (selectedImages.contains(image)) {
                                    MaterialTheme.colorScheme.primary
                                } else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(9.dp))
                Row {
                    Spacer(Modifier.width(0.dp))

                    Button(onClick = onDismissRequest) {
                        Text(stringResource(Res.string.cancel))
                    }

                    Spacer(Modifier.width(6.dp))

                    Button(
                        onClick = {
                            onUpload(selectedImages)
                            onDismissRequest()
                        },
                        enabled = selectedImages.isNotEmpty()
                    ) {
                        Text(stringResource(Res.string.ok))
                    }
                }
            }
        }
    }
}