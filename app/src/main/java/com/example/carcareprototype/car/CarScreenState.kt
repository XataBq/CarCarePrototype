package com.example.carcareprototype.car

import com.example.carcareprototype.presentation.state.DiagnosticState

internal data class CarScreenState(
    val connectionText: String,
    val mileageText: String,
    val mileageStatus: String
)

internal fun DiagnosticState.toCarScreenState(): CarScreenState = CarScreenState(
    connectionText = if (androidAutoConnected) "Car session active" else "Waiting for car host",
    mileageText = mileageKm?.let { "%.1f km".format(it) } ?: "No data",
    mileageStatus = mileageStatus
)
