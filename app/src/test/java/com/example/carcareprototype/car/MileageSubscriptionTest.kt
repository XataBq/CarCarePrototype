package com.example.carcareprototype.car

import org.junit.Assert.*
import org.junit.Test

class MileageSubscriptionTest {
    private class Fixture : MileageBackend, MileageScheduler {
        var now = 100L
        var permission = true
        var current = true
        var live = 0
        var maximumLive = 0
        var removeFails = false
        var registrationError: Exception? = null
        val callbacks = mutableListOf<(MileageReply) -> Unit>()
        val timers = mutableListOf<Timer>()
        val logs = mutableListOf<String>()
        val states = mutableListOf<MileageState>()
        data class Timer(val due: Long, val action: () -> Unit, var cancelled: Boolean = false)
        val subject = MileageSubscription(this, this, { now }, { 1_000_000 + now },
            { permission }, { current }, { state, message -> states += state; logs += message }, logs::add)
        override fun register(callback: (MileageReply) -> Unit) {
            callbacks += callback
            live++
            maximumLive = maxOf(maximumLive, live)
            registrationError?.let { throw it }
        }
        override fun remove() {
            if (removeFails) error("host remove failed")
            live--
        }
        override fun schedule(delayMillis: Long, action: () -> Unit): () -> Unit {
            val timer = Timer(now + delayMillis, action)
            timers += timer
            return { timer.cancelled = true }
        }
        fun advance(ms: Long = MileageSubscription.CALLBACK_TIMEOUT_MS) {
            now += ms
            timers.filter { !it.cancelled && it.due <= now }.toList().forEach {
                it.cancelled = true
                it.action()
            }
        }
        fun reply(status: Int = 1, value: Double? = 45.2, index: Int = callbacks.lastIndex, stamp: Long = 877712202363415) {
            callbacks[index](MileageReply(value, status, stamp, 1_000_000 + now, now, now * 1_000_000))
        }
        fun start() = subject.start("ON_START")
    }

    @Test fun firstSuccessPreservesUnitsAndRawMetadata() {
        val f = Fixture(); f.start(); f.reply()
        assertEquals(45.2, f.subject.state.lastSuccess!!.valueKm, 0.0)
        assertEquals(1_000_100, f.subject.state.lastSuccess!!.receivedAtMillis)
        assertEquals(877712202363415, f.subject.state.lastReply!!.rawTimestamp)
        assertEquals("SUCCESS", f.subject.state.requestStatus)
        assertEquals("UNVERIFIED", f.subject.state.verification)
        assertTrue(f.subject.state.receivedInCurrentAttempt)
        f.advance()
        assertEquals("SUCCESS", f.subject.state.requestStatus)
    }

    @Test fun unknownIsPreservedBeyondNoValidDataTimeout() {
        val f = Fixture(); f.start(); f.reply(0, null)
        assertEquals("UNKNOWN", f.subject.state.requestStatus)
        f.advance()
        assertEquals("NO_VALID_DATA_TIMEOUT", f.subject.state.requestStatus)
        assertEquals("UNKNOWN", f.subject.state.lastReply!!.status)
        assertNull(f.subject.state.lastSuccess)
        assertEquals(1, f.live)
    }

    @Test fun unavailableAndUnimplementedKeepOriginalStatuses() {
        for ((raw, name) in listOf(2 to "UNIMPLEMENTED", 3 to "UNAVAILABLE")) {
            val f = Fixture(); f.start(); f.reply(raw, null)
            assertEquals(name, f.subject.state.requestStatus)
            f.advance()
            assertEquals(name, f.subject.state.lastReply!!.status)
            assertEquals("NO_VALID_DATA_TIMEOUT", f.subject.state.requestStatus)
        }
    }

    @Test fun noCallbackTimeoutHasExactBoundaryAndKeepsListener() {
        val f = Fixture(); f.start(); f.advance(14_999)
        assertEquals("SUBSCRIBING", f.subject.state.requestStatus)
        f.advance(1)
        assertEquals("NO_CALLBACK_TIMEOUT", f.subject.state.requestStatus)
        assertNull(f.subject.state.lastReply)
        assertEquals(1, f.live)
        assertEquals(1, f.callbacks.size)
    }

    @Test fun lateCallbackRecoversWithoutReregistering() {
        val f = Fixture(); f.start(); f.advance(); f.reply()
        assertEquals("SUCCESS", f.subject.state.requestStatus)
        assertEquals(1, f.callbacks.size)
    }

