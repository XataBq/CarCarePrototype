package com.example.carcareprototype.car

import android.app.Application
import com.example.carcareprototype.diagnostics.DiagnosticRepository

class CarDiagnosticsApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        DiagnosticRepository.initialize(this)
    }
}
