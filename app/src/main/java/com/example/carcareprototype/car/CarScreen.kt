package com.example.carcareprototype.car

import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Pane
import androidx.car.app.model.PaneTemplate
import androidx.car.app.model.Row
import androidx.car.app.model.Template
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.example.carcareprototype.diagnostics.DiagnosticRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class CarScreen(
    carContext: CarContext
) : Screen(carContext), DefaultLifecycleObserver {
    private var stateJob: Job? = null

    init {
        lifecycle.addObserver(this)
    }

    override fun onStart(owner: LifecycleOwner) {
        stateJob = lifecycleScope.launch {
            DiagnosticRepository.state.collect {
                invalidate()
            }
        }
    }

    override fun onStop(owner: LifecycleOwner) {
        stateJob?.cancel()
        stateJob = null
    }

    override fun onGetTemplate(): Template {
        val state = DiagnosticRepository.state.value

        val mileageText = state.mileageKm
            ?.let { "%.1f km".format(it) }
            ?: "No data"

        val connectionText = if (state.androidAutoConnected) {
            "Car session active"
        } else {
            "Waiting for car host"
        }

        val pane = Pane.Builder()
            .addRow(
                Row.Builder()
                    .setTitle("Connection")
                    .addText(connectionText)
                    .build()
            )
            .addRow(
                Row.Builder()
                    .setTitle("Odometr")
                    .addText(mileageText)
                    .build()
            )
            .addRow(
                Row.Builder()
                    .setTitle("Mileage API status")
                    .addText(state.mileageStatus)
                    .build()
            )
            .build()

        return PaneTemplate.Builder(pane)
            .setTitle("Car Connection Tester")
            .build()
    }
}