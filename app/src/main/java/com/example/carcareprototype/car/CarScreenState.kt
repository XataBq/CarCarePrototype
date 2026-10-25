package com.example.carcareprototype.car

import com.example.carcareprototype.presentation.state.DiagnosticState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

internal data class CarScreenState(
    val connectionText: String,
    val mileageText: String,
    val mileageStatus: String,
    val receiptText: String,
    val responseText: String,
    val freshness: String,
)

internal fun DiagnosticState.toCarScreenState(): CarScreenState = CarScreenState(
    connectionText = if (androidAutoConnected) "Car session active" else "Waiting for car host",
    mileageText = mileage.lastSuccess?.let { "%.1f km".format(it.valueKm) } ?: "No data",
    mileageStatus = mileage.requestStatus,
    receiptText = "Received at: " + (mileage.lastSuccess?.receivedAtMillis?.let {
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(it))
    } ?: "N/A"),
    responseText = mileage.lastReply?.let { "${it.status}: ${it.rawValueKm ?: "null"} km" } ?: "No callback yet",
    freshness = mileage.freshness,
)
