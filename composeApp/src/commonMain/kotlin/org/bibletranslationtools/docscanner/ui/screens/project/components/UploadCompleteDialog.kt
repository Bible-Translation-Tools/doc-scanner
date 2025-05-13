package org.bibletranslationtools.docscanner.ui.screens.project.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import docscanner.composeapp.generated.resources.Res
import docscanner.composeapp.generated.resources.ok
import org.jetbrains.compose.resources.stringResource

data class UploadStatus(
    val message: String,
    val url: String? = null,
    val onDismiss: () -> Unit
)

@Composable
fun UploadCompleteDialog(status: UploadStatus) {
    val uriHandler = LocalUriHandler.current

    val url by remember { mutableStateOf(status.url) }

    Dialog(onDismissRequest = status.onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth()
                .height(IntrinsicSize.Min)
                .clip(RoundedCornerShape(16.dp))
        ) {
            Column(
                verticalArrangement = Arrangement.SpaceAround,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(32.dp)
                    .fillMaxHeight()
            ) {
                Text(text = status.message)

                url?.let {
                    Spacer(modifier = Modifier.height(16.dp))
                    TextButton(onClick = { uriHandler.openUri(it) }) {
                        Text(text = it)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = status.onDismiss,
                    modifier = Modifier.width(128.dp)
                ) {
                    Text(stringResource(Res.string.ok))
                }
            }
        }
    }
}