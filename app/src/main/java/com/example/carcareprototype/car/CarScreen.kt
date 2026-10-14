package com.example.carcareprototype.car

import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.ParkedOnlyOnClickListener
import androidx.car.app.model.Pane
import androidx.car.app.model.PaneTemplate
import androidx.car.app.model.Row
import androidx.car.app.model.Template
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.example.carcareprototype.diagnostics.DiagnosticRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class CarScreen(
    carContext: CarContext,
    private val onRequestMileagePermission: () -> Unit
) : Screen(carContext), DefaultLifecycleObserver {
    private var stateJob: Job? = null

    init {
        lifecycle.addObserver(this)
    }

    override fun onStart(owner: LifecycleOwner) {
        stateJob = lifecycleScope.launch {
            DiagnosticRepository.state
                .map { it.toCarScreenState() }
                .distinctUntilChanged()
                .collect { invalidate() }
        }
    }

    override fun onStop(owner: LifecycleOwner) {
        stateJob?.cancel()
        stateJob = null
    }

    override fun onGetTemplate(): Template {
        val state = DiagnosticRepository.state.value.toCarScreenState()

        val pane = Pane.Builder()
            .addRow(
                Row.Builder()
                    .setTitle("Connection")
                    .addText(state.connectionText)
                    .build()
            )
            .addRow(
                Row.Builder()
                    .setTitle("Odometr")
                    .addText(state.mileageText)
                    .build()
            )
            .addRow(
                Row.Builder()
                    .setTitle("Mileage API status")
                    .addText(state.mileageStatus)
                    .build()
            )

        if (!CarMileagePermission.isGranted(carContext)) {
            pane.addAction(
                Action.Builder()
                    .setTitle("Allow mileage access")
                    .setOnClickListener(ParkedOnlyOnClickListener.create { onRequestMileagePermission() })
                    .build()
            )
        }

        return PaneTemplate.Builder(pane.build())
            .setTitle("Car Connection Tester")
            .build()
    }
}
