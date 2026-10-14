package com.example.carcareprototype.bluetooth.uuids

import org.junit.Assert.assertEquals
import org.junit.Test

class BluetoothUuidCatalogTest {
    @Test
    fun classicCatalogResolvesSpp() {
        val meaning = BluetoothUuidCatalog.classic("00001101-0000-1000-8000-00805f9b34fb")

        assertEquals("Serial Port (SPP)", meaning?.name)
        assertEquals(UuidKind.CLASSIC_SERVICE, meaning?.kind)
    }

    @Test
    fun characteristicCatalogResolvesDeviceName() {
        val meaning = BluetoothUuidCatalog.gattCharacteristic("1800", "2A00")

        assertEquals("Device Name", meaning?.name)
        assertEquals(UuidKind.GATT_CHARACTERISTIC, meaning?.kind)
    }
}
