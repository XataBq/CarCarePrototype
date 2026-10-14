package com.example.carcareprototype.car

import com.example.carcareprototype.presentation.state.DiagnosticState
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class CarScreenStateTest {
    @Test
    fun logsAndTimestampDoNotChangeVisibleState() {
        val state = DiagnosticState(mileageKm = 100.0, mileageStatus = "SUCCESS")
        assertEquals(
            state.toCarScreenState(),
            state.copy(logs = listOf("Manual event"), mileageTimestampMillis = 42L).toCarScreenState()
        )
    }

    @Test
    fun onlyChangesToDisplayedFieldsEmitNewState() = runBlocking {
        val state = DiagnosticState(mileageKm = 100.01, mileageStatus = "SUCCESS")
        val visible = flowOf(
            state,
            state.copy(logs = listOf("Manual event")),
            state.copy(mileageTimestampMillis = 42L),
            state.copy(mileageKm = 100.02),
            state.copy(mileageKm = 101.0),
            state.copy(mileageStatus = "UNAVAILABLE"),
            state.copy(androidAutoConnected = true)
        ).map { it.toCarScreenState() }.distinctUntilChanged().toList()
        assertEquals(4, visible.size)
        assertNotEquals(visible[0].mileageText, visible[1].mileageText)
        assertEquals("UNAVAILABLE", visible[2].mileageStatus)
        assertEquals("Car session active", visible[3].connectionText)
    }

    @Test
    fun missingMileageKeepsExistingDisplayText() {
        val visible = DiagnosticState().toCarScreenState()
        assertEquals("No data", visible.mileageText)
        assertEquals("Waiting for car host", visible.connectionText)
        assertEquals("NOT_REQUESTED", visible.mileageStatus)
    }
}