    @Test fun lateInvalidCallbackUpdatesRawResponseWithoutHidingTimeout() {
        val f = Fixture(); f.start(); f.advance(); f.reply(0, null)
        assertEquals("NO_VALID_DATA_TIMEOUT", f.subject.state.requestStatus)
        assertEquals(0, f.subject.state.lastReply!!.rawStatus)
    }

    @Test fun stopStartRetainsReadingButWaitsForNewData() {
        val f = Fixture(); f.start(); f.reply()
        val reading = f.subject.state.lastSuccess
        f.subject.stop(); assertEquals(0, f.live)
        f.start()
        assertEquals(reading, f.subject.state.lastSuccess)
        assertFalse(f.subject.state.receivedInCurrentAttempt)
        assertEquals("SUBSCRIBING", f.subject.state.requestStatus)
        assertEquals(2, f.callbacks.size)
        f.advance()
        assertEquals(reading, f.subject.state.lastSuccess)
    }

    @Test fun rapidLifecycleChangesNeverOverlapListeners() {
        val f = Fixture()
        repeat(30) { f.start(); f.start(); f.subject.stop() }
        assertEquals(1, f.maximumLive)
        assertEquals(0, f.live)
        f.advance()
        assertEquals("STOPPED", f.subject.state.requestStatus)
    }

    @Test fun retryReplacesListenerAndCoalescesClicksWhileWaiting() {
        val f = Fixture(); f.start(); f.reply()
        val old = f.subject.state.attemptId
        repeat(20) { f.subject.retry() }
        assertNotEquals(old, f.subject.state.attemptId)
        assertEquals(2, f.callbacks.size)
        assertEquals(1, f.maximumLive)
        assertEquals(45.2, f.subject.state.lastSuccess!!.valueKm, 0.0)
    }

    @Test fun oldCallbackAndOldTimeoutCannotOverwriteNewAttempt() {
        val f = Fixture(); f.start(); f.reply(0, null)
        val timer = f.timers.single()
        f.subject.retry()
        f.reply(value = 122000.0)
        f.reply(value = 9.0, index = 0)
        timer.action() // Simulate cancellation racing with an already queued task.
        assertEquals(122000.0, f.subject.state.lastSuccess!!.valueKm, 0.0)
        assertEquals("SUCCESS", f.subject.state.requestStatus)
    }

    @Test fun invalidResponseAndPermissionErrorsDoNotEraseLastSuccess() {
        val f = Fixture(); f.start(); f.reply(); f.subject.retry(); f.reply(0, null)
        f.advance()
        assertEquals(45.2, f.subject.state.lastSuccess!!.valueKm, 0.0)
        f.permission = false; f.subject.retry()
        assertEquals("PERMISSION_REQUIRED", f.subject.state.requestStatus)
        assertEquals(45.2, f.subject.state.lastSuccess!!.valueKm, 0.0)
        assertFalse(f.subject.state.receivedInCurrentAttempt)
    }

    @Test fun carIdentityChangeClearsReadingAndInvalidatesOldCallback() {
        val f = Fixture(); f.start(); f.reply()
        f.current = false // Car ID selection invalidates immediately, before its collector resumes.
        f.reply(value = 999.0)
        assertEquals(45.2, f.subject.state.lastSuccess!!.valueKm, 0.0)
        f.current = true; f.subject.resetIdentity()
        assertNull(f.subject.state.lastSuccess)
        assertNull(f.subject.state.lastReply)
        f.reply(index = 0)
        assertNull(f.subject.state.lastSuccess)
        f.reply(value = 122000.0)
        assertEquals(122000.0, f.subject.state.lastSuccess!!.valueKm, 0.0)
    }

    @Test fun newConnectionNeverInheritsManualOrUnidentifiedReading() {
        val first = Fixture(); first.start(); first.reply(); first.subject.stop("ON_DESTROY")
        val second = Fixture(); second.start()
        assertNull(second.subject.state.lastSuccess)
    }

    @Test fun destructionCancelsTimeoutAndIgnoresCallback() {
        val f = Fixture(); f.start(); f.subject.stop("ON_DESTROY")
        assertTrue(f.timers.all { it.cancelled })
        f.advance(); f.reply()
        assertNull(f.subject.state.lastSuccess)
        assertEquals("STOPPED", f.subject.state.requestStatus)
    }

    @Test fun denialThenGrantSubscribesOnlyWhenPermitted() {
        val f = Fixture(); f.permission = false; f.start()
        assertEquals(0, f.callbacks.size)
        f.subject.permissionDenied()
        assertEquals("PERMISSION_DENIED", f.subject.state.requestStatus)
        f.subject.retry(); assertEquals(0, f.callbacks.size)
        f.permission = true; f.start(); f.reply()
        assertEquals("SUCCESS", f.subject.state.requestStatus)
        assertEquals(1, f.callbacks.size)
    }

