package com.example.carcareprototype.car

import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import androidx.car.app.CarContext
import androidx.car.app.hardware.CarHardwareManager
import androidx.car.app.hardware.common.OnCarDataAvailableListener
import androidx.car.app.hardware.info.CarInfo
import androidx.car.app.hardware.info.Mileage
import com.example.carcareprototype.diagnostics.DiagnosticRepository

internal class MileageDataSource(
    private val carContext: CarContext,
    private val connectionToken: String,
    private val isStarted: () -> Boolean,
) {
    private val handler = Handler(Looper.getMainLooper())
    private var revision = DiagnosticRepository.mileageRevision.value
    private var permissionRequestInFlight = false
    private val subscription = MileageSubscription(
        backend = object : MileageBackend {
            private var info: CarInfo? = null
            private var listener: OnCarDataAvailableListener<Mileage>? = null

            override fun register(callback: (MileageReply) -> Unit) {
                val carInfo = carContext.getCarService(CarHardwareManager::class.java).carInfo
                val newListener = OnCarDataAvailableListener<Mileage> { mileage ->
                    if (!isStarted()) return@OnCarDataAvailableListener
                    val value = mileage.odometerMeters
                    // AndroidX 1.7's misleading name returns kilometers. Keep raw timestamp units.
                    callback(MileageReply(value.value?.toDouble(), value.status, value.timestampMillis,
                        System.currentTimeMillis(), SystemClock.elapsedRealtime(), SystemClock.elapsedRealtimeNanos()))
                }
                info = carInfo
                listener = newListener
                carInfo.addMileageListener(carContext.mainExecutor, newListener)
            }

            override fun remove() {
                listener?.let { info?.removeMileageListener(it) }
                listener = null
                info = null
            }
        },
        scheduler = MileageScheduler { delay, action ->
            val task = Runnable { action() }
            handler.postDelayed(task, delay)
            val cancel: () -> Unit = { handler.removeCallbacks(task) }
            cancel
        },
        elapsedMillis = SystemClock::elapsedRealtime,
        wallMillis = System::currentTimeMillis,
        permitted = { CarMileagePermission.isGranted(carContext) },
        scopeCurrent = { revision == DiagnosticRepository.mileageRevision.value },
        publish = { state, message -> DiagnosticRepository.updateMileage(state, connectionToken, revision, message) },
        log = { DiagnosticRepository.log("connection=$connectionToken $it", connectionToken) },
    )

    fun start() {
        checkMainThread()
        if (!isStarted()) return
        synchronizeIdentity()
        subscription.start("STARTED / permission refresh")
        if (!CarMileagePermission.isGranted(carContext) && CarMileagePermission.lastRequestDenied) {
            subscription.permissionDenied()
        }
    }

    fun identityChanged() {
        checkMainThread()
        synchronizeIdentity()
    }

    private fun synchronizeIdentity() {
        val latest = DiagnosticRepository.mileageRevision.value
        if (revision != latest) {
            revision = latest
            subscription.resetIdentity()
        }
    }

    fun retry() {
        checkMainThread()
        if (!isStarted()) {
            DiagnosticRepository.log("Retry rejected: car session is not STARTED", connectionToken)
            return
        }
        synchronizeIdentity()
        subscription.retry()
    }

    fun requestPermission() {
        checkMainThread()
        if (!isStarted() || permissionRequestInFlight) return
        if (CarMileagePermission.isGranted(carContext)) { start(); return }
        permissionRequestInFlight = true
        DiagnosticRepository.log("Mileage permission requested connection=$connectionToken", connectionToken)
        try {
            carContext.requestPermissions(listOf(CarMileagePermission.NAME), carContext.mainExecutor) { _, _ ->
                permissionRequestInFlight = false
                val granted = CarMileagePermission.isGranted(carContext)
                CarMileagePermission.reportRequestResult(granted)
                if (granted) {
                    start()
                } else if (isStarted()) subscription.permissionDenied()
            }
        } catch (error: Exception) {
            permissionRequestInFlight = false
            DiagnosticRepository.log("Mileage permission request failed: ${error.message}", connectionToken)
            subscription.permissionRequestFailed(error)
        }
    }

    fun stop(reason: String = "ON_STOP") {
        checkMainThread()
        subscription.stop(reason)
    }

    private fun checkMainThread() = check(Looper.myLooper() == Looper.getMainLooper())
}
