package com.example.carcareprototype.store

import android.net.Uri
import com.example.carcareprototype.bluetooth.BluetoothRecord

sealed interface HistoryAction {
    data class Append(val record: BluetoothRecord) : HistoryAction
    data object Clear : HistoryAction
    data class Export(val uri: Uri) : HistoryAction
}