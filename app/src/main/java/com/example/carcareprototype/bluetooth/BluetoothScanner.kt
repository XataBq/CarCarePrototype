package com.example.carcareprototype.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.ParcelUuid
import android.os.Parcelable
import androidx.core.content.ContextCompat
import androidx.core.content.IntentCompat

class BluetoothScanner(context: Context) {

    private val appContext = context.applicationContext
    private val permissions = BluetoothPermissions(appContext)
    private val adapter: BluetoothAdapter? =
        appContext.getSystemService(BluetoothManager::class.java)?.adapter

    private var onEvent: ((BluetoothScanEvent) -> Unit)? = null
    private var registered = false

    private fun emit(event: BluetoothScanEvent) {
        onEvent?.invoke(event)
    }

    @SuppressLint("MissingPermission")
    private fun describe(
        device: BluetoothDevice,
        reportedName: String? = null,
        rssi: Int? = null
    ): BluetoothDeviceInfo {
        val type = when (device.type) {
            BluetoothDevice.DEVICE_TYPE_CLASSIC -> BluetoothDeviceType.CLASSIC
            BluetoothDevice.DEVICE_TYPE_LE -> BluetoothDeviceType.BLE
            BluetoothDevice.DEVICE_TYPE_DUAL -> BluetoothDeviceType.DUAL
            else -> BluetoothDeviceType.UNKNOWN
        }
        val name = reportedName?.takeIf { it.isNotBlank() }
            ?: device.name?.takeIf { it.isNotBlank() }
            ?: "Unknown device"
        val cachedUuids = device.uuids?.map { it.uuid.toString() }.orEmpty()

        return BluetoothDeviceInfo(
            name = name,
            address = device.address,
            bonded = device.bondState == BluetoothDevice.BOND_BONDED,
            type = type,
            rssi = rssi,
            uuids = cachedUuids,
            uuidSource = if (cachedUuids.isEmpty()) BluetoothUuidSource.NONE else BluetoothUuidSource.CACHED
        )
    }

    private fun Intent.bluetoothDevice(): BluetoothDevice? =
        IntentCompat.getParcelableExtra(
            this, BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java
        )

