package com.example.carcareprototype.car

import org.junit.Assert.*
import org.junit.Test

class CarSessionTrackerTest {
    @Test fun reconnectWithinTwoMinutesIncludesOriginalSessionOffset() {
        val tracker = CarSessionTracker()
        tracker.connect("one", "my-car", 1_000, 10)
        val original = tracker.latest!!
        tracker.disconnect("one", 2_000)
        tracker.connect("two", "my-car", 122_000, 500)
        assertEquals(original.id, tracker.latest!!.id)
        assertEquals(10L, tracker.latest!!.startOffset)
    }

    @Test fun reconnectAfterTimeoutStartsNewSession() {
        val tracker = CarSessionTracker()
        tracker.connect("one", "car", 0, 0)
        val previous = tracker.latest!!.id
        tracker.disconnect("one", 1_000)
        tracker.connect("two", "car", 121_001, 100)
        assertNotEquals(previous, tracker.latest!!.id)
        assertEquals(100L, tracker.latest!!.startOffset)
    }

    @Test fun anotherCarStartsNewSessionEvenInsideTimeout() {
        val tracker = CarSessionTracker()
        tracker.connect("one", "A", 0, 0)
        val previous = tracker.latest!!.id
        tracker.disconnect("one", 1_000)
        tracker.connect("two", "B", 1_001, 100)
        assertNotEquals(previous, tracker.latest!!.id)
        assertEquals("B", tracker.latest!!.carId)
        assertFalse(tracker.disconnect("one", 2_000))
        assertEquals("two", tracker.connectionToken)
    }

    @Test fun manualCarChangeSplitsLiveSessionButInitialNamingKeepsLogs() {
        val tracker = CarSessionTracker()
        tracker.connect("one", null, 0, 0)
        val unnamed = tracker.latest!!.id
        tracker.changeCar("A", 100)
        assertEquals(unnamed, tracker.latest!!.id)
        tracker.changeCar("B", 200)
        assertNotEquals(unnamed, tracker.latest!!.id)
        assertEquals(200L, tracker.latest!!.startOffset)
    }

    @Test fun unknownCarsAreNeverAutomaticallyMerged() {
        val tracker = CarSessionTracker()
        tracker.connect("one", null, 0, 0)
        val previous = tracker.latest!!.id
        tracker.disconnect("one", 1_000)
        tracker.connect("two", null, 1_001, 100)
        assertNotEquals(previous, tracker.latest!!.id)
    }

    @Test fun processRestartRestoresReconnectBoundary() {
        val tracker = CarSessionTracker()
        tracker.restore(CarLogSession("saved", "A", 50, 10_000), 20_000)
        tracker.connect("new", "A", 130_000, 100)
        assertEquals("saved", tracker.latest!!.id)
        val killed = CarSessionTracker()
        killed.restore(CarLogSession("active", "A", 50), 20_000)
        killed.connect("new", "A", 140_001, 100)
        assertNotEquals("active", killed.latest!!.id)
    }

    @Test fun clockMovingBackwardsDoesNotMergeSessions() {
        val tracker = CarSessionTracker()
        tracker.restore(CarLogSession("saved", "A", 0, 10_000), 10_000)
        tracker.connect("new", "A", 9_999, 100)
        assertNotEquals("saved", tracker.latest!!.id)
    }
}
