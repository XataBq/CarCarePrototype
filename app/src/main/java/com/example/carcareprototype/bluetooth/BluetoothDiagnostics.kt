package com.example.carcareprototype.bluetooth

import android.net.Uri
import kotlinx.coroutines.flow.StateFlow

/** Presentation-facing operations; transport timing and ownership belong to the implementation. */
interface BluetoothDiagnostics {
    val state: StateFlow<BluetoothUiState>
    fun start()
    fun stop()
    fun refreshPaired()
    fun discover()
    fun requestSdp(address: String)
    fun scanBle()
    fun connectSpp(address: String)
    fun connectGatt(address: String)
    fun disconnect()
    fun clearHistory()
    fun exportHistory(uri: Uri)
    fun close()
}
