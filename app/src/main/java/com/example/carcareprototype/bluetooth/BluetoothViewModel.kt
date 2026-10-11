package com.example.carcareprototype.bluetooth

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BluetoothViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = BluetoothRepository(application)
    private var scanTimeout: Job? = null
    private var connectionTimeout: Job? = null

    val uiState: StateFlow<BluetoothUiState> = combine(
        repository.devices,
        repository.adapterState,
        repository.records,
        repository.link
    ) { devices, adapterState, records, link ->
        BluetoothUiState(
            devices = devices,
            adapterState = adapterState,
            records = records,
            link = link
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), BluetoothUiState())

    fun start() = repository.start()

    fun stop() {
        scanTimeout?.cancel()
        connectionTimeout?.cancel()
        repository.stop()
    }

    fun refreshPaired() = repository.refreshPaired()
    fun scanClassic() {
        scanTimeout?.cancel()
        repository.discover()
    }

    fun scanBle() {
        repository.scanBle()
        scanTimeout?.cancel()
        scanTimeout = viewModelScope.launch {
            delay(12_000)
            repository.stopBleScan()
        }
    }

    fun requestSdp(address: String) = repository.requestSdp(address)

    fun connectSpp(address: String) {
        repository.connectSpp(address)
        scheduleConnectionTimeout()
    }

    fun connectGatt(address: String) {
        repository.connectGatt(address)
        scheduleConnectionTimeout()
    }

    private fun scheduleConnectionTimeout() {
        connectionTimeout?.cancel()
        connectionTimeout = viewModelScope.launch {
            delay(20_000)
            repository.abortTimedOutLink()
        }
    }

    fun disconnect() {
        connectionTimeout?.cancel()
        repository.disconnect()
    }

    fun clearHistory() = repository.clearHistory()
    fun exportHistory(uri: Uri) = repository.exportHistory(uri)

    override fun onCleared() {
        stop()
        repository.close()
        super.onCleared()
    }
}
