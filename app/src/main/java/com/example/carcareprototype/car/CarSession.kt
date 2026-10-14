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
        lifecycle.addObserver(this)
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                CarMileagePermission.changes.collect { mileageDataSource?.start() }
            }
        }
    }

    override fun onCreateScreen(intent: Intent): Screen {
        DiagnosticRepository.connectCar(connectionToken)
        DiagnosticRepository.log("Car Screen created", connectionToken)

        mileageDataSource = MileageDataSource(carContext, connectionToken)

        return CarScreen(carContext) { mileageDataSource?.requestPermission() }
    }

    override fun onStart(owner: LifecycleOwner) {
        DiagnosticRepository.log("Car session started", connectionToken)
    }

    override fun onStop(owner: LifecycleOwner) {
        mileageDataSource?.stop()
        DiagnosticRepository.log("Car screen stopped", connectionToken)
    }

    override fun onDestroy(owner: LifecycleOwner) {
        mileageDataSource?.stop()
        mileageDataSource = null

        DiagnosticRepository.log("Car session destroyed", connectionToken)
        DiagnosticRepository.disconnectCar(connectionToken)
    }
}
