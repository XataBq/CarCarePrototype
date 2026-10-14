package com.example.carcareprototype.presentation

import android.app.Application
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.carcareprototype.car.CarLogExporter
import com.example.carcareprototype.car.CarLogExportMode
import com.example.carcareprototype.car.carLogFileName
import com.example.carcareprototype.diagnostics.DiagnosticRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout

internal data class CarExportState(
    val sessionOnly: Boolean? = null,
    val fileName: String = "car_diagnostics.jsonl",
    val editName: Boolean = false,
    val sessionId: String? = null,
    val sessionCarId: String? = null,
    val sessionStartOffset: Long = 0,
    val folder: Uri? = null,
    val conflict: Uri? = null,
    val busy: Boolean = false,
    val message: String? = null,
    val error: String? = null,
    val savedFile: Uri? = null
)

internal class DiagnosticViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = DiagnosticRepository
    private val resolver = application.contentResolver
    private val preferences = application.getSharedPreferences("car_log_export", 0)
    private val exporter = CarLogExporter(resolver)
    val state = repository.state
    private val _export = MutableStateFlow(CarExportState(
        folder = preferences.getString("folder", null)?.let(Uri::parse)
    ))
    val export = _export.asStateFlow()

    fun beginExport(sessionOnly: Boolean) {
        if (_export.value.busy) return
        val current = state.value
        if (sessionOnly && current.lastSessionId == null) return
        _export.update {
            it.copy(sessionOnly = sessionOnly, editName = sessionOnly,
                sessionId = if (sessionOnly) current.lastSessionId else null,
                sessionCarId = if (sessionOnly) current.lastSessionCarId else null,
                sessionStartOffset = if (sessionOnly) current.lastSessionStartOffset else 0,
                fileName = if (sessionOnly) "car_session.jsonl" else "car_diagnostics.jsonl",
                conflict = null, message = null, error = null)
        }
        if (!sessionOnly && _export.value.folder != null) save()
    }

    fun dismissExport() {
        if (!_export.value.busy) _export.update { it.copy(sessionOnly = null, conflict = null, error = null) }
    }

    fun changeName(name: String) {
        if (!_export.value.busy) _export.update { it.copy(fileName = name, conflict = null, error = null) }
    }

    fun chooseAnotherName() {
        _export.update { it.copy(conflict = null, editName = true, error = null) }
    }

    fun selectFolder(uri: Uri) {
        try {
            resolver.takePersistableUriPermission(
                uri, Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
            preferences.edit().putString("folder", uri.toString()).apply()
            _export.update { it.copy(folder = uri, conflict = null, error = null) }
            if (_export.value.sessionOnly == false && !_export.value.editName) save()
        } catch (error: Exception) {
            _export.update { it.copy(error = "Could not access folder: ${error.message}") }
        }
    }

    fun save(mode: CarLogExportMode = CarLogExportMode.CREATE) {
        val request = _export.value
        if (request.sessionOnly == null) return
        if (request.busy) return
        val folder = request.folder ?: return
        val name = try {
            carLogFileName(request.fileName)
        } catch (error: IllegalArgumentException) {
            _export.update { it.copy(error = error.message) }
            return
        }
        _export.update { it.copy(busy = true, error = null) }
        viewModelScope.launch {
            try {
                val existing = withContext(Dispatchers.IO) { exporter.find(folder, name) }
                if (mode == CarLogExportMode.CREATE && existing != null) {
                    _export.update { it.copy(conflict = existing, busy = false) }
                    return@launch
                }

                val snapshot = withTimeout(30_000) {
                    repository.snapshot(request.sessionId, request.sessionStartOffset)
                }
                val saved = withContext(Dispatchers.IO) {
                    exporter.write(folder, name, mode, request.conflict, snapshot)
                }
                _export.update {
                    it.copy(sessionOnly = null, conflict = null, busy = false,
                        message = "Saved $name in the selected folder", savedFile = saved)
                }
            } catch (timeout: TimeoutCancellationException) {
                _export.update { it.copy(busy = false, conflict = null,
                    error = "The journal is not ready. Check the storage error and try again.") }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                _export.update { it.copy(busy = false, conflict = null, error = "Export failed: ${error.message}") }
            }
        }
    }
}
