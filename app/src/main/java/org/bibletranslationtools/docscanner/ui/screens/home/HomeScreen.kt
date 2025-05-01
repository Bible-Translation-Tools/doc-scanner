package org.bibletranslationtools.docscanner.ui.screens.home

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
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import org.bibletranslationtools.docscanner.FileUtilities
import org.bibletranslationtools.docscanner.R
import org.bibletranslationtools.docscanner.data.local.DirectoryProvider
import org.bibletranslationtools.docscanner.data.models.PdfEntity
import org.bibletranslationtools.docscanner.ui.screens.common.ErrorScreen
import org.bibletranslationtools.docscanner.ui.screens.common.LoadingDialog
import org.bibletranslationtools.docscanner.ui.screens.home.components.PdfLayout
import org.bibletranslationtools.docscanner.ui.screens.home.components.RenameDialog
import org.bibletranslationtools.docscanner.ui.viewmodel.PdfViewModel
import org.bibletranslationtools.docscanner.utils.showToast
import org.koin.compose.koinInject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
class HomeScreen() : Screen {

    @Composable
    override fun Content() {
        val viewModel: PdfViewModel = koinScreenModel()
        val directoryProvider: DirectoryProvider = koinInject()

        var renamePdfEntity by remember { mutableStateOf<PdfEntity?>(null) }

        // for activity
        val activity = LocalActivity.current
        val context = LocalContext.current

        val pdfList by viewModel.pdfStateFlow.collectAsState()
        var expandedItemId by remember { mutableStateOf<String?>(null) }

        // val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

        val scannerLauncher =
            rememberLauncherForActivityResult(contract = StartIntentSenderForResult()) { result ->
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
                            fileName
                        )

                        val pdfEntity = PdfEntity(
                            UUID.randomUUID().toString(),
                            fileName,
                            FileUtilities.getFileSize(directoryProvider, fileName),
                            date
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
                    .setResultFormats(GmsDocumentScannerOptions.RESULT_FORMAT_PDF)
                    .setScannerMode(GmsDocumentScannerOptions.SCANNER_MODE_FULL)
                    .build()

            )
        }

        if (viewModel.loadingDialog) {
            LoadingDialog(
                onDismissRequest = { viewModel.loadingDialog = false }
            )
        }

        renamePdfEntity?.let { pdf ->
            RenameDialog(
                name = pdf.name,
                onRename = { viewModel.renamePdf(pdf, it) },
                onDismissRequest = { renamePdfEntity = null }
            )
        }

        Scaffold(
            topBar = {
                TopAppBar(title = {
                    Text(text = stringResource(id = R.string.app_name))
                })
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

            if (pdfList.isEmpty()) {
                ErrorScreen(message = "No Pdf found")
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValue)
                ) {
                    items(items = pdfList, key = { it.id }) { pdfEntity ->
                        PdfLayout(
                            pdfEntity = pdfEntity,
                            menuShown = expandedItemId == pdfEntity.id,
                            onCardClick = {
                                val getFileUri = FileUtilities.getFileUri(
                                    context,
                                    directoryProvider,
                                    pdfEntity.name
                                )
                                val browserIntent = Intent(Intent.ACTION_VIEW, getFileUri)
                                browserIntent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                                context.startActivity(browserIntent)
                            },
                            onMoreClick = {
                                expandedItemId = if (expandedItemId != pdfEntity.id) {
                                    pdfEntity.id
                                } else null
                            },
                            onUploadClick = {
                                viewModel.uploadPdf(pdfEntity)
                            },
                            onShareClick = {
                                val fileUri = FileUtilities.getFileUri(
                                    context,
                                    directoryProvider,
                                    pdfEntity.name
                                )
                                val shareIntent = Intent(Intent.ACTION_SEND)
                                shareIntent.type = "application/pdf"
                                shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri)
                                shareIntent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                                context.startActivity(
                                    Intent.createChooser(shareIntent, "share")
                                )
                            },
                            onRenameClick = {
                                renamePdfEntity = pdfEntity
                            },
                            onDeleteClick = {
                                viewModel.deletePdf(pdfEntity)
                            },
                            onDismissRequest = { expandedItemId = null }
                        )
                    }
                }
            }
        }
    }
}
