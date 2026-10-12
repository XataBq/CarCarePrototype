package com.example.carcareprototype.presentation

import androidx.lifecycle.ViewModel
import com.example.carcareprototype.diagnostics.DiagnosticRepository

internal class DiagnosticViewModel(
    private val repository: DiagnosticRepository = DiagnosticRepository
) : ViewModel() {

    val state = repository.state
}
