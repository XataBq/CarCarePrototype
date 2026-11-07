package com.example.carcareprototype.presentation

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carcareprototype.car.CarLogExportMode
import com.example.carcareprototype.car.carLogFileName
import com.example.carcareprototype.diagnostics.DiagnosticJournal
import com.example.carcareprototype.diagnostics.CarLogExportOperations
import com.example.carcareprototype.car.MileagePermissionState
import kotlinx.coroutines.flow.StateFlow
import com.example.carcareprototype.diagnostics.CarLogExportResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

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

internal class DiagnosticViewModel(
    private val repository: DiagnosticJournal,
    private val exportService: CarLogExportOperations,
    val mileagePermission: StateFlow<MileagePermissionState>,
    val retryFeedback: StateFlow<String?>,
    private val refreshPermission: () -> Unit,
    private val reportPermission: (Boolean) -> Unit,
    private val requestRetry: () -> Unit,
) : ViewModel() {
    val state = repository.state
    private val _export = MutableStateFlow(CarExportState(
        folder = exportService.folder
    ))
    val export = _export.asStateFlow()

    fun selectCar(id: String) = repository.selectCar(id)
    fun logManualEvent() = repository.log("Manual diagnostic event")
    fun retryMileage() = requestRetry()
    fun refreshMileagePermission() = refreshPermission()
    fun onMileagePermissionResult(granted: Boolean) {
        reportPermission(granted)
        repository.log("Phone mileage permission result granted=${mileagePermission.value.granted}")
    }

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
        if (_export.value.busy) return
        _export.update { it.copy(conflict = null, editName = true, error = null) }
    }

    fun selectFolder(uri: Uri) {
        if (_export.value.busy) return
        _export.update { it.copy(busy = true, error = null) }
        viewModelScope.launch {
            var selected = false
            try {
                exportService.selectFolder(uri)
                _export.update { it.copy(folder = uri, conflict = null, error = null) }
                selected = true
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                _export.update { it.copy(error = "Could not access folder: ${error.message}") }
            } finally {
                _export.update { it.copy(busy = false) }
            }
            if (selected && _export.value.sessionOnly == false && !_export.value.editName) save()
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
                when (val result = exportService.export(folder, name, mode, request.conflict,
                    request.sessionId, request.sessionStartOffset)) {
                    is CarLogExportResult.Conflict -> _export.update { it.copy(conflict = result.destination) }
                    is CarLogExportResult.Saved -> _export.update {
                        it.copy(sessionOnly = null, conflict = null,
                            message = "Saved $name in the selected folder", savedFile = result.destination)
                    }
                }
            } catch (timeout: TimeoutCancellationException) {
                _export.update { it.copy(conflict = null,
                    error = "The journal is not ready. Check the storage error and try again.") }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                _export.update { it.copy(conflict = null, error = "Export failed: ${error.message}") }
            } finally {
                _export.update { it.copy(busy = false) }
            }
        }
    }
}
