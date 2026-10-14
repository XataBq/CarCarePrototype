package com.example.carcareprototype.bluetooth

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

internal class BluetoothPermissions(
    private val sdkInt: Int,
    private val isGranted: (String) -> Boolean
) {
    constructor(context: Context) : this(
        Build.VERSION.SDK_INT,
        { permission ->
            ContextCompat.checkSelfPermission(context.applicationContext, permission) ==
                PackageManager.PERMISSION_GRANTED
        }
    )

    @SuppressLint("InlinedApi")
    private val scanPermission = if (sdkInt >= Build.VERSION_CODES.S) {
        Manifest.permission.BLUETOOTH_SCAN
    } else {
        Manifest.permission.ACCESS_FINE_LOCATION
    }

    val requiredPermissions: Array<String>
        @SuppressLint("InlinedApi")
        get() = if (sdkInt >= Build.VERSION_CODES.S) {
            arrayOf(Manifest.permission.BLUETOOTH_CONNECT, scanPermission)
        } else {
            arrayOf(scanPermission)
        }

    @SuppressLint("InlinedApi")
    fun canConnect(): Boolean =
        sdkInt < Build.VERSION_CODES.S || isGranted(Manifest.permission.BLUETOOTH_CONNECT)

    fun canScan(): Boolean = isGranted(scanPermission)

    @SuppressLint("InlinedApi")
    fun canCancelDiscovery(): Boolean =
        sdkInt < Build.VERSION_CODES.S || isGranted(Manifest.permission.BLUETOOTH_SCAN)
}
