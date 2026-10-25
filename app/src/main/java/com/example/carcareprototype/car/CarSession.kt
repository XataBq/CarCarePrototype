package com.example.carcareprototype.car

import android.content.Intent
import androidx.car.app.Screen
import androidx.car.app.Session
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.carcareprototype.diagnostics.DiagnosticRepository
import kotlinx.coroutines.launch
import java.util.UUID

class CarSession : Session(), DefaultLifecycleObserver {

    private var mileageDataSource: MileageDataSource? = null
    private val connectionToken = UUID.randomUUID().toString()

    init {
        DiagnosticRepository.log("CarSession created connection=$connectionToken")
        lifecycle.addObserver(this)
        lifecycleScope.launch {
            DiagnosticRepository.mileageRevision.collect { mileageDataSource?.identityChanged() }
        }
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                CarMileagePermission.changes.collect { mileageDataSource?.start() }
            }
        }
    }

    override fun onCreateScreen(intent: Intent): Screen {
        if (mileageDataSource == null) {
            DiagnosticRepository.connectCar(connectionToken)
            mileageDataSource = MileageDataSource(carContext, connectionToken) {
                lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)
            }
        }
        DiagnosticRepository.log("CarScreen created connection=$connectionToken", connectionToken)
        return CarScreen(carContext, connectionToken,
            onRequestMileagePermission = { mileageDataSource?.requestPermission() },
            onRetryMileage = { mileageDataSource?.retry() })
    }

    override fun onStart(owner: LifecycleOwner) {
        DiagnosticRepository.log("CarSession ON_START connection=$connectionToken", connectionToken)
        MileageRetry.attach(connectionToken) { mileageDataSource?.retry() }
    }

    override fun onStop(owner: LifecycleOwner) {
        MileageRetry.detach(connectionToken)
        mileageDataSource?.stop()
        DiagnosticRepository.log("CarSession ON_STOP connection=$connectionToken", connectionToken)
    }

    override fun onDestroy(owner: LifecycleOwner) {
        MileageRetry.detach(connectionToken)
        mileageDataSource?.stop("ON_DESTROY")
        mileageDataSource = null

        DiagnosticRepository.log("CarSession ON_DESTROY connection=$connectionToken", connectionToken)
        DiagnosticRepository.disconnectCar(connectionToken)
    }
}
