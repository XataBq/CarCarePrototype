package com.example.carcareprototype.car

import com.example.carcareprototype.presentation.state.DiagnosticState
import org.junit.Assert.*
import org.junit.Test

class CarScreenStateTest {
    private val mileage = MileageState(requestStatus = "SUCCESS", lastSuccess = MileageReading(45.2, 1000, "a"), receivedInCurrentAttempt = true)

    @Test fun logsDoNotInvalidateCarTemplate() {
        val state = DiagnosticState(mileage = mileage)
        assertEquals(state.toCarScreenState(), state.copy(logs = listOf("event")).toCarScreenState())
    }

    @Test fun receiptTimeAndFreshnessAreVisible() {
        val state = DiagnosticState(mileage = mileage)
        assertNotEquals(state.toCarScreenState(), state.copy(mileage = mileage.copy(lastSuccess = mileage.lastSuccess!!.copy(receivedAtMillis = 5000))).toCarScreenState())
        assertNotEquals(state.toCarScreenState(), state.copy(mileage = mileage.copy(receivedInCurrentAttempt = false)).toCarScreenState())
    }

    @Test fun timeoutDoesNotHideLastReplyOrLastReading() {
        val reply = MileageReply(null, 0, 0, 2000, 2000, 2000000)
        val visible = DiagnosticState(mileage = mileage.copy(requestStatus = "NO_VALID_DATA_TIMEOUT", lastReply = reply, receivedInCurrentAttempt = false)).toCarScreenState()
        assertEquals("NO_VALID_DATA_TIMEOUT", visible.mileageStatus)
        assertEquals("UNKNOWN: null km", visible.responseText)
        assertTrue(visible.mileageText.endsWith(" km"))
        assertEquals("PREVIOUS_READING", visible.freshness)
    }

    @Test fun missingMileageHasNoClaimOfOdometer() {
        val visible = DiagnosticState().toCarScreenState()
        assertEquals("No data", visible.mileageText)
        assertEquals("Waiting for car host", visible.connectionText)
        assertEquals("NOT_REQUESTED", visible.mileageStatus)
        assertEquals("NO_READING", visible.freshness)
    }
}
