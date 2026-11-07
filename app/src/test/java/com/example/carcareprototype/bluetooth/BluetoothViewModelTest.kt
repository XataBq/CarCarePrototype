package com.example.carcareprototype.bluetooth

import android.net.Uri
import androidx.lifecycle.ViewModelStore
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class BluetoothViewModelTest {
    private class FakeDiagnostics : BluetoothDiagnostics {
        override val state = MutableStateFlow(BluetoothUiState())
        val calls = mutableListOf<String>()
        override fun start() { calls += "start" }
        override fun stop() { calls += "stop" }
        override fun refreshPaired() { calls += "paired" }
        override fun discover() { calls += "classic" }
        override fun requestSdp(address: String) { calls += "sdp:$address" }
        override fun scanBle() { calls += "ble" }
        override fun connectSpp(address: String) { calls += "spp:$address" }
        override fun connectGatt(address: String) { calls += "gatt:$address" }
        override fun disconnect() { calls += "disconnect" }
        override fun clearHistory() { calls += "clear" }
        override fun exportHistory(uri: Uri) { calls += "export" }
        override fun close() { calls += "close" }
    }

    @Test fun repositoryRemainsTheSingleStateSourceWithoutSubscriptionDelays() {
        val repository = FakeDiagnostics()
        val vm = BluetoothViewModel(repository)
        assertSame(repository.state, vm.uiState)
        val update = BluetoothUiState(link = BluetoothLinkState(phase = LinkPhase.FAILED, error = "timeout"))
        repository.state.value = update
        assertEquals(update, vm.uiState.value)
    }

    @Test fun clearingViewModelClosesOwnerOnceWithoutSeparateDuplicateStop() {
        val repository = FakeDiagnostics()
        val store = ViewModelStore()
        store.put("bluetooth", BluetoothViewModel(repository))
        store.clear()
        assertEquals(listOf("close"), repository.calls)
    }
}
