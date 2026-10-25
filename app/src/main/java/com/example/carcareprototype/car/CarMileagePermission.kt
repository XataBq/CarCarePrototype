package com.example.carcareprototype.car

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

internal object CarMileagePermission {
    const val NAME = "com.google.android.gms.permission.CAR_MILEAGE"

    private val revision = MutableStateFlow(0L)
    val changes = revision.asStateFlow()
    var lastRequestDenied: Boolean = false
        private set

    fun isGranted(context: Context): Boolean =
        ContextCompat.checkSelfPermission(context, NAME) == PackageManager.PERMISSION_GRANTED

    fun refresh() = revision.update { it + 1 }

    fun reportRequestResult(granted: Boolean) {
        lastRequestDenied = !granted
        refresh()
    }
}
