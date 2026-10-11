package com.example.carcareprototype.bluetooth

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import java.io.IOException
import java.util.UUID
class RfcommClient(context: Context) {
    private val appContext = context.applicationContext
    private val adapter = appContext.getSystemService(BluetoothManager::class.java)?.adapter
    private val lock = Any()
    private class Connection(val address: String) {
        @Volatile var socket: BluetoothSocket? = null
        @Volatile var stopped = false
    }
    private var current: Connection? = null

    private fun hasConnectPermission() = Build.VERSION.SDK_INT < 31 ||
        ContextCompat.checkSelfPermission(appContext, Manifest.permission.BLUETOOTH_CONNECT) ==
        PackageManager.PERMISSION_GRANTED

    private fun hasScanPermission() = if (Build.VERSION.SDK_INT >= 31) {
        ContextCompat.checkSelfPermission(appContext, Manifest.permission.BLUETOOTH_SCAN) ==
        PackageManager.PERMISSION_GRANTED
    } else true

    @SuppressLint("MissingPermission")
    fun connect(address: String, onEvent: (BluetoothLinkEvent) -> Unit) {
        if (!hasConnectPermission() || !hasScanPermission()) {
            onEvent(BluetoothLinkEvent.Failure(LinkMode.RFCOMM, address, "Bluetooth permission missing"))
            return
        }
        val bt = adapter
        if (bt == null || !bt.isEnabled) {
            onEvent(BluetoothLinkEvent.Failure(LinkMode.RFCOMM, address, "Bluetooth disabled"))
            return
        }
        val connection = synchronized(lock) {
            if (current != null) return
            Connection(address).also { current = it }
        }
        onEvent(BluetoothLinkEvent.Phase(LinkMode.RFCOMM, address, LinkPhase.CONNECTING, "Opening secure SPP RFCOMM"))
        Thread({
            var socket: BluetoothSocket? = null
            try {
                val device = bt.getRemoteDevice(address)
                check(device.bondState == BluetoothDevice.BOND_BONDED) {
                    "SPP connection requires a paired device"
                }
                if (bt.isDiscovering) bt.cancelDiscovery()
                val opened = device.createRfcommSocketToServiceRecord(SPP_UUID)
                socket = opened
                synchronized(lock) {
                    if (connection.stopped) opened.close()
                    else connection.socket = opened
                }
                if (connection.stopped) return@Thread
                opened.connect()
                if (connection.stopped) return@Thread
                onEvent(BluetoothLinkEvent.Phase(LinkMode.RFCOMM, address, LinkPhase.CONNECTED, "SPP connected; passively reading only"))
                val input = opened.inputStream
                val buffer = ByteArray(256)
                var previews = 0
                while (!connection.stopped) {
                    val count = input.read(buffer)
                    if (count < 0) break
                    if (count == 0) continue
                    if (previews++ < 20) {
                        val preview = buffer.take(minOf(count, 64)).joinToString(" ") {
                            "%02X".format(it.toInt() and 0xFF)
                        }
                        onEvent(BluetoothLinkEvent.IncomingBytes(address, count, preview))
                    }
                }
            } catch (e: Exception) {
                if (!connection.stopped) {
                    onEvent(
                        BluetoothLinkEvent.Failure(
                        LinkMode.RFCOMM, address, "${e.javaClass.simpleName}: ${e.message}"
                    ))
                }
            } finally {
                try { socket?.close() } catch (_: IOException) { }
                val wasCurrent = synchronized(lock) {
                    if (current === connection) { current = null; true } else false
                }
                if (wasCurrent && !connection.stopped) {
                    onEvent(BluetoothLinkEvent.Phase(LinkMode.RFCOMM, address, LinkPhase.IDLE, "SPP closed"))
                }
            }
        }, "CarTester-RFCOMM").start()
    }

    fun stop() {
        val connection = synchronized(lock) {
            current?.also { it.stopped = true }.also { current = null }
        }
        try { connection?.socket?.close() } catch (_: IOException) { }
    }

    companion object {
        const val SPP_UUID_TEXT = "00001101-0000-1000-8000-00805f9b34fb"
        val SPP_UUID: UUID = UUID.fromString(SPP_UUID_TEXT)
    }
}
