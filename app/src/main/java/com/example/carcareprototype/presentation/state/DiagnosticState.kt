package com.example.carcareprototype.presentation.state

data class DiagnosticState(
    val androidAutoConnected: Boolean = false,
    val mileageKm: Double? = null,
    val mileageStatus: String = "NOT_REQUESTED",
    val mileageTimestampMillis: Long? = null,
    val logs: List<String> = emptyList(),
    val storageError: String? = null,
    val selectedCarId: String = "",
    val lastSessionId: String? = null,
    val lastSessionCarId: String? = null,
    val lastSessionStartOffset: Long = 0,
)
