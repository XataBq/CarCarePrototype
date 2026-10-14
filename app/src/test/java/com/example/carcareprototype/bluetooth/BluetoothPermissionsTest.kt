package com.example.carcareprototype.bluetooth

import android.Manifest
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BluetoothPermissionsTest {
    @Test
    fun legacyAndroidAllowsConnectionAndCancellationWithoutLocation() {
        for (sdkInt in listOf(29, 30)) {
            val permissions = BluetoothPermissions(sdkInt) { false }

            assertTrue(permissions.canConnect())
            assertTrue(permissions.canCancelDiscovery())
            assertFalse(permissions.canScan())
            assertArrayEquals(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                permissions.requiredPermissions
            )
        }
    }

    @Test
    fun legacyDeviceSearchRequiresFineLocation() {
        val granted = mutableSetOf(Manifest.permission.ACCESS_COARSE_LOCATION)
        val permissions = BluetoothPermissions(30, granted::contains)

        assertFalse(permissions.canScan())
        granted.add(Manifest.permission.ACCESS_FINE_LOCATION)
        assertTrue(permissions.canScan())
    }

    @Test
    fun modernAndroidChecksConnectAndScanIndependently() {
        for (sdkInt in listOf(31, 36)) {
            val connectOnly = BluetoothPermissions(sdkInt) {
                it == Manifest.permission.BLUETOOTH_CONNECT
            }
            assertTrue(connectOnly.canConnect())
            assertFalse(connectOnly.canScan())
            assertFalse(connectOnly.canCancelDiscovery())

            val scanOnly = BluetoothPermissions(sdkInt) {
                it == Manifest.permission.BLUETOOTH_SCAN
            }
            assertFalse(scanOnly.canConnect())
            assertTrue(scanOnly.canScan())
            assertTrue(scanOnly.canCancelDiscovery())
            assertArrayEquals(
                arrayOf(Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN),
                scanOnly.requiredPermissions
            )
        }
    }

    @Test
    fun modernAndroidLocationGrantDoesNotAuthorizeBluetooth() {
        val permissions = BluetoothPermissions(31) {
            it == Manifest.permission.ACCESS_FINE_LOCATION
        }

        assertFalse(permissions.canConnect())
        assertFalse(permissions.canScan())
        assertFalse(permissions.canCancelDiscovery())
    }

    @Test
    fun permissionChangesAreCheckedOnEveryOperation() {
        val granted = mutableSetOf<String>()
        val permissions = BluetoothPermissions(31, granted::contains)

        assertFalse(permissions.canConnect())
        assertFalse(permissions.canScan())

        granted.addAll(permissions.requiredPermissions)
        assertTrue(permissions.canConnect())
        assertTrue(permissions.canScan())
        assertTrue(permissions.canCancelDiscovery())

        granted.clear()
        assertFalse(permissions.canConnect())
        assertFalse(permissions.canScan())
        assertFalse(permissions.canCancelDiscovery())
    }
}
