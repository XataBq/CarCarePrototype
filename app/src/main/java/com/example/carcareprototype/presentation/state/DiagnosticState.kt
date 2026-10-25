package com.example.carcareprototype.presentation.state

import com.example.carcareprototype.car.MileageState

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
