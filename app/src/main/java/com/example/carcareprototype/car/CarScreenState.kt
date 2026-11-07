package com.example.carcareprototype.car

import com.example.carcareprototype.diagnostics.DiagnosticState
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

internal fun DiagnosticState.toCarScreenState(): CarScreenState = mileage.toCarScreenState(androidAutoConnected)

internal fun MileageState.toCarScreenState(connected: Boolean): CarScreenState = CarScreenState(
    connectionText = if (connected) "Car session active" else "Waiting for car host",
    mileageText = lastSuccess?.let { "%.1f km".format(it.valueKm) } ?: "No data",
    mileageStatus = requestStatus,
    receiptText = "Received at: " + (lastSuccess?.receivedAtMillis?.let {
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(it))
    } ?: "N/A"),
    responseText = lastReply?.let { "${it.status}: ${it.rawValueKm ?: "null"} km" } ?: "No callback yet",
    freshness = freshness,
)