    private val receiver = object : BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        override fun onReceive(context: Context, intent: Intent) {
            try {
                when (intent.action) {
                    BluetoothDevice.ACTION_FOUND,
                    BluetoothDevice.ACTION_NAME_CHANGED,
                    BluetoothDevice.ACTION_BOND_STATE_CHANGED -> {
                        if (!permissions.canConnect()) return
                        val device = intent.bluetoothDevice() ?: return
                        val rssi = if (intent.hasExtra(BluetoothDevice.EXTRA_RSSI)) {
                            intent.getShortExtra(
                                BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE
                            ).toInt()
                        } else null
                        emit(BluetoothScanEvent.DeviceSeen(
                            describe(device, intent.getStringExtra(BluetoothDevice.EXTRA_NAME), rssi)
                        ))
                    }
                    BluetoothDevice.ACTION_UUID -> {
                        if (!permissions.canConnect()) return
                        val device = intent.bluetoothDevice() ?: return

                        val parcelables: Array<Parcelable>? =
                            IntentCompat.getParcelableArrayExtra(
                                intent,
                                BluetoothDevice.EXTRA_UUID,
                                Parcelable::class.java
                            )
                        val uuids = parcelables?.filterIsInstance<ParcelUuid>()
                            ?.map { it.uuid.toString() }
                        emit(BluetoothScanEvent.SdpResult(describe(device), uuids))
                    }
                    BluetoothAdapter.ACTION_DISCOVERY_STARTED ->
                        emit(BluetoothScanEvent.Scanning(true))
                    BluetoothAdapter.ACTION_DISCOVERY_FINISHED ->
                        emit(BluetoothScanEvent.Scanning(false))
                    BluetoothAdapter.ACTION_STATE_CHANGED -> refreshPaired()
                }
            } catch (e: SecurityException) {
                emit(BluetoothScanEvent.Message("PERMISSION", e.message ?: "Access denied"))
            } catch (e: RuntimeException) {
                emit(BluetoothScanEvent.Message("ERROR", "Broadcast: ${e.message}"))
            }
        }
    }

    fun start(callback: (BluetoothScanEvent) -> Unit) {
        onEvent = callback
        if (!registered) {
            val filter = IntentFilter().apply {
                addAction(BluetoothDevice.ACTION_FOUND)
                addAction(BluetoothDevice.ACTION_NAME_CHANGED)
                addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
                addAction(BluetoothDevice.ACTION_UUID)
                addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
                addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
                addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
            }
            ContextCompat.registerReceiver(
                appContext, receiver, filter, ContextCompat.RECEIVER_EXPORTED
            )
            registered = true
        }
        refreshPaired()
    }

    @SuppressLint("MissingPermission")
    fun refreshPaired() {
        val bt = adapter ?: run {
            emit(BluetoothScanEvent.Adapter(false, false))
            return
        }
        if (!permissions.canConnect()) {
            emit(BluetoothScanEvent.Message("PERMISSION", "Grant Bluetooth connect permission"))
            return
        }
        try {
            emit(BluetoothScanEvent.Adapter(true, bt.isEnabled))
            if (!bt.isEnabled) return
            val bonded = bt.bondedDevices
            bonded.forEach { emit(BluetoothScanEvent.DeviceSeen(describe(it))) }
            emit(BluetoothScanEvent.Message("PAIRED", "Paired devices: ${bonded.size}"))
        } catch (e: SecurityException) {
            emit(BluetoothScanEvent.Message("PERMISSION", e.message ?: "Access denied"))
        }
    }

    @SuppressLint("MissingPermission")
    fun discover() {
        if (!permissions.canConnect() || !permissions.canScan()) {
            emit(BluetoothScanEvent.Message("PERMISSION", "Grant Bluetooth scan/connect permissions"))
            return
        }
        val bt = adapter ?: run {
            emit(BluetoothScanEvent.Message("ERROR", "No Bluetooth adapter"))
            return
        }
        try {
            if (!bt.isEnabled) {
                emit(BluetoothScanEvent.Message("ERROR", "Bluetooth is disabled"))
                return
            }
            if (bt.isDiscovering) bt.cancelDiscovery()
            val started = bt.startDiscovery()
            emit(BluetoothScanEvent.Scanning(started))
            emit(BluetoothScanEvent.Message("DISCOVERY", "Classic discovery started=$started"))
        } catch (e: SecurityException) {
            emit(BluetoothScanEvent.Message("PERMISSION", e.message ?: "Access denied"))
        }
    }

    @SuppressLint("MissingPermission")
    fun requestSdp(address: String) {
        if (!permissions.canConnect() || !permissions.canScan()) {
            emit(BluetoothScanEvent.Message("PERMISSION", "Grant Bluetooth scan/connect permissions"))
            return
        }
        val bt = adapter ?: return
        try {
            if (bt.isDiscovering) bt.cancelDiscovery()
            val accepted = bt.getRemoteDevice(address).fetchUuidsWithSdp()
            emit(BluetoothScanEvent.Message("SDP_REQUEST", "Accepted=$accepted", address))
        } catch (e: IllegalArgumentException) {
            emit(BluetoothScanEvent.Message("SDP_ERROR", "Invalid address: ${e.message}", address))
        } catch (e: SecurityException) {
            emit(BluetoothScanEvent.Message("PERMISSION", e.message ?: "Access denied", address))
        }
    }

    @SuppressLint("MissingPermission")
    fun cancelDiscovery() {
        if (!permissions.canCancelDiscovery()) return
        try { if (adapter?.isDiscovering == true) adapter.cancelDiscovery() }
        catch (_: SecurityException) { }
    }

    @SuppressLint("MissingPermission")
    fun stop() {
        cancelDiscovery()
        if (registered) {
            appContext.unregisterReceiver(receiver)
            registered = false
        }
        onEvent = null
    }
}
