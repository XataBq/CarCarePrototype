package com.example.carcareprototype.presentation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.carcareprototype.car.CarMileagePermission
import com.example.carcareprototype.car.MileageRetry
import com.example.carcareprototype.diagnostics.CarLogExportService
import com.example.carcareprototype.diagnostics.DiagnosticRepository

/** Composition boundary: no Activity or Context is retained by the ViewModel. */
internal class DiagnosticViewModelFactory(context: Context) : ViewModelProvider.Factory {
    private val app = context.applicationContext

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(DiagnosticViewModel::class.java))
        CarMileagePermission.refresh(app)
        @Suppress("UNCHECKED_CAST")
        return DiagnosticViewModel(
            DiagnosticRepository,
            CarLogExportService(app, DiagnosticRepository::snapshot),
            CarMileagePermission.state,
            MileageRetry.feedback,
            refreshPermission = { CarMileagePermission.refresh(app) },
            reportPermission = { result ->
                CarMileagePermission.reportRequestResult(result && CarMileagePermission.isGranted(app))
            },
            requestRetry = MileageRetry::request,
        ) as T
    }
}
