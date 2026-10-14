package com.example.carcareprototype.bluetooth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BluetoothLinkReducerTest {
    private val services = listOf(GattServiceInfo("180f", emptyList()))
    private val ready = BluetoothLinkState(LinkMode.GATT, LinkPhase.READY, "A", services)

    private fun phase(mode: LinkMode, phase: LinkPhase, address: String = "A") =
        BluetoothLinkEvent.Phase(mode, address, phase, "test")

    @Test
    fun rfcommCloseAfterFailurePreservesError() {
        val connected = BluetoothLinkState(LinkMode.RFCOMM, LinkPhase.CONNECTED, "A")
        val failed = connected.reduce(BluetoothLinkEvent.Failure(LinkMode.RFCOMM, "A", "Read failed"))
        assertEquals(failed, failed.reduce(phase(LinkMode.RFCOMM, LinkPhase.IDLE)))
        assertEquals(LinkPhase.FAILED, failed.phase)
        assertEquals("Read failed", failed.error)
    }

    @Test
    fun remoteGattDisconnectClearsServices() {
        val closed = ready.reduce(phase(LinkMode.GATT, LinkPhase.IDLE))
        assertEquals(LinkPhase.IDLE, closed.phase)
        assertTrue(closed.services.isEmpty())
    }

    @Test
    fun everyNewAttemptClearsPreviousServicesEvenForSameAddress() {
        for (address in listOf("A", "B")) {
            val connecting = ready.reduce(phase(LinkMode.GATT, LinkPhase.CONNECTING, address))
            assertEquals(address, connecting.address)
            assertTrue(connecting.services.isEmpty())
        }
    }

    @Test
    fun serviceDiscoveryKeepsNewServicesWhenBecomingReady() {
        val connected = BluetoothLinkState().reduce(phase(LinkMode.GATT, LinkPhase.CONNECTING))
        val discovering = connected.reduce(phase(LinkMode.GATT, LinkPhase.DISCOVERING))
        val discovered = discovering.reduce(BluetoothLinkEvent.GattServices("A", services))
        val result = discovered.reduce(phase(LinkMode.GATT, LinkPhase.READY))
        assertEquals(LinkPhase.READY, result.phase)
        assertEquals(services, result.services)
    }

    @Test
    fun callbacksFromAnotherDeviceDoNotChangeActiveConnection() {
        assertEquals(ready, ready.reduce(phase(LinkMode.GATT, LinkPhase.IDLE, "B")))
        assertEquals(ready, ready.reduce(BluetoothLinkEvent.GattServices("B", emptyList())))
        assertEquals(ready, ready.reduce(BluetoothLinkEvent.Failure(LinkMode.RFCOMM, "B", "Failed")))
    }

    @Test
    fun lateDiscoveryCallbacksDoNotReviveClosedOrFailedLink() {
        val closed = ready.reduce(phase(LinkMode.GATT, LinkPhase.IDLE))
        assertEquals(closed, closed.reduce(BluetoothLinkEvent.GattServices("A", services)))
        assertEquals(closed, closed.reduce(phase(LinkMode.GATT, LinkPhase.READY)))
        val failed = ready.reduce(BluetoothLinkEvent.Failure(LinkMode.GATT, "A", "Failed"))
        assertEquals(failed, failed.reduce(BluetoothLinkEvent.GattServices("A", services)))
        assertTrue(failed.services.isEmpty())
    }

    @Test
    fun newAttemptClearsPreviousErrorAndPreflightFailureIsVisible() {
        val failed = BluetoothLinkState().reduce(
            BluetoothLinkEvent.Failure(LinkMode.RFCOMM, "A", "Permission missing")
        )
        assertEquals(LinkPhase.FAILED, failed.phase)
        val connecting = failed.reduce(phase(LinkMode.RFCOMM, LinkPhase.CONNECTING))
        assertEquals(LinkPhase.CONNECTING, connecting.phase)
        assertEquals(null, connecting.error)
    }
}
