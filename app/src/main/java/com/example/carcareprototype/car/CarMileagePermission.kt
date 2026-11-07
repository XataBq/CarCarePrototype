package com.example.carcareprototype.car

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

internal object CarMileagePermission {
    const val NAME = "com.google.android.gms.permission.CAR_MILEAGE"

    private val _state = MutableStateFlow(MileagePermissionState())
    val state = _state.asStateFlow()
    val changes = state
    val lastRequestDenied: Boolean get() = state.value.denied

    fun isGranted(context: Context): Boolean =
        ContextCompat.checkSelfPermission(context, NAME) == PackageManager.PERMISSION_GRANTED

    fun refresh(context: Context) {
        val granted = isGranted(context)
        val next = state.value.copy(granted = granted, denied = !granted && state.value.denied)
        if (next == state.value) return
        _state.value = next
    }

    fun reportRequestResult(granted: Boolean) {
        _state.value = MileagePermissionState(granted, denied = !granted)
    }
}

internal data class MileagePermissionState(val granted: Boolean = false, val denied: Boolean = false)
