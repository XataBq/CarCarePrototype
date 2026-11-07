package com.example.carcareprototype.presentation

import android.net.Uri
import android.provider.DocumentsContract
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.example.carcareprototype.car.CarLogExportMode

@Composable
internal fun CarLogExportDialogs(
    export: CarExportState,
    onDismiss: () -> Unit,
    onSave: (CarLogExportMode) -> Unit,
    onChooseAnotherName: () -> Unit,
    onChangeName: (String) -> Unit,
    onPickFolder: (Uri?) -> Unit,
) {
    if (export.sessionOnly != null) {
        if (export.conflict != null) {
            AlertDialog(
                onDismissRequest = onDismiss,
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
                        TextButton(onClick = { onSave(CarLogExportMode.APPEND) }, enabled = !export.busy) {
                            Text("Append")
                        }
                        TextButton(onClick = { onSave(CarLogExportMode.REPLACE) }, enabled = !export.busy) {
                            Text("Replace")
                        }
                        TextButton(onClick = onChooseAnotherName, enabled = !export.busy) {
                            Text("Change name")
                        }
                    }
                },
                dismissButton = {
                    TextButton(onClick = onDismiss, enabled = !export.busy) { Text("Cancel") }
                }
            )
        } else {
            AlertDialog(
                onDismissRequest = onDismiss,
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
                            onValueChange = onChangeName,
                            label = { Text("File name (.jsonl)") },
                            singleLine = true,
                            enabled = !export.busy,
                            isError = export.error != null
                        )
                        else Text("File: ${export.fileName}")
                        Text("Choose a folder on your phone, for example Documents/CarCare.")
                        export.folder?.let { Text("Folder: ${DocumentsContract.getTreeDocumentId(it)}") }
                        TextButton(
                            onClick = { onPickFolder(export.folder) },
                            enabled = !export.busy
                        ) { Text(if (export.folder == null) "Choose folder" else "Change folder") }
                        export.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                        if (export.busy) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = { onSave(CarLogExportMode.CREATE) },
                        enabled = export.folder != null && !export.busy
                    ) { Text("Save") }
                },
                dismissButton = {
                    TextButton(onClick = onDismiss, enabled = !export.busy) { Text("Cancel") }
                }
            )
        }
    }
}
