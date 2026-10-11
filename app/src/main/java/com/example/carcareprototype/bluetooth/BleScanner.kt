package com.example.carcareprototype.bluetooth

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

class BleScanner(context: Context) {
    private val appContext = context.applicationContext
    private val adapter: BluetoothAdapter? =
        appContext.getSystemService(BluetoothManager::class.java)?.adapter

    private var callback: ((BluetoothScanEvent) -> Unit)? = null
    private var scanning = false

    private fun connectAllowed() = Build.VERSION.SDK_INT < 31 ||
        ContextCompat.checkSelfPermission(appContext, Manifest.permission.BLUETOOTH_CONNECT) ==
        PackageManager.PERMISSION_GRANTED

    private fun scanAllowed() = if (Build.VERSION.SDK_INT >= 31) {
        ContextCompat.checkSelfPermission(appContext, Manifest.permission.BLUETOOTH_SCAN) ==
            PackageManager.PERMISSION_GRANTED
    } else {
        ContextCompat.checkSelfPermission(appContext, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    private fun describe(result: ScanResult): BluetoothDeviceInfo {
        val device = result.device
        val type = when (device.type) {
            BluetoothDevice.DEVICE_TYPE_DUAL -> "Dual"
            BluetoothDevice.DEVICE_TYPE_LE -> "BLE"
            else -> "BLE"
        }
        val advertised = result.scanRecord?.serviceUuids
            ?.map { it.uuid.toString() }.orEmpty()
        val name = result.scanRecord?.deviceName?.takeIf { it.isNotBlank() }
            ?: device.name?.takeIf { it.isNotBlank() }
            ?: "Unknown device"
        return BluetoothDeviceInfo(
            name = name,
            address = device.address,
            bonded = device.bondState == BluetoothDevice.BOND_BONDED,
            type = type,
            rssi = result.rssi,
            bleAdvertisedUuids = advertised,
            observedBle = true
        )
    }

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) = handleScanResult(result)

        override fun onBatchScanResults(results: MutableList<ScanResult>) = results.forEach(::handleScanResult)

        override fun onScanFailed(errorCode: Int) {
            scanning = false
            callback?.invoke(BluetoothScanEvent.BleScanning(false))
            callback?.invoke(BluetoothScanEvent.Message("BLE_SCAN_ERROR", "Scan failed: $errorCode"))
        }
    }

    private fun handleScanResult(result: ScanResult) {
        try {
            callback?.invoke(BluetoothScanEvent.DeviceSeen(describe(result)))
        } catch (e: SecurityException) {
            callback?.invoke(BluetoothScanEvent.Message("BLE_PERMISSION", e.message ?: "Permission denied"))
        }
    }

    @SuppressLint("MissingPermission")
    fun start(onEvent: (BluetoothScanEvent) -> Unit) {
        callback = onEvent
        if (scanning) return
        if (!scanAllowed() || !connectAllowed()) {
            onEvent(BluetoothScanEvent.Message("PERMISSION", "Grant Bluetooth scan/connect permissions"))
            return
        }
        val bt = adapter
        if (bt?.isEnabled != true) {
            onEvent(BluetoothScanEvent.Message("BLE_SCAN_ERROR", "Bluetooth disabled or adapter unavailable"))
            return
        }
        val le = bt.bluetoothLeScanner
        if (le == null) {
            onEvent(BluetoothScanEvent.Message("BLE_SCAN_ERROR", "BLE scanner unavailable"))
            return
        }
        try {
            le.startScan(scanCallback)
            scanning = true
            onEvent(BluetoothScanEvent.BleScanning(true))
            onEvent(BluetoothScanEvent.Message("BLE_SCAN", "Started BLE scan"))
        } catch (e: SecurityException) {
            onEvent(BluetoothScanEvent.Message("BLE_PERMISSION", e.message ?: "Permission denied"))
        } catch (e: IllegalStateException) {
            onEvent(BluetoothScanEvent.Message("BLE_SCAN_ERROR", e.message ?: "Scanner unavailable"))
        }
    }

    @SuppressLint("MissingPermission")
    fun stop() {
        if (scanning && scanAllowed()) {
            try { adapter?.bluetoothLeScanner?.stopScan(scanCallback) }
            catch (_: SecurityException) { }
        }
        scanning = false
        callback?.invoke(BluetoothScanEvent.BleScanning(false))
        callback = null
    }
}
