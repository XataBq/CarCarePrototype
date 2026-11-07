package com.example.carcareprototype.presentation

import android.net.Uri
import com.example.carcareprototype.car.CarLogExportMode
import com.example.carcareprototype.car.CarLogSnapshot
import com.example.carcareprototype.car.MileagePermissionState
import com.example.carcareprototype.diagnostics.CarLogExportOperations
import com.example.carcareprototype.diagnostics.CarLogExportResult
import com.example.carcareprototype.diagnostics.DiagnosticJournal
import com.example.carcareprototype.diagnostics.DiagnosticState
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.*
import org.junit.Test

class DiagnosticViewModelTest {
    private class Journal : DiagnosticJournal {
        override val state = MutableStateFlow(DiagnosticState())
        val actions = mutableListOf<String>()
        override fun selectCar(id: String) { actions += "car:$id" }
        override fun log(message: String, connectionToken: String?) { actions += message }
        override suspend fun snapshot(sessionId: String?, startOffset: Long): CarLogSnapshot = error("Not requested")
    }
    private class Exports : CarLogExportOperations {
        override val folder: Uri? = null
        override suspend fun selectFolder(uri: Uri) { error("Not requested") }
        override suspend fun export(folder: Uri, name: String, mode: CarLogExportMode, expected: Uri?,
            sessionId: String?, sessionStartOffset: Long): CarLogExportResult = error("Not requested")
    }

    @Test fun journalUpdatesAreVisibleWithoutAnotherMutableCopy() {
        val journal = Journal()
        val vm = DiagnosticViewModel(journal, Exports(), MutableStateFlow(MileagePermissionState()),
            MutableStateFlow(null), {}, {}, {})
        assertSame(journal.state, vm.state)
        journal.state.value = DiagnosticState(selectedCarId = "Tiguan")
        assertEquals("Tiguan", vm.state.value.selectedCarId)
        vm.selectCar("Kia")
        assertEquals(listOf("car:Kia"), journal.actions)
        // A selection is confirmed by the journal, not an optimistic mutable VM copy.
        assertEquals("Tiguan", vm.state.value.selectedCarId)
    }

    @Test fun permissionAndRetryHaveOneInjectedOwner() {
        val permission = MutableStateFlow(MileagePermissionState())
        var refreshes = 0
        var retries = 0
        val vm = DiagnosticViewModel(Journal(), Exports(), permission, MutableStateFlow(null),
            { refreshes++ }, { granted -> permission.value = MileagePermissionState(granted, !granted) },
            { retries++ })
        vm.onMileagePermissionResult(false)
        assertTrue(vm.mileagePermission.value.denied)
        vm.onMileagePermissionResult(true)
        assertTrue(vm.mileagePermission.value.granted)
        assertFalse(vm.mileagePermission.value.denied)
        vm.refreshMileagePermission()
        vm.retryMileage()
        assertEquals(1, refreshes)
        assertEquals(1, retries)
    }
}
