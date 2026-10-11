package com.example.carcareprototype.bluetooth

import android.Manifest
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.example.carcareprototype.bluetooth.uuids.BluetoothUuidCatalog

@Composable
fun BluetoothScreen(vm: BluetoothViewModel = viewModel()) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    val adapter = state.adapterState
    val link = state.link
    val context = LocalContext.current
    val busy = link.phase in setOf(LinkPhase.CONNECTING, LinkPhase.CONNECTED, LinkPhase.DISCOVERING, LinkPhase.READY)

    val permissions = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN)
        } else arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { vm.refreshPaired() }
    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/x-ndjson")
    ) { uri -> if (uri != null) vm.exportHistory(uri) }

    DisposableEffect(vm) {
        vm.start()
        onDispose { vm.stop() }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Bluetooth Diagnostics", style = MaterialTheme.typography.headlineSmall)
            Text("Adapter: " + when {
                !adapter.available -> "Unavailable / permissions needed"
                adapter.enabled -> "Enabled"
                else -> "Disabled"
            })
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { permissionLauncher.launch(permissions) }) { Text("Permissions") }
                OutlinedButton(onClick = {
                    context.startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS))
                }) { Text("BT settings") }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = vm::refreshPaired) { Text("Paired") }
                Button(onClick = vm::scanClassic,
                    enabled = adapter.enabled && !adapter.scanning && !busy) {
                    Text(if (adapter.scanning) "Classic…" else "Classic")
                }
                Button(onClick = vm::scanBle,
                    enabled = adapter.enabled && !adapter.bleScanning && !busy) {
                    Text(if (adapter.bleScanning) "BLE…" else "BLE")
                }
            }
        }
        item {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text("Active link", style = MaterialTheme.typography.titleMedium)
                    Text("${link.mode} / ${link.phase}")
                    link.address?.let { Text("Address: $it") }
                    link.error?.let { Text("Error: $it", color = MaterialTheme.colorScheme.error) }
                    if (busy) {
                        OutlinedButton(onClick = vm::disconnect) { Text("Disconnect") }
                    }
                    if (link.services.isNotEmpty()) {
                        Text("GATT services: ${link.services.size}")
                        link.services.forEach { service ->
                            BluetoothUuidLabel(
                                service.uuid,
                                BluetoothUuidCatalog.gattService(service.uuid)
                            )
                            service.characteristics.forEach { char ->
                                BluetoothUuidLabel(
                                    uuid = char.uuid,
                                    meaning = BluetoothUuidCatalog.gattCharacteristic(service.uuid, char.uuid),
                                    properties = char.properties,
                                    modifier = Modifier.padding(start = 12.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
        item { Text("Devices (${state.devices.size})", style = MaterialTheme.typography.titleMedium) }
        items(state.devices, key = { it.address }) { device ->
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text(device.name, style = MaterialTheme.typography.titleMedium)
                    Text("Address: ${device.address}")
                    Text("Type: ${device.type}; paired: ${device.bonded}")
                    Text("RSSI: ${device.rssi?.let { "$it dBm" } ?: "N/A"}")
                    Text("Classic UUID (${device.uuidSource}): ${device.uuids.size}")
                    device.uuids.forEach { uuid ->
                        BluetoothUuidLabel(uuid, BluetoothUuidCatalog.classic(uuid))
                    }
                    Text("BLE advertisement UUIDs: ${device.bleAdvertisedUuids.size}")
                    device.bleAdvertisedUuids.forEach { uuid ->
                        BluetoothUuidLabel(uuid, BluetoothUuidCatalog.gattService(uuid))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { vm.requestSdp(device.address) }, enabled = !busy) {
                            Text("SDP")
                        }
                        OutlinedButton(
                            onClick = { vm.connectSpp(device.address) },
                            enabled = !busy && device.bonded && RfcommClient.SPP_UUID_TEXT in device.uuids
                        ) { Text("SPP") }
                        OutlinedButton(
                            onClick = { vm.connectGatt(device.address) },
                            enabled = !busy && (device.observedBle || device.type == "BLE" || device.type == "Dual")
                        ) { Text("GATT") }
                    }
                }
            }
        }
        item {
            HorizontalDivider()
            Text("Saved diagnostic events", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = {
                    exportLauncher.launch("bluetooth_diagnostics.jsonl")
                }) { Text("Export") }
                OutlinedButton(onClick = vm::clearHistory) { Text("Clear") }
            }
        }
        items(state.records.takeLast(40).asReversed(), key = { it.id }) { record ->
            val time = remember(record.timestampMillis) {
                SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(record.timestampMillis))
            }
            Text("[$time] ${record.kind}: ${record.message}" +
                (record.deviceName?.let { " ($it)" } ?: "") +
                (record.deviceAddress?.let { " [$it]" } ?: ""),
                style = MaterialTheme.typography.bodySmall)
        }
    }
}
