package com.example.carcareprototype.bluetooth

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

internal class BluetoothScanner(
    context: Context,
) {
    private val appContext = context.applicationContext

    private val manager = appContext.getSystemService(
        BluetoothManager::class.java
    )

    private val adapter: BluetoothAdapter?
        get() = manager?.adapter

    private val _state = MutableStateFlow(BluetoothUiState())
    val state = _state.asStateFlow()

    private var receiverRegistered = false

    private fun hasConnectPermission(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
            ContextCompat.checkSelfPermission(
                appContext,
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED

    private fun hasScanPermission(): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(
                appContext,
                Manifest.permission.BLUETOOTH_SCAN
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(
                appContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        }

    private fun log(message: String) {
        _state.update {
            it.copy(logs = (it.logs + message).takeLast(100))
        }
    }

    @SuppressLint("MissingPermission")
    private fun toInfo(device: BluetoothDevice): BluetoothDeviceInfo {
        val type = when(device.type) {
            BluetoothDevice.DEVICE_TYPE_CLASSIC -> "Classic"
            BluetoothDevice.DEVICE_TYPE_LE -> "BLE"
            BluetoothDevice.DEVICE_TYPE_DUAL -> "Dual"
            else -> "Unknown"
        }

        return BluetoothDeviceInfo(
            name = device.name ?: "Unknown device",
            address = device.address,
            bonded = device.bondState == BluetoothDevice.BOND_BONDED,
            type = type,
            uuids = device.uuids
                ?.map { it.uuid.toString() }
                .orEmpty()
        )
    }

    @SuppressLint("MissingPermission")
    private fun addOrUpdate(device: BluetoothDevice) {
        if (!hasConnectPermission()) return

        val info = toInfo(device)

        _state.update { current ->
            val devices = current.devices
                .filterNot { it.address == info.address } + info

            current.copy(
                devices = devices.sortedWith(
                    compareByDescending<BluetoothDeviceInfo> { it.bonded }
                        .thenBy { it.name }
                )
            )
        }
    }

    private val receiver = object : BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        override fun onReceive(context: Context, intent: Intent) {

            when (intent.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val device = intent.bluetoothDeviceExtra() ?: return
                    addOrUpdate(device)
                    log("Found: ${device.address}")
                }

                BluetoothDevice.ACTION_UUID -> {
                    val device = intent.bluetoothDeviceExtra() ?: return

                    addOrUpdate(device)
                    log("SDP result: ${device.address}")

                    val uuids = device.uuids
                        ?.map { it.uuid.toString() }
                        .orEmpty()

                    _state.update { current ->
                        current.copy(
                            devices = current.devices.map {
                                if (it.address == device.address) {
                                    it.copy(uuids = uuids)
                                } else it
                            }
                        )
                    }
                    log("UUID count: ${uuids.size}")
                }

                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    _state.update { it.copy(scanning = false) }
                    log("Discovery finished")
                }

                BluetoothAdapter.ACTION_STATE_CHANGED -> {
                    refresh()
                }
            }
        }
    }

    private fun Intent.bluetoothDeviceExtra(): BluetoothDevice? {
        return if (Build.VERSION.SDK_INT >= 33) {
            getParcelableExtra(
                BluetoothDevice.EXTRA_DEVICE,
                BluetoothDevice::class.java,
            )
        } else {
            Suppress("DEPRECATION")
            getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
        }
    }

    fun register() {
        if (receiverRegistered) return

        val filter = IntentFilter().apply {
            addAction(BluetoothDevice.ACTION_FOUND)
            addAction(BluetoothDevice.ACTION_UUID)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
            addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
        }

        ContextCompat.registerReceiver(
            appContext,
            receiver,
            filter,
            ContextCompat.RECEIVER_EXPORTED
        )

        receiverRegistered = true
        refresh()
    }

    @SuppressLint("MissingPermission")
    fun refresh() {
        val bluetoothAdapter = adapter

        if (bluetoothAdapter == null) {
            _state.update {
                it.copy(available = false, enabled = false)
            }
            log("Bluetooth adapter unavailable")
            return
        }

        val enabled = bluetoothAdapter.isEnabled

        _state.update {
            it.copy(available = true, enabled = enabled)
        }

        if (!enabled) {
            log("Bluetooth disabled")
            return
        }

        if (!hasConnectPermission()) {
            log("BLUETOOTH_CONNECT permission missing")
            return
        }

        try {
            bluetoothAdapter.bondedDevices.forEach(::addOrUpdate)
            log(
                "Bonded devices: " +
                    bluetoothAdapter.bondedDevices.size
            )
        } catch (e: SecurityException) {
            log("Permission error: ${e.message}")
        }
    }

    @SuppressLint("MissingPermission")
    fun startDiscovery() {
        val bluetoothAdapter = adapter ?: run {
            log("No Bluetooth adapter")
            return
        }

        if (!bluetoothAdapter.isEnabled) {
            log("Enable Bluetooth first")
            return
        }

        if (!hasScanPermission() || !hasConnectPermission()) {
            log("Bluetooth permissions missing")
            return
        }

        try {
            if (bluetoothAdapter.isDiscovering) {
                bluetoothAdapter.cancelDiscovery()
            }

            val started = bluetoothAdapter.startDiscovery()

            _state.update { it.copy(scanning = started) }
            log("Discovery start: $started")
        } catch (e: SecurityException) {
            log("Discovery permission error: ${e.message}")
        }
    }

    @SuppressLint("MissingPermission")
    fun fetchUuids(address: String) {
        if (!hasConnectPermission()) {
            log("BLUETOOTH_CONNECT permission missing")
            return
        }

        val bluetoothAdapter = adapter ?: return

        try {
            if (hasScanPermission() && bluetoothAdapter.isDiscovering) {
                bluetoothAdapter.cancelDiscovery()
            }

            val device = bluetoothAdapter.getRemoteDevice(address)

            val started = device.fetchUuidsWithSdp()

            log("SDP requested for $address: $started")
        } catch (e: Exception) {
            log("SDP error: ${e.message}")
        }
    }

    @SuppressLint("MissingPermission")
    fun close() {
        if (hasScanPermission()) {
            try {
                if (adapter?.isDiscovering == true) {
                    adapter?.cancelDiscovery()
                }
            } catch (_: SecurityException) {}
        }

        if (receiverRegistered) {
            appContext.unregisterReceiver(receiver)
            receiverRegistered = false
        }
    }
}