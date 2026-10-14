package com.example.carcareprototype.car

import androidx.car.app.CarContext
import androidx.car.app.hardware.CarHardwareManager
import androidx.car.app.hardware.common.CarValue
import androidx.car.app.hardware.common.OnCarDataAvailableListener
import androidx.car.app.hardware.info.CarInfo
import androidx.car.app.hardware.info.Mileage
import com.example.carcareprototype.diagnostics.DiagnosticRepository

internal class MileageDataSource(
    private val carContext: CarContext,
    private val connectionToken: String
) {
    private var carInfo: CarInfo? = null
    private var listening = false
    private var active = false
    private var permissionRequestInFlight = false

    private val mileageListener = OnCarDataAvailableListener<Mileage> { mileage ->
        if (!active || !CarMileagePermission.isGranted(carContext)) return@OnCarDataAvailableListener

        val odometer = mileage.odometerMeters

        val status = when (odometer.status) {
            CarValue.STATUS_SUCCESS -> "SUCCESS"
            CarValue.STATUS_UNIMPLEMENTED -> "UNIMPLEMENTED"
            CarValue.STATUS_UNAVAILABLE -> "UNAVAILABLE"
            CarValue.STATUS_UNKNOWN -> "UNKNOWN"
            else -> "UNKNOWN_${odometer.status}"
        }

        val valueKm = if (
            odometer.status == CarValue.STATUS_SUCCESS
        ) {
            odometer.value?.let { it.toDouble() / 1_000.0 }
        } else { null }

        DiagnosticRepository.setMileage(
            mileageKm = valueKm,
            status = status,
            timestampMillis = odometer.timestampMillis,
            connectionToken = connectionToken,
        )
    }

    fun start() {
        active = true
        if (!CarMileagePermission.isGranted(carContext)) {
            removeListener()
            reportPermissionStatus("PERMISSION_REQUIRED")
            return
        }
        if (listening) return

        DiagnosticRepository.setMileage(
            mileageKm = null,
            status = "SUBSCRIBING",
            connectionToken = connectionToken
        )

        try {
            val hardwareManager = carContext.getCarService(
                CarHardwareManager::class.java
            )

            val info = hardwareManager.carInfo

            info.addMileageListener(
                carContext.mainExecutor,
                mileageListener,
            )

            carInfo = info
            listening = true

            DiagnosticRepository.log(
                "Mileage listener registered", connectionToken
            )
        } catch (e: SecurityException) {
            DiagnosticRepository.log("Mileage permission unavailable: ${e.message}", connectionToken)
            reportPermissionStatus("PERMISSION_REQUIRED")
        } catch (e: Exception) {
            DiagnosticRepository.log(
                "Mileage subscription failed: ${e.javaClass.simpleName}: ${e.message}", connectionToken
            )

            DiagnosticRepository.setMileage(
                mileageKm = null,
                status = "ERROR",
                connectionToken = connectionToken
            )
        }
    }

    fun requestPermission() {
        if (!active || permissionRequestInFlight) return
        if (CarMileagePermission.isGranted(carContext)) {
            start()
            return
        }
        permissionRequestInFlight = true
        reportPermissionStatus("PERMISSION_REQUESTED")
        try {
            carContext.requestPermissions(listOf(CarMileagePermission.NAME), carContext.mainExecutor) { _, _ ->
                permissionRequestInFlight = false
                if (CarMileagePermission.isGranted(carContext)) {
                    CarMileagePermission.refresh()
                    if (active) start()
                } else if (active) {
                    reportPermissionStatus("PERMISSION_DENIED")
                }
            }
        } catch (e: Exception) {
            permissionRequestInFlight = false
            DiagnosticRepository.log("Mileage permission request failed: ${e.message}", connectionToken)
            reportPermissionStatus("PERMISSION_REQUEST_FAILED")
        }
    }

    private fun reportPermissionStatus(status: String) {
        DiagnosticRepository.setMileage(null, status, connectionToken = connectionToken)
    }

    fun stop() {
        active = false
        removeListener()
    }

    private fun removeListener() {
        if (!listening) return

        try {
            carInfo?.removeMileageListener(mileageListener)

            DiagnosticRepository.log(
                "Mileage listener removed", connectionToken
            )
        } catch (e: Exception) {
            DiagnosticRepository.log(
                "Mileage listener removal failed: ${e.message}", connectionToken
            )
        } finally {
            listening = false
            carInfo = null
        }
    }
}
