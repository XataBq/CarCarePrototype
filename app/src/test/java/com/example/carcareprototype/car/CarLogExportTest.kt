package com.example.carcareprototype.car

import org.junit.Assert.*
import org.junit.Test

class CarLogExportTest {
    @Test fun normalizesExtensionWithoutChangingUserName() {
        assertEquals("поездка.jsonl", carLogFileName(" поездка "))
        assertEquals("car_diagnostics.jsonl", carLogFileName("car_diagnostics.jsonl"))
        assertEquals("CAR.JSONL", carLogFileName("CAR.JSONL"))
    }

    @Test fun rejectsPathsControlCharactersAndOversizedNames() {
        listOf("", " ", ".", "..", "../car", "car\\logs", "car\nlogs", "x".repeat(241)).forEach {
            assertThrows(IllegalArgumentException::class.java) { carLogFileName(it) }
        }
    }
}
