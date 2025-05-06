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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.RESULT_FORMAT_PDF
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.SCANNER_MODE_FULL
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.bibletranslationtools.docscanner.R
import org.bibletranslationtools.docscanner.data.local.DirectoryProvider
import org.bibletranslationtools.docscanner.data.models.Pdf
import org.bibletranslationtools.docscanner.data.models.ProjectWithData
import org.bibletranslationtools.docscanner.data.models.getName
import org.bibletranslationtools.docscanner.data.models.getRepo
import org.bibletranslationtools.docscanner.ui.screens.common.ErrorScreen
import org.bibletranslationtools.docscanner.ui.screens.common.ExtraAction
import org.bibletranslationtools.docscanner.ui.screens.common.LoadingDialog
import org.bibletranslationtools.docscanner.ui.screens.common.PageType
import org.bibletranslationtools.docscanner.ui.screens.common.TopNavigationBar
import org.bibletranslationtools.docscanner.ui.screens.project.components.PdfLayout
import org.bibletranslationtools.docscanner.ui.screens.project.components.PdfRenameDialog
import org.bibletranslationtools.docscanner.ui.viewmodel.ProjectViewModel
import org.bibletranslationtools.docscanner.utils.FileUtilities
import org.bibletranslationtools.docscanner.utils.showToast
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class ProjectScreen(private val project: ProjectWithData) : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val viewModel = koinScreenModel<ProjectViewModel> {
            parametersOf(project)
        }
        val directoryProvider: DirectoryProvider = koinInject()

        var renamePdf by remember { mutableStateOf<Pdf?>(null) }
        val uiScope = rememberCoroutineScope()

        val activity = LocalActivity.current
        val context = LocalContext.current

        val pdfs by viewModel.pdfState.collectAsStateWithLifecycle()
        var expandedItemId by remember { mutableStateOf<String?>(null) }

        // val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

        val scannerLauncher = rememberLauncherForActivityResult(
            contract = StartIntentSenderForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val scanningResult = GmsDocumentScanningResult.fromActivityResultIntent(result.data)

                scanningResult?.pdf?.let { pdf ->
                    Log.d("pdfName", pdf.uri.lastPathSegment.toString())

                    val date = Date()
                    val fileName = SimpleDateFormat(
                        "dd-MMM-yyyy HH:mm:ss",
                        Locale.getDefault()
                    ).format(date) + ".pdf"

                    FileUtilities.copyPdfFileToAppDirectory(
                        context,
                        directoryProvider,
                        pdf.uri,
                        fileName,
                        project
                    )

                    uiScope.launch(Dispatchers.IO) {
                        val repo = project.getRepo(directoryProvider)
                        repo.setAuthor("mxaln", "murmax82@gmail.com")
                        repo.commit()
                    }

                    val pdfEntity = Pdf(
                        UUID.randomUUID().toString(),
                        fileName,
                        FileUtilities.getFileSize(directoryProvider, fileName, project),
                        date,
                        project.project.id
                    )

                    viewModel.insertPdf(pdfEntity)
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

        if (viewModel.loadingDialog) {
            LoadingDialog(
                onDismissRequest = { viewModel.loadingDialog = false }
            )
        }

        renamePdf?.let { pdf ->
            PdfRenameDialog(
                name = pdf.name,
                onRename = { viewModel.renamePdf(pdf, it) },
                onDismissRequest = { renamePdf = null }
            )
        }

        Scaffold(
            topBar = {
                val extraActions = mutableListOf<ExtraAction>()
                TopNavigationBar(
                    title = project.getName(),
                    profile = null,
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
                    }, text = {
                        Text(
                            text = stringResource(R.string.scan),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }, icon = {
                        Icon(
                            painter = painterResource(id = R.drawable.document_scanner),
                            contentDescription = "Scan"
                        )
                    })
            }
        ) { paddingValue ->

            if (pdfs.isEmpty()) {
                ErrorScreen(message = stringResource(R.string.no_scan_found))
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValue)
                ) {
                    items(items = pdfs, key = { it.id }) { pdf ->
                        PdfLayout(
                            pdf = pdf,
                            menuShown = expandedItemId == pdf.id,
                            onCardClick = {
                                val getFileUri = FileUtilities.getFileUri(
                                    context,
                                    directoryProvider,
                                    pdf.name,
                                    project
                                )
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
        }
    }
}
