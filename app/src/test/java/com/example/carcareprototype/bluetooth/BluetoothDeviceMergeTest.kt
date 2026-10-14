package com.example.carcareprototype.bluetooth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class BluetoothDeviceMergeTest {
    private val classic = BluetoothDeviceInfo(
        name = "Adapter", address = "A", bonded = true, type = BluetoothDeviceType.CLASSIC,
        rssi = -60, uuids = listOf(SPP_SERVICE_UUID), uuidSource = BluetoothUuidSource.CACHED
    )
    private val ble = BluetoothDeviceInfo(
        name = "Unknown device", address = "A", bonded = true, type = BluetoothDeviceType.BLE,
        bleAdvertisedUuids = listOf("180f"), observedBle = true
    )

    @Test
    fun bleObservationPreservesKnownClassicDataAndBecomesDual() {
        val merged = mergeBluetoothDevice(classic, ble)
        assertEquals("Adapter", merged.name)
        assertEquals(BluetoothDeviceType.DUAL, merged.type)
        assertEquals(-60, merged.rssi)
        assertEquals(classic.uuids, merged.uuids)
        assertEquals(ble.bleAdvertisedUuids, merged.bleAdvertisedUuids)
        assertTrue(merged.observedBle)
        assertTrue(merged.canConnectSpp)
        assertTrue(merged.canConnectGatt)
        assertEquals(BluetoothDeviceType.CLASSIC, classic.type)
    }

    @Test
    fun classicObservationPreservesBleCapabilityAndAdvertisement() {
        val merged = mergeBluetoothDevice(ble, classic)
        assertEquals(BluetoothDeviceType.DUAL, merged.type)
        assertTrue(merged.observedBle)
        assertEquals(ble.bleAdvertisedUuids, merged.bleAdvertisedUuids)
    }

    @Test
    fun authoritativeSdpResultIncludingEmptyListSurvivesCachedObservations() {
        for (uuids in listOf(emptyList(), listOf("110a"))) {
            val previous = classic.copy(uuids = uuids, uuidSource = BluetoothUuidSource.SDP)
            val merged = mergeBluetoothDevice(previous, classic)
            assertEquals(uuids, merged.uuids)
            assertEquals(BluetoothUuidSource.SDP, merged.uuidSource)
        }
    }

    @Test
    fun rssiUpdateKeepsPositionAndDoesNotProduceDiscoveryLog() {
        val other = classic.copy(address = "B")
        val devices = listOf(classic, other)
        val result = updateBluetoothDevices(devices, classic.copy(rssi = -40))
        assertEquals(listOf("A", "B"), result.devices.map { it.address })
        assertEquals(-40, result.devices.first().rssi)
        assertEquals(-60, devices.first().rssi)
        assertFalse(result.meaningfulChange)
    }

    @Test
    fun identicalObservationReusesList() {
        val devices = listOf(classic)
        val result = updateBluetoothDevices(devices, classic)
        assertSame(devices, result.devices)
        assertFalse(result.meaningfulChange)
    }

    @Test
    fun nameAndBondChangesUpdateSortOrder() {
        val other = classic.copy(name = "Beta", address = "B")
        val renamed = updateBluetoothDevices(listOf(classic, other), classic.copy(name = "Zulu"))
        assertEquals(listOf("B", "A"), renamed.devices.map { it.address })
        assertTrue(renamed.meaningfulChange)
        val unpaired = updateBluetoothDevices(listOf(classic, other), classic.copy(bonded = false))
        assertEquals(listOf("B", "A"), unpaired.devices.map { it.address })
    }

    @Test
    fun newDeviceAndLogDataUseMergedInformation() {
        val added = updateBluetoothDevices(emptyList(), classic)
        assertTrue(added.meaningfulChange)
        val result = updateBluetoothDevices(added.devices, ble)
        assertTrue(result.meaningfulChange)
        assertEquals(result.devices.single(), result.device)
        assertEquals("Adapter", result.device.name)
        assertEquals(BluetoothDeviceType.DUAL, result.device.type)
        assertEquals(classic.uuids, result.device.uuids)
    }
}
