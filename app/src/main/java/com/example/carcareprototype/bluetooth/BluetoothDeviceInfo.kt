package com.example.carcareprototype.bluetooth

data class BluetoothDeviceInfo(
    val name: String,
    val address: String,
    val bonded: Boolean,
    val type: String,
    val uuids: List<String> = emptyList(),
)

data class BluetoothUiState(
    val available: Boolean = false,
    val enabled: Boolean = false,
    val scanning: Boolean = false,
    val devices: List<BluetoothDeviceInfo> = emptyList(),
    val logs: List<String> = emptyList(),
)