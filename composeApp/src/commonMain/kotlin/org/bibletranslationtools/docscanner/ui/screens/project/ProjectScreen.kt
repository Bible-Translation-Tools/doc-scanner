package org.bibletranslationtools.docscanner.ui.screens.project

import android.app.Activity
import android.content.Intent
import android.util.Log
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts.StartIntentSenderForResult
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.RESULT_FORMAT_PDF
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.SCANNER_MODE_FULL
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import docscanner.composeapp.generated.resources.Res
import docscanner.composeapp.generated.resources.document_scanner
import docscanner.composeapp.generated.resources.no_scan_found
import docscanner.composeapp.generated.resources.scan
import org.bibletranslationtools.docscanner.data.models.Pdf
import org.bibletranslationtools.docscanner.data.models.Project
import org.bibletranslationtools.docscanner.data.models.getTitle
import org.bibletranslationtools.docscanner.ui.common.AlertDialog
import org.bibletranslationtools.docscanner.ui.common.ErrorScreen
import org.bibletranslationtools.docscanner.ui.common.ExtraAction
import org.bibletranslationtools.docscanner.ui.common.PageType
import org.bibletranslationtools.docscanner.ui.common.ProgressDialog
import org.bibletranslationtools.docscanner.ui.common.TopNavigationBar
import org.bibletranslationtools.docscanner.ui.screens.project.components.PdfLayout
import org.bibletranslationtools.docscanner.ui.screens.project.components.PdfRenameDialog
import org.bibletranslationtools.docscanner.ui.viewmodel.ProjectViewModel
import org.bibletranslationtools.docscanner.utils.showToast
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.core.parameter.parametersOf

data class ProjectScreen(
    private val project: Project
) : Screen {
    @Composable
    override fun Content() {
        val viewModel = koinScreenModel<ProjectViewModel> {
            parametersOf(project)
        }

        var renamePdf by remember { mutableStateOf<Pdf?>(null) }

        val activity = LocalActivity.current
        val context = LocalContext.current

        val state by viewModel.state.collectAsStateWithLifecycle()
        var expandedItemId by remember { mutableStateOf<Int?>(null) }

        // val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

        val scannerLauncher = rememberLauncherForActivityResult(
            contract = StartIntentSenderForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val scanningResult = GmsDocumentScanningResult.fromActivityResultIntent(result.data)

                scanningResult?.pdf?.let { pdf ->
                    Log.d("pdfName", pdf.uri.lastPathSegment.toString())
                    viewModel.insertPdf(context, pdf.uri)
                }
            }
        }

        val scanner = remember {
            GmsDocumentScanning.getClient(
                GmsDocumentScannerOptions.Builder()
                    .setPageLimit(1000)
                    .setGalleryImportAllowed(true)
                    .setResultFormats(RESULT_FORMAT_PDF)
                    .setScannerMode(SCANNER_MODE_FULL)
                    .build()
            )
        }

        Scaffold(
            topBar = {
                val extraActions = mutableListOf<ExtraAction>()
                TopNavigationBar(
                    title = project.getTitle(),
                    profile = state.profile,
                    page = PageType.PROJECT,
                    extraAction = extraActions.toTypedArray()
                )
            },
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    modifier = Modifier.offset(0.dp, 0.dp),
                    onClick = {
                        scanner.getStartScanIntent(activity!!).addOnSuccessListener {
                            scannerLauncher.launch(
                                IntentSenderRequest.Builder(it).build()
                            )
                        }.addOnFailureListener {
                            it.printStackTrace()
                            context.showToast(it.message.toString())
                        }
                    },
                    text = {
                        Text(
                            text = stringResource(Res.string.scan),
                            style = MaterialTheme.typography.titleMedium
                        )
                    },
                    icon = {
                        Icon(
                            painter = painterResource(Res.drawable.document_scanner),
                            contentDescription = "Scan"
                        )
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                )
            }
        ) { paddingValue ->

            if (state.pdfs.isEmpty()) {
                ErrorScreen(message = stringResource(Res.string.no_scan_found))
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValue)
                ) {
                    items(items = state.pdfs, key = { it.id }) { pdf ->
                        PdfLayout(
                            pdf = pdf,
                            menuShown = expandedItemId == pdf.id,
                            onCardClick = {
                                val getFileUri = viewModel.getFileUri(context, pdf.name)
                                val browserIntent = Intent(Intent.ACTION_VIEW, getFileUri)
                                browserIntent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                                context.startActivity(browserIntent)
                            },
                            onMoreClick = {
                                expandedItemId = if (expandedItemId != pdf.id) {
                                    pdf.id
                                } else null
                            },
                            onRenameClick = {
                                renamePdf = pdf
                            },
                            onDeleteClick = {
                                viewModel.deletePdf(pdf)
                            },
                            onDismissRequest = { expandedItemId = null }
                        )
                    }
                }
            }

            state.progress?.let {
                ProgressDialog(it)
            }

            state.alert?.let {
                AlertDialog(it.message, it.onClosed)
            }

            renamePdf?.let { pdf ->
                PdfRenameDialog(
                    name = pdf.name,
                    onRename = { viewModel.renamePdf(pdf, it) },
                    onDismissRequest = { renamePdf = null }
                )
            }
        }
    }
}
