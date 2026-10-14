package com.example.carcareprototype.car

import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class CarDiagnosticStoreTest {
    @get:Rule val temporary = TemporaryFolder()
    private fun journal() = File(temporary.root, "car.jsonl")
    private fun CarLogSnapshot.text(): String = ByteArrayOutputStream().also(::writeTo).toString("UTF-8")

    @Test fun restoresOnlyLast100WithoutTruncatingHistory() {
        val file = journal()
        val store = CarDiagnosticStore(file)
        repeat(350) { store.append("{\"event\":$it,\"text\":\"Автомобиль 🚗\"}") }
        val restored = CarDiagnosticStore(file)
        assertEquals(file.readLines().takeLast(100), restored.readLast())
        assertEquals(350, restored.snapshot().text().lines().filter(String::isNotEmpty).size)
        assertTrue(restored.readLast(0).isEmpty())
    }

    @Test fun concurrentWritersDoNotLoseOrInterleaveEntries() {
        val store = CarDiagnosticStore(journal())
        val executor = Executors.newFixedThreadPool(8)
        try {
            val tasks = (0 until 8).map { worker -> Callable {
                repeat(150) { store.append("worker=$worker,event=$it") }
            } }
            executor.invokeAll(tasks, 20, TimeUnit.SECONDS).forEach { it.get() }
        } finally { executor.shutdownNow() }
        val lines = store.snapshot().text().lines().filter(String::isNotEmpty)
        assertEquals(1200, lines.size)
        assertEquals(1200, lines.toSet().size)
        repeat(8) { worker ->
            assertEquals((0 until 150).map { "worker=$worker,event=$it" }, lines.filter { it.startsWith("worker=$worker,") })
        }
    }

    @Test fun snapshotIsStableWhileMoreEventsAreAppended() {
        val store = CarDiagnosticStore(journal())
        store.append("old")
        val offset = store.size()
        store.append("session A")
        store.append("phone-only")
        store.append("session B")
        val all = store.snapshot()
        val session = store.snapshot(offset) { it.startsWith("session") }
        store.append("after snapshot")
        assertEquals("old\nsession A\nphone-only\nsession B\n", all.text())
        assertEquals("session A\nsession B\n", session.text())
    }

    @Test fun recoversIncompleteUtf8TailBeforeAppending() {
        val file = journal()
        file.writeText("first\nsecond\npartial🚗")
        val store = CarDiagnosticStore(file)
        assertEquals(listOf("first", "second"), store.readLast())
        store.append("third")
        assertEquals("first\nsecond\nthird\n", store.snapshot().text())
    }

    @Test fun readsAcrossBlockBoundariesAndHandlesEmptyJournal() {
        val store = CarDiagnosticStore(journal())
        assertTrue(store.readLast().isEmpty())
        assertEquals("", store.snapshot().text())
        val longEntry = "ж".repeat(10_000)
        store.append("first")
        store.append(longEntry)
        store.append("last")
        assertEquals(listOf(longEntry, "last"), store.readLast(2))
    }

    @Test fun retriesAfterDestinationBecomesWritable() {
        val directory = File(temporary.root, "not-created")
        val store = CarDiagnosticStore(File(directory, "car.jsonl"))
        assertThrows(java.io.IOException::class.java) { store.append("first") }
        assertTrue(directory.mkdir())
        store.append("first")
        assertEquals(listOf("first"), store.readLast())
    }
}
