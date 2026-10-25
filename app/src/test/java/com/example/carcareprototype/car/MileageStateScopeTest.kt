package com.example.carcareprototype.car

import org.junit.Assert.*
import org.junit.Test

class MileageStateScopeTest {
    private val reading = MileageState("SUCCESS", "attempt-a",
        lastSuccess = MileageReading(45.2, 1000, "attempt-a"), receivedInCurrentAttempt = true)

    @Test fun queuedOldCarReadingCannotPopulateNewCar() {
        val scope = MileageStateScope()
        scope.connect("connection", "Tiguan")
        assertTrue(scope.update("connection", 0, 0, reading))
        scope.selectCar("Kia")
        assertFalse(scope.update("connection", 0, 1, reading))
        assertEquals("Kia", scope.carId)
        assertNull(scope.state.lastSuccess)
        assertTrue(scope.update("connection", 1, 1, MileageState("UNKNOWN")))
        assertNull(scope.state.lastSuccess)
    }

    @Test fun multipleSelectionsRejectIntermediateQueuedAttempts() {
        val scope = MileageStateScope(); scope.connect("connection", "A")
        scope.selectCar("B"); scope.selectCar("A")
        assertFalse(scope.update("connection", 1, 2, reading))
        assertFalse(scope.update("connection", 0, 2, reading))
        assertNull(scope.state.lastSuccess)
    }

    @Test fun sameManualIdReconnectNeverRestoresReading() {
        val scope = MileageStateScope(); scope.connect("old", "Tiguan")
        scope.update("old", 0, 0, reading)
        scope.disconnect("old"); scope.connect("new", "Tiguan")
        assertNull(scope.state.lastSuccess)
        assertFalse(scope.update("old", 0, 0, reading))
        assertNull(scope.state.lastSuccess)
    }

    @Test fun unidentifiedReadingsAreConnectionScoped() {
        val scope = MileageStateScope(); scope.connect("old", null)
        scope.update("old", 0, 0, reading)
        scope.connect("new", null)
        assertNull(scope.state.lastSuccess)
        scope.disconnect("old")
        assertTrue(scope.update("new", 0, 0, reading))
    }

    @Test fun stoppedAndResumedSameConnectionKeepsPreviousReading() {
        val scope = MileageStateScope(); scope.connect("same", "Tiguan")
        scope.update("same", 0, 0, reading)
        scope.update("same", 0, 0, reading.copy(requestStatus = "STOPPED", receivedInCurrentAttempt = false))
        scope.update("same", 0, 0, scope.state.copy(requestStatus = "SUBSCRIBING"))
        assertEquals(reading.lastSuccess, scope.state.lastSuccess)
        assertFalse(scope.state.receivedInCurrentAttempt)
    }
}
