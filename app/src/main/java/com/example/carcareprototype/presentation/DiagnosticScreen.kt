package com.example.carcareprototype.presentation

import android.content.Intent
import android.net.Uri
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.carcareprototype.car.CarMileagePermission
import com.example.carcareprototype.car.toCarScreenState

@Composable
internal fun DiagnosticScreen(
    vm: DiagnosticViewModel = viewModel(factory = DiagnosticViewModelFactory(LocalContext.current)),
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val mileageDisplay = remember(state.androidAutoConnected, state.mileage) { state.toCarScreenState() }
    val retryFeedback by vm.retryFeedback.collectAsStateWithLifecycle()
    val permission by vm.mileagePermission.collectAsStateWithLifecycle()
    val export by vm.export.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var editingCar by rememberSaveable { mutableStateOf(false) }
    var carId by rememberSaveable { mutableStateOf("") }
    var permissionRequested by rememberSaveable { mutableStateOf(false) }
    val mileagePermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        vm.onMileagePermissionResult(granted)
    }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        vm.refreshMileagePermission()
    }
    LaunchedEffect(Unit) {
        if (!permission.granted && !permissionRequested) {
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
                TextButton(onClick = vm::retryMileage) { Text("Retry mileage") }
                retryFeedback?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                if (!permission.granted) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(if (permission.denied) "Mileage access denied" else "Mileage permission required",
                            modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
                        TextButton(onClick = {
                            if (permission.denied) {
                                context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                    Uri.parse("package:${context.packageName}")))
                            } else {
                                permissionRequested = true
                                mileagePermissionLauncher.launch(CarMileagePermission.NAME)
                            }
                        }) { Text(if (permission.denied) "App settings" else "Allow mileage") }
                    }
                }
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Last session: ${state.lastSessionCarId ?: "Unidentified car"}",
                modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
            TextButton(onClick = vm::logManualEvent) {
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
                    vm.selectCar(carId)
                    editingCar = false
                }, enabled = carId.isNotBlank() && carId.none(Char::isISOControl)) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { editingCar = false }) { Text("Cancel") } }
        )
    }

    CarLogExportDialogs(
        export = export,
        onDismiss = vm::dismissExport,
        onSave = vm::save,
        onChooseAnotherName = vm::chooseAnotherName,
        onChangeName = vm::changeName,
        onPickFolder = { folderPicker.launch(it) },
    )
}
