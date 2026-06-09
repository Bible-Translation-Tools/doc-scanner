package org.bibletranslationtools.docscanner.ui.screens.project.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import docscanner.composeapp.generated.resources.Res
import docscanner.composeapp.generated.resources.cancel
import docscanner.composeapp.generated.resources.chapter
import docscanner.composeapp.generated.resources.name
import docscanner.composeapp.generated.resources.ok
import docscanner.composeapp.generated.resources.upload_images
import kotlinx.io.files.Path
import org.bibletranslationtools.docscanner.data.models.Image
import org.bibletranslationtools.docscanner.ui.common.ImageDialog
import org.jetbrains.compose.resources.stringResource

private const val MAX_CHAPTER_LENGTH = 3

@Composable
fun UploadImagesDialog(
    images: List<Image>,
    onUpload: (List<Image>) -> Unit,
    onDismissRequest: () -> Unit,
) {
    var cachedImages by rememberSaveable { mutableStateOf(images) }
    var selectedImages by rememberSaveable { mutableStateOf<List<Image>>(emptyList()) }
    var imagePreview by rememberSaveable { mutableStateOf<Image?>(null) }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.padding(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    stringResource(Res.string.upload_images),
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(modifier = Modifier.height(32.dp))
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(Res.string.name),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Row {
                        Text(
                            text = stringResource(Res.string.chapter),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "",
                            modifier = Modifier.width(50.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                BoxWithConstraints {
                    val parentMaxHeight = this.maxHeight
                    val targetHeightFraction = 0.4f
                    val calculatedMaxHeight = with(LocalDensity.current) {
                        (parentMaxHeight.toPx() * targetHeightFraction).toDp()
                    }
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.heightIn(max = calculatedMaxHeight)
                    ) {
                        items(cachedImages) { image ->
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                                    .clickable { imagePreview = image }
                            ) {
                                Text(text = Path(image.path).name)
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = if (image.chapter > 0) image.chapter.toString() else "",
                                        onValueChange = { chapter ->
                                            if (chapter.length <= MAX_CHAPTER_LENGTH) {
                                                cachedImages = cachedImages.map { img ->
                                                    if (img == image) {
                                                        img.copy(chapter = chapter.toIntOrNull() ?: 0)
                                                    } else {
                                                        img
                                                    }
                                                }
                                                selectedImages = selectedImages.filter {
                                                    it.path != image.path
                                                }
                                            }
                                        },
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(
                                            keyboardType = KeyboardType.Number
                                        ),
                                        modifier = Modifier.width(64.dp)
                                    )
                                    Checkbox(
                                        checked = selectedImages.contains(image),
                                        onCheckedChange = { selected ->
                                            selectedImages = if (selected) {
                                                selectedImages + image
                                            } else {
                                                selectedImages - image
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = onDismissRequest,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.outline
                        )
                    ) {
                        Text(stringResource(Res.string.cancel))
                    }

                    Spacer(modifier = Modifier.width(32.dp))

                    Button(
                        onClick = {
                            onUpload(selectedImages)
                            onDismissRequest()
                        },
                        enabled = selectedImages.isNotEmpty()
                                && selectedImages.all { it.chapter > 0 }
                    ) {
                        Text(stringResource(Res.string.ok))
                    }
                }
            }

            imagePreview?.let {
                ImageDialog(it.path) {
                    imagePreview = null
                }
            }
        }
    }
}