package com.example.carcareprototype.bluetooth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BluetoothDeviceRulesTest {
    private val device = BluetoothDeviceInfo("Adapter", "A", true, BluetoothDeviceType.CLASSIC)

    @Test
    fun sppRequiresBothBondAndStandardService() {
        val spp = device.copy(uuids = listOf(SPP_SERVICE_UUID))
        assertTrue(spp.canConnectSpp)
        assertFalse(spp.copy(bonded = false).canConnectSpp)
        assertFalse(device.canConnectSpp)
        assertFalse(device.copy(uuids = listOf("00001102-0000-1000-8000-00805f9b34fb")).canConnectSpp)
    }

    @Test
    fun gattUsesDeviceTypeOrObservedBleCapability() {
        assertFalse(device.canConnectGatt)
        assertFalse(device.copy(type = BluetoothDeviceType.UNKNOWN).canConnectGatt)
        assertTrue(device.copy(type = BluetoothDeviceType.BLE).canConnectGatt)
        assertTrue(device.copy(type = BluetoothDeviceType.DUAL).canConnectGatt)
        assertTrue(device.copy(observedBle = true).canConnectGatt)
    }

    @Test
    fun activeConnectionPhasesBlockAnotherConnection() {
        val active = listOf(LinkPhase.CONNECTING, LinkPhase.CONNECTED, LinkPhase.DISCOVERING, LinkPhase.READY)
        for (phase in LinkPhase.entries) {
            assertEquals(phase.toString(), phase in active, BluetoothLinkState(phase = phase).isBusy)
        }
    }

    @Test
    fun enumLabelsPreserveDisplayedValues() {
        assertEquals(listOf("Classic", "BLE", "Dual", "Unknown"), BluetoothDeviceType.entries.map { it.toString() })
        assertEquals(listOf("NONE", "CACHED", "SDP"), BluetoothUuidSource.entries.map { it.toString() })
    }
}
