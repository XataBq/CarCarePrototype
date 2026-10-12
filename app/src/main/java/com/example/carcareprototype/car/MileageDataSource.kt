package com.example.carcareprototype.car

import androidx.car.app.CarContext
import androidx.car.app.hardware.CarHardwareManager
import androidx.car.app.hardware.common.CarValue
import androidx.car.app.hardware.common.OnCarDataAvailableListener
import androidx.car.app.hardware.info.CarInfo
import androidx.car.app.hardware.info.Mileage
import com.example.carcareprototype.diagnostics.DiagnosticRepository

internal class MileageDataSource(
    private val carContext: CarContext
) {
    private var carInfo: CarInfo? = null
    private var listening = false

    private val mileageListener = OnCarDataAvailableListener<Mileage> { mileage ->

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
        )
    }

    fun start() {
        if (listening) return

        DiagnosticRepository.setMileage(
            mileageKm = null,
            status = "SUBSCRIBING"
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
                "Mileage listener registered"
            )
        } catch (e: Exception) {
            DiagnosticRepository.log(
                "Mileage subscription failed: ${e.javaClass.simpleName}: ${e.message}"
            )

            DiagnosticRepository.setMileage(
                mileageKm = null,
                status = "ERROR"
            )
        }
    }

    fun stop() {
        if (!listening) return

        try {
            carInfo?.removeMileageListener(mileageListener)

            DiagnosticRepository.log(
                "Mileage listener removed"
            )
        } catch (e: Exception) {
            DiagnosticRepository.log(
                "Mileage listener removal failed: ${e.message}"
            )
        } finally {
            listening = false
            carInfo = null
        }
    }
}
