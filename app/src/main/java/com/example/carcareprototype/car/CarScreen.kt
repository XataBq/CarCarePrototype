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
    private val connectionToken: String,
    private val onRequestMileagePermission: () -> Unit,
    private val onRetryMileage: () -> Unit,
) : Screen(carContext), DefaultLifecycleObserver {
    private var stateJob: Job? = null

    init {
        lifecycle.addObserver(this)
    }

    override fun onStart(owner: LifecycleOwner) {
        DiagnosticRepository.log("CarScreen ON_START connection=$connectionToken", connectionToken)
        stateJob = lifecycleScope.launch {
            DiagnosticRepository.state
                .map { it.toCarScreenState() }
                .distinctUntilChanged()
                .collect { invalidate() }
        }
    }

    override fun onStop(owner: LifecycleOwner) {
        DiagnosticRepository.log("CarScreen ON_STOP connection=$connectionToken", connectionToken)
        stateJob?.cancel()
        stateJob = null
    }

    override fun onDestroy(owner: LifecycleOwner) {
        DiagnosticRepository.log("CarScreen ON_DESTROY connection=$connectionToken", connectionToken)
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
                    .setTitle("Last received (UNVERIFIED)")
                    .addText(state.mileageText)
                    .addText(state.receiptText)
                    .build()
            )
            .addRow(
                Row.Builder()
                    .setTitle("Current request / last API response")
                    .addText(state.mileageStatus)
                    .addText(state.responseText)
                    .build()
            )

        pane.addRow(Row.Builder().setTitle("Android Auto Car Hardware API")
            .addText(state.freshness).build())
        pane.addAction(Action.Builder().setTitle("Retry mileage")
            .setOnClickListener(ParkedOnlyOnClickListener.create { onRetryMileage() }).build())

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
