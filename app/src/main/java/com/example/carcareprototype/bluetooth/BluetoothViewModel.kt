package com.example.carcareprototype.bluetooth

import android.app.Application
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class BluetoothViewModel internal constructor(
    private val repository: BluetoothDiagnostics,
) : ViewModel() {
    val uiState = repository.state

    fun start() = repository.start()
    fun stop() = repository.stop()
    fun refreshPaired() = repository.refreshPaired()
    fun scanClassic() = repository.discover()
    fun scanBle() = repository.scanBle()
    fun requestSdp(address: String) = repository.requestSdp(address)
    fun connectSpp(address: String) = repository.connectSpp(address)
    fun connectGatt(address: String) = repository.connectGatt(address)
    fun disconnect() = repository.disconnect()
    fun clearHistory() = repository.clearHistory()
    fun exportHistory(uri: Uri) = repository.exportHistory(uri)

    override fun onCleared() {
        repository.close()
        super.onCleared()
    }

    companion object {
        fun factory(application: Application): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    require(modelClass.isAssignableFrom(BluetoothViewModel::class.java))
                    @Suppress("UNCHECKED_CAST")
                    return BluetoothViewModel(BluetoothRepository(application)) as T
                }
            }
    }
}
