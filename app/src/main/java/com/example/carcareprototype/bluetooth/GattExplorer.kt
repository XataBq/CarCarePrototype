package com.example.carcareprototype.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.BluetoothDevice
import android.content.Context

class GattExplorer(context: Context) {
    private val appContext = context.applicationContext
    private val permissions = BluetoothPermissions(appContext)
    private val adapter = appContext.getSystemService(BluetoothManager::class.java)?.adapter
    private val lock = Any()
    private class Connection(val address: String) {
        @Volatile var gatt: BluetoothGatt? = null
        @Volatile var closed = false
    }
    private var current: Connection? = null

    @SuppressLint("MissingPermission")
    fun connect(address: String, onEvent: (BluetoothLinkEvent) -> Unit) {
        if (!permissions.canConnect()) {
            onEvent(BluetoothLinkEvent.Failure(LinkMode.GATT, address, "Bluetooth CONNECT permission missing"))
            return
        }
        val bt = adapter
        if (bt?.isEnabled != true) {
            onEvent(BluetoothLinkEvent.Failure(LinkMode.GATT, address, "Bluetooth disabled"))
            return
        }
        val session = synchronized(lock) {
            if (current != null) return
            Connection(address).also { current = it }
        }
        onEvent(BluetoothLinkEvent.Phase(LinkMode.GATT, address, LinkPhase.CONNECTING, "Connecting GATT"))

        val callback = object : BluetoothGattCallback() {
            override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
                if (session.closed) { gatt.close(); return }
                if (status != BluetoothGatt.GATT_SUCCESS) {
                    onEvent(BluetoothLinkEvent.Failure(LinkMode.GATT, address, "GATT connection error: $status"))
                    stopSession(session)
                    return
                }
                when (newState) {
                    BluetoothProfile.STATE_CONNECTED -> {
                        onEvent(BluetoothLinkEvent.Phase(LinkMode.GATT, address, LinkPhase.DISCOVERING, "Connected, discovering services"))
                        try {
                            if (!gatt.discoverServices()) {
                                onEvent(BluetoothLinkEvent.Failure(LinkMode.GATT, address, "discoverServices() returned false"))
                                stopSession(session)
                            }
                        } catch (e: SecurityException) {
                            onEvent(BluetoothLinkEvent.Failure(LinkMode.GATT, address, "Permission: ${e.message}"))
                            stopSession(session)
                        }
                    }
                    BluetoothProfile.STATE_DISCONNECTED -> {
                        onEvent(BluetoothLinkEvent.Phase(LinkMode.GATT, address, LinkPhase.IDLE, "GATT disconnected"))
                        stopSession(session)
                    }
                }
            }

            override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
                if (session.closed) return
                if (status != BluetoothGatt.GATT_SUCCESS) {
                    onEvent(BluetoothLinkEvent.Failure(LinkMode.GATT, address, "Service discovery failed: $status"))
                    stopSession(session)
                    return
                }
                val services = gatt.services.map { service ->
                    GattServiceInfo(
                        uuid = service.uuid.toString(),
                        characteristics = service.characteristics.map { char ->
                            GattCharacteristicInfo(char.uuid.toString(), propertiesString(char.properties))
                        }
                    )
                }
                onEvent(BluetoothLinkEvent.GattServices(address, services))
                onEvent(BluetoothLinkEvent.Phase(LinkMode.GATT, address, LinkPhase.READY, "Found ${services.size} GATT services"))
            }
        }

        try {
            val device = bt.getRemoteDevice(address)
            val gatt = device.connectGatt(appContext, false, callback, BluetoothDevice.TRANSPORT_LE)
            synchronized(lock) {
                if (session.closed) gatt?.close() else session.gatt = gatt
            }
            if (gatt == null) {
                onEvent(BluetoothLinkEvent.Failure(LinkMode.GATT, address, "connectGatt returned null"))
                stopSession(session)
            }
        } catch (e: Exception) {
            onEvent(BluetoothLinkEvent.Failure(LinkMode.GATT, address, "${e.javaClass.simpleName}: ${e.message}"))
            stopSession(session)
        }
    }

    @SuppressLint("MissingPermission")
    private fun stopSession(session: Connection) {
        val gatt = synchronized(lock) {
            session.closed = true
            if (current === session) current = null
            session.gatt.also { session.gatt = null }
        }
        try { gatt?.disconnect() } catch (_: SecurityException) { }
        gatt?.close()
    }

    fun stop() {
        val session = synchronized(lock) { current } ?: return
        stopSession(session)
    }

    private fun propertiesString(value: Int): String = buildList {
        if (value and BluetoothGattCharacteristic.PROPERTY_READ != 0) add("READ")
        if (value and BluetoothGattCharacteristic.PROPERTY_WRITE != 0) add("WRITE")
        if (value and BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE != 0) add("WRITE_NO_RESPONSE")
        if (value and BluetoothGattCharacteristic.PROPERTY_NOTIFY != 0) add("NOTIFY")
        if (value and BluetoothGattCharacteristic.PROPERTY_INDICATE != 0) add("INDICATE")
    }.joinToString("|").ifEmpty { "OTHER" }
}
