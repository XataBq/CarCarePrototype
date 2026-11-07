package com.example.carcareprototype.diagnostics

import com.example.carcareprototype.car.CarLogSnapshot
import kotlinx.coroutines.flow.StateFlow

/** Application diagnostics contract shared by phone presentation and export workflows. */
internal interface DiagnosticJournal {
    val state: StateFlow<DiagnosticState>
    fun selectCar(id: String)
    fun log(message: String, connectionToken: String? = null)
    suspend fun snapshot(sessionId: String? = null, startOffset: Long = 0): CarLogSnapshot
}
