package com.example.carcareprototype.diagnostics

import android.util.Log
import com.example.carcareprototype.presentation.state.DiagnosticState
import com.example.carcareprototype.store.HistoryAction
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DiagnosticRepository {

    private val historyQueue = Channel<HistoryAction>(Channel.UNLIMITED)

    private val _state = MutableStateFlow(DiagnosticState())
    val state = _state.asStateFlow()

    fun setAndroidAutoConnected(connected: Boolean) {
        _state.update {
            it.copy(androidAutoConnected = connected)
        }

        log(
            if (connected) "Car Host Connected"
            else "Car Host Disconnected"
        )
    }

    fun setMileage(
        mileageKm: Double?,
        status: String,
        timestampMillis: Long? = null,
    ) {
        _state.update {
            it.copy(
                mileageKm = mileageKm,
                mileageStatus = status,
                mileageTimestampMillis = timestampMillis,
            )
        }

        log(
            "Mileage update: " +
                "value=$mileageKm km, " +
                "status=$status, " +
                "timestamp=$timestampMillis"
        )
    }

    fun log(message: String) {
        Log.d(LOG_NAME, message)

        val time = SimpleDateFormat(
            "HH:mm:ss",
            Locale.getDefault()
        ).format(Date())

        _state.update {
            it.copy(
                logs = (it.logs + "[$time] $message").takeLast(100)
            )
        }
    }

    private const val LOG_NAME = "CarTester"
}
