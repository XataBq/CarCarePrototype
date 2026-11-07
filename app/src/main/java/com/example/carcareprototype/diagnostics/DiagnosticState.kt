package com.example.carcareprototype.diagnostics

import com.example.carcareprototype.car.MileageState

/** Shared diagnostic state; neither persistence nor the car host depends on phone presentation. */
data class DiagnosticState(
    val androidAutoConnected: Boolean = false,
    val mileage: MileageState = MileageState(),
    val logs: List<String> = emptyList(),
    val storageError: String? = null,
    val selectedCarId: String = "",
    val lastSessionId: String? = null,
    val lastSessionCarId: String? = null,
    val lastSessionStartOffset: Long = 0,
)
