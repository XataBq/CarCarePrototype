package com.example.carcareprototype.car

import android.content.Intent
import androidx.car.app.Screen
import androidx.car.app.Session
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.example.carcareprototype.diagnostics.DiagnosticRepository

class CarSession : Session(), DefaultLifecycleObserver {

    private var mileageDataSource: MileageDataSource? = null

    init {
        lifecycle.addObserver(this)
    }

    override fun onCreateScreen(intent: Intent): Screen {
        DiagnosticRepository.log("Car Screen created")

        mileageDataSource = MileageDataSource(carContext)

        return CarScreen(carContext)
    }

    override fun onStart(owner: LifecycleOwner) {
        DiagnosticRepository.log("Car session started")
        DiagnosticRepository.setAndroidAutoConnected(true)

        mileageDataSource?.start()
    }

    override fun onStop(owner: LifecycleOwner) {
        DiagnosticRepository.setAndroidAutoConnected(false)

        mileageDataSource?.stop()
    }

    override fun onDestroy(owner: LifecycleOwner) {
        mileageDataSource?.stop()
        mileageDataSource = null

        DiagnosticRepository.setAndroidAutoConnected(false)
        DiagnosticRepository.log("Car session destroyed")
    }
}