    @Test fun retryWhileStoppedDoesNotRegister() {
        val f = Fixture(); f.start(); f.subject.stop(); f.subject.retry()
        assertEquals(1, f.callbacks.size)
        assertEquals("SESSION_INACTIVE", f.subject.state.requestStatus)
    }

    @Test fun invalidSuccessDoesNotBecomeReading() {
        for (value in listOf(null, Double.NaN, Double.POSITIVE_INFINITY, -1.0)) {
            val f = Fixture(); f.start(); f.reply(value = value); f.advance()
            assertNull(f.subject.state.lastSuccess)
            assertEquals("SUCCESS", f.subject.state.lastReply!!.status)
            assertEquals("NO_VALID_DATA_TIMEOUT", f.subject.state.requestStatus)
        }
    }

    @Test fun zeroIsValid() {
        val f = Fixture(); f.start(); f.reply(value = 0.0)
        assertEquals(0.0, f.subject.state.lastSuccess!!.valueKm, 0.0)
    }

    @Test fun failedRemovalBlocksNewRegistrationUntilCleanupSucceeds() {
        val f = Fixture(); f.start(); f.reply(); f.removeFails = true
        f.subject.retry(); f.subject.retry()
        assertEquals(1, f.callbacks.size)
        assertEquals("ERROR", f.subject.state.requestStatus)
        f.reply(value = 99.0)
        assertEquals(45.2, f.subject.state.lastSuccess!!.valueKm, 0.0)
        f.removeFails = false; f.subject.retry()
        assertEquals(2, f.callbacks.size)
        assertEquals(1, f.maximumLive)
    }

    @Test fun registrationFailureCleansUpPartialSubscription() {
        val f = Fixture(); f.registrationError = IllegalStateException("host failed"); f.start()
        assertEquals("ERROR", f.subject.state.requestStatus)
        assertEquals(0, f.live)
        assertTrue(f.timers.all { it.cancelled })
    }

    @Test fun permissionRevocationAtTimeoutRemovesListener() {
        val f = Fixture(); f.start(); f.permission = false; f.advance()
        assertEquals("PERMISSION_REQUIRED", f.subject.state.requestStatus)
        assertEquals(0, f.live)
    }

    @Test fun laterSuccessUpdatesReceiptTimeWithoutChangingUnits() {
        val f = Fixture(); f.start(); f.reply()
        f.advance(1000); f.reply(value = 45.3)
        assertEquals(45.3, f.subject.state.lastSuccess!!.valueKm, 0.0)
        assertEquals(1_001_100, f.subject.state.lastSuccess!!.receivedAtMillis)
    }

    @Test fun timestampAnomaliesAreLoggedWithoutConversionOrRejectingValue() {
        val f = Fixture(); f.start(); f.reply(stamp = 877712202363415)
        f.reply(stamp = 877712202363414)
        assertEquals(877712202363414, f.subject.state.lastReply!!.rawTimestamp)
        assertEquals("SUCCESS", f.subject.state.requestStatus)
        assertTrue(f.logs.last().contains("timestampAheadOfElapsedMillis=true"))
        assertTrue(f.logs.last().contains("timestampRegressed=true"))
    }

    @Test fun permissionRequestFailureIsVisibleAndRetainsReading() {
        val f = Fixture(); f.start(); f.reply()
        f.subject.permissionRequestFailed(IllegalStateException("host failed"))
        assertEquals("ERROR", f.subject.state.requestStatus)
        assertEquals(45.2, f.subject.state.lastSuccess!!.valueKm, 0.0)
    }

    @Test fun permissionResultAfterStopCannotReactivateSubscription() {
        val f = Fixture(); f.start(); f.subject.stop()
        f.subject.permissionDenied()
        f.subject.permissionRequestFailed(IllegalStateException("late"))
        assertEquals("STOPPED", f.subject.state.requestStatus)
        assertEquals(0, f.live)
    }

    @Test fun identityChangeCannotPublishPreviousCarReadingEvenWhenRemovalFails() {
        val f = Fixture(); f.start(); f.reply(); f.states.clear()
        f.removeFails = true; f.subject.resetIdentity()
        assertTrue(f.states.isNotEmpty())
        assertTrue(f.states.all { it.lastSuccess == null && it.lastReply == null })
        assertEquals(1, f.callbacks.size)
    }
}
