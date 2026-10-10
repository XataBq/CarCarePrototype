package com.example.carcareprototype.bluetooth

import android.Manifest
import android.os.Build
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

@Composable
fun BluetoothScreen() {

    val context = LocalContext.current

    val scanner = remember {
        BluetoothScanner(context)
    }

    val state by scanner.state.collectAsStateWithLifecycle()

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        scanner.refresh()
    }

    DisposableEffect(scanner) {
        scanner.register()

        onDispose {
            scanner.close()
        }
    }

    val permissions = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT
            )
        } else {
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        Text(
            "Bluetooth Diagnostics",
            style = MaterialTheme.typography.headlineSmall
        )

        Text(
            "Bluetooth: " + when {
                !state.available -> "Unavailable"
                state.enabled -> "Enabled"
                else -> "Disabled"
            }
        )

        Button(
            onClick = {
                permissionLauncher.launch(permissions)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Grant permissions")
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = scanner::refresh,
                modifier = Modifier.weight(1f)
            ) {
                Text("Paired devices")
            }

            Button(
                onClick = scanner::startDiscovery,
                modifier = Modifier.weight(1f),
                enabled = state.enabled && !state.scanning
            ) {
                Text(if (state.scanning) "Scanning..." else "Scan")
            }
        }

        Text(
            "Devices (${state.devices.size})",
            style = MaterialTheme.typography.titleMedium
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(
                items = state.devices,
                key = { it.address }
            ) { device ->

                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            device.name,
                            style = MaterialTheme.typography.titleMedium
                        )

                        Text("Address: ${device.address}")
                        Text("Type: ${device.type}")

                        Text(
                            "Paired: " +
                                if (device.bonded) "Yes" else "No"
                        )

                        Text("Services: ${device.uuids.size}")

                        device.uuids.forEach { uuid ->
                            Text(
                                uuid,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                scanner.fetchUuids(device.address)
                            }
                        ) {
                            Text("Discover services (SDP)")
                        }
                    }
                }
            }
        }

        HorizontalDivider()

        Text(
            "Bluetooth logs",
            style = MaterialTheme.typography.titleSmall
        )

        Column(
            modifier = Modifier.heightIn(max = 120.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            state.logs.takeLast(4).forEach { log ->
                Text(
                    log,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}