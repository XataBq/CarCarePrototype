package com.example.carcareprototype.presentation

import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.carcareprototype.car.CarLogExportMode
import com.example.carcareprototype.car.CarMileagePermission
import com.example.carcareprototype.car.MileageRetry
import com.example.carcareprototype.car.toCarScreenState
import com.example.carcareprototype.diagnostics.DiagnosticRepository

@Composable
internal fun DiagnosticScreen(vm: DiagnosticViewModel = viewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    val mileageDisplay = state.toCarScreenState()
    val retryFeedback by MileageRetry.feedback.collectAsStateWithLifecycle()
    val export by vm.export.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var editingCar by rememberSaveable { mutableStateOf(false) }
    var carId by rememberSaveable { mutableStateOf("") }
    var mileagePermissionGranted by remember { mutableStateOf(CarMileagePermission.isGranted(context)) }
    var permissionRequested by rememberSaveable { mutableStateOf(false) }
    var permissionDenied by rememberSaveable { mutableStateOf(false) }
    val mileagePermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        mileagePermissionGranted = granted && CarMileagePermission.isGranted(context)
        permissionDenied = !mileagePermissionGranted
        CarMileagePermission.reportRequestResult(mileagePermissionGranted)
        DiagnosticRepository.log("Phone mileage permission result granted=$mileagePermissionGranted")
    }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        mileagePermissionGranted = CarMileagePermission.isGranted(context)
        if (mileagePermissionGranted) permissionDenied = false
        CarMileagePermission.refresh()
    }
    LaunchedEffect(Unit) {
        if (!CarMileagePermission.isGranted(context) && !permissionRequested) {
            permissionRequested = true
            mileagePermissionLauncher.launch(CarMileagePermission.NAME)
        }
    }
    val folderPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) {
        it?.let(vm::selectFolder)
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Car Connection Tester", style = MaterialTheme.typography.headlineSmall)
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text("Android Auto: " + (if (state.androidAutoConnected) "Car session active" else "Not connected"))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Car ID: ${state.selectedCarId.ifEmpty { "Not set" }}", modifier = Modifier.weight(1f))
                    TextButton(onClick = { carId = state.selectedCarId; editingCar = true }) {
                        Text("Set car ID")
                    }
                }
                HorizontalDivider()
                Text("Last received: ${mileageDisplay.mileageText}",
                    style = MaterialTheme.typography.titleLarge)
                Text("Current request: ${mileageDisplay.mileageStatus}")
                Text(mileageDisplay.receiptText + " • " + mileageDisplay.freshness, style = MaterialTheme.typography.bodySmall)
                Text("Last API: ${mileageDisplay.responseText}; raw timestamp: ${state.mileage.lastReply?.rawTimestamp ?: "N/A"}", style = MaterialTheme.typography.bodySmall)
                Text("Source: Android Auto Car Hardware API", style = MaterialTheme.typography.bodySmall)
                Text("Mileage verification: UNVERIFIED — compare with dashboard; not used for maintenance", style = MaterialTheme.typography.bodySmall)
                TextButton(onClick = MileageRetry::request) { Text("Retry mileage") }
                retryFeedback?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                if (!mileagePermissionGranted) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(if (permissionDenied) "Mileage access denied" else "Mileage permission required",
                            modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
                        TextButton(onClick = {
                            if (permissionDenied) {
                                context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                    Uri.parse("package:${context.packageName}")))
                            } else {
                                permissionRequested = true
                                mileagePermissionLauncher.launch(CarMileagePermission.NAME)
                            }
                        }) { Text(if (permissionDenied) "App settings" else "Allow mileage") }
                    }
                }
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Last session: ${state.lastSessionCarId ?: "Unidentified car"}",
                modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
            TextButton(onClick = { DiagnosticRepository.log("Manual diagnostic event") }) {
                Text("Test logging")
            }
        }
        export.message?.let { message ->
            Text(message, style = MaterialTheme.typography.bodySmall)
            export.savedFile?.let { uri ->
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, "application/x-ndjson")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                if (intent.resolveActivity(context.packageManager) != null) {
                    TextButton(onClick = { context.startActivity(intent) }) { Text("Open saved file") }
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = { vm.beginExport(false) },
                enabled = !export.busy,
                modifier = Modifier.weight(1f)
            ) { Text("Export all logs") }
            Button(
                onClick = { vm.beginExport(true) },
                enabled = !export.busy && state.lastSessionId != null,
                modifier = Modifier.weight(1f)
            ) { Text("Export last session") }
        }
        HorizontalDivider()
        LazyColumn(
            modifier = Modifier.fillMaxWidth().weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            item { Text("Diagnostic logs (last 100)", style = MaterialTheme.typography.titleMedium) }
            state.storageError?.let { error ->
                item { Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
            }
            if (state.logs.isEmpty()) item { Text("No diagnostic events yet") }
            items(state.logs.asReversed()) { log ->
                Text(log, style = MaterialTheme.typography.bodySmall)
            }
        }
    }

    if (editingCar) {
        AlertDialog(
            onDismissRequest = { editingCar = false },
            title = { Text("Car ID") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = carId, onValueChange = { carId = it.take(80) },
                        label = { Text("Unique name for this car") }, singleLine = true)
                    Text("Manual label, not verified vehicle identity. Reconnects within 2 minutes share logs. Changing this label or reconnecting clears the displayed mileage.")
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    DiagnosticRepository.selectCar(carId)
                    editingCar = false
                }, enabled = carId.isNotBlank() && carId.none(Char::isISOControl)) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { editingCar = false }) { Text("Cancel") } }
        )
    }

    if (export.sessionOnly != null) {
        if (export.conflict != null) {
            AlertDialog(
                onDismissRequest = vm::dismissExport,
                properties = DialogProperties(
                    dismissOnBackPress = !export.busy,
                    dismissOnClickOutside = !export.busy
                ),
                title = { Text("File already exists") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("${export.fileName} already exists in the selected folder.")
                        Text("Append keeps the existing contents. Replace overwrites the file.")
                        if (export.busy) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }
                },
                confirmButton = {
                    Column {
                        TextButton(onClick = { vm.save(CarLogExportMode.APPEND) }, enabled = !export.busy) {
                            Text("Append")
                        }
                        TextButton(onClick = { vm.save(CarLogExportMode.REPLACE) }, enabled = !export.busy) {
                            Text("Replace")
                        }
                        TextButton(onClick = vm::chooseAnotherName, enabled = !export.busy) {
                            Text("Change name")
                        }
                    }
                },
                dismissButton = {
                    TextButton(onClick = vm::dismissExport, enabled = !export.busy) { Text("Cancel") }
                }
            )
        } else {
            AlertDialog(
                onDismissRequest = vm::dismissExport,
                properties = DialogProperties(
                    dismissOnBackPress = !export.busy,
                    dismissOnClickOutside = !export.busy
                ),
                title = { Text(if (export.sessionOnly == true) "Export last session logs" else "Export all logs") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (export.sessionOnly == true) Text("Car: ${export.sessionCarId ?: "Unidentified car"}")
                        if (export.editName) OutlinedTextField(
                            value = export.fileName,
                            onValueChange = vm::changeName,
                            label = { Text("File name (.jsonl)") },
                            singleLine = true,
                            enabled = !export.busy,
                            isError = export.error != null
                        )
                        else Text("File: ${export.fileName}")
                        Text("Choose a folder on your phone, for example Documents/CarCare.")
                        export.folder?.let { Text("Folder: ${DocumentsContract.getTreeDocumentId(it)}") }
                        TextButton(
                            onClick = { folderPicker.launch(export.folder) },
                            enabled = !export.busy
                        ) { Text(if (export.folder == null) "Choose folder" else "Change folder") }
                        export.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                        if (export.busy) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = { vm.save() },
                        enabled = export.folder != null && !export.busy
                    ) { Text("Save") }
                },
                dismissButton = {
                    TextButton(onClick = vm::dismissExport, enabled = !export.busy) { Text("Cancel") }
                }
            )
        }
    }
}
