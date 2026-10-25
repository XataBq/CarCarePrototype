package com.example.carcareprototype.diagnostics

import android.content.Context
import android.util.Log
import com.example.carcareprototype.car.CarDiagnosticStore
import com.example.carcareprototype.car.CarLogRecord
import com.example.carcareprototype.car.CarLogSnapshot
import com.example.carcareprototype.car.CarSessionTracker
import com.example.carcareprototype.car.MileageState
import com.example.carcareprototype.car.MileageStateScope
import com.example.carcareprototype.presentation.state.DiagnosticState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

object DiagnosticRepository {
    private sealed interface Action {
        data class Event(
            val timestamp: Long,
            val message: String,
            val kind: Kind = Kind.LOG,
            val token: String? = null,
            val carId: String? = null,
            val mileage: MileageState? = null,
            val revision: Long? = null
        ) : Action
        data class Snapshot(val sessionId: String?, val startOffset: Long, val result: CompletableDeferred<CarLogSnapshot>) : Action
    }
    private enum class Kind { LOG, CONNECT, DISCONNECT, SELECT_CAR, MILEAGE }

    private val queue = Channel<Action>(Channel.UNLIMITED)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var initialized = false
    private val _state = MutableStateFlow(DiagnosticState())
    val state = _state.asStateFlow()
    private val _mileageRevision = MutableStateFlow(0L)
    internal val mileageRevision = _mileageRevision.asStateFlow()

    @Synchronized
    fun initialize(context: Context) {
        if (initialized) return
        val app = context.applicationContext
        val store = CarDiagnosticStore(File(app.filesDir, "car_diagnostics.jsonl"))
        initialized = true
        scope.launch {
            val preferences = app.getSharedPreferences("car_diagnostics", 0)
            var selectedCarId = preferences.getString("car_id", null)
            val tracker = CarSessionTracker()
            val mileageScope = MileageStateScope()
            val restored = persist { store.readLast() }.mapNotNull { line ->
                runCatching { CarLogRecord.fromJson(line) }
                    .onFailure { Log.w(LOG_NAME, "Ignoring malformed history entry", it) }.getOrNull()
            }
            restored.lastOrNull()?.let { tracker.restore(it.lastSession, it.timestampMillis) }
            _state.update {
                it.copy(logs = restored.map(CarLogRecord::displayText), selectedCarId = selectedCarId.orEmpty(),
                    lastSessionId = tracker.latest?.id, lastSessionCarId = tracker.latest?.carId,
                    lastSessionStartOffset = tracker.latest?.startOffset ?: 0)
            }
            for (action in queue) {
                when (action) {
                    is Action.Snapshot -> {
                        if (!action.result.isActive) continue
                        try {
                            val snapshot = if (action.sessionId != null) {
                                store.snapshot(action.startOffset) {
                                    runCatching { CarLogRecord.fromJson(it).sessionId == action.sessionId }.getOrDefault(false)
                                }
                            } else store.snapshot()
                            action.result.complete(snapshot)
                        } catch (error: Exception) {
                            action.result.completeExceptionally(error)
                        }
                    }
                    is Action.Event -> {
                        if (action.token != null && action.kind != Kind.CONNECT &&
                            action.token != tracker.connectionToken) continue
                        var includeInSession = tracker.connectionToken != null
                        when (action.kind) {
                            Kind.CONNECT -> {
                                tracker.connect(action.token!!, selectedCarId, action.timestamp, persist { store.size() })
                                includeInSession = true
                                mileageScope.connect(action.token, selectedCarId)
                                _state.update { it.copy(androidAutoConnected = true, mileage = mileageScope.state) }
                            }
                            Kind.DISCONNECT -> {
                                includeInSession = tracker.disconnect(action.token!!, action.timestamp)
                                mileageScope.disconnect(action.token)
                                _state.update { it.copy(androidAutoConnected = false, mileage = mileageScope.state) }
                            }
                            Kind.SELECT_CAR -> {
                                persist { check(preferences.edit().putString("car_id", action.carId).commit()) }
                                selectedCarId = action.carId
                                tracker.changeCar(selectedCarId, persist { store.size() })
                                mileageScope.selectCar(selectedCarId)
                                _state.update { it.copy(selectedCarId = selectedCarId.orEmpty(), mileage = mileageScope.state) }
                            }
                            Kind.MILEAGE -> {
                                if (!mileageScope.update(action.token, action.revision, mileageRevision.value, action.mileage!!)) continue
                                _state.update { it.copy(mileage = mileageScope.state) }
                            }
                            Kind.LOG -> Unit
                        }
                        val record = CarLogRecord(action.timestamp, action.message,
                            if (includeInSession) tracker.latest?.id else null, tracker.latest)
                        val json = record.toJson()
                        persist { store.append(json) }
                        _state.update {
                            it.copy(logs = (it.logs + record.displayText()).takeLast(100),
                                lastSessionId = tracker.latest?.id, lastSessionCarId = tracker.latest?.carId,
                                lastSessionStartOffset = tracker.latest?.startOffset ?: 0)
                        }
                    }
                }
            }
        }
    }

    private suspend fun <T> persist(operation: () -> T): T {
        while (true) {
            try {
                val result = operation()
                _state.update { it.copy(storageError = null) }
                return result
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                Log.e(LOG_NAME, "Car journal IO failed; retrying", error)
                _state.update { it.copy(storageError = "Car logs are waiting to be saved: ${error.message}") }
                delay(2_000)
            }
        }
    }

    internal suspend fun snapshot(sessionId: String? = null, startOffset: Long = 0): CarLogSnapshot {
        val result = CompletableDeferred<CarLogSnapshot>()
        try {
            queue.send(Action.Snapshot(sessionId, startOffset, result))
            return result.await()
        } finally {
            result.cancel()
        }
    }

    fun selectCar(id: String) {
        val normalized = id.trim().ifEmpty { null }
        require(normalized == null || (normalized.length <= 80 && normalized.none(Char::isISOControl)))
        enqueue(Action.Event(System.currentTimeMillis(), "Selected car: ${normalized ?: "unidentified"}",
            Kind.SELECT_CAR, carId = normalized))
        // Callers are on main: enqueue the reset before waking the subscription collector.
        _mileageRevision.update { it + 1 }
    }

    fun connectCar(token: String) = enqueue(Action.Event(
        System.currentTimeMillis(), "Car Host Connected", Kind.CONNECT, token
    ))

    fun disconnectCar(token: String) = enqueue(Action.Event(
        System.currentTimeMillis(), "Car Host Disconnected", Kind.DISCONNECT, token
    ))

    internal fun updateMileage(mileage: MileageState, connectionToken: String, revision: Long, message: String) {
        enqueue(Action.Event(System.currentTimeMillis(),
            "connection=$connectionToken $message",
            Kind.MILEAGE, connectionToken, mileage = mileage, revision = revision))
    }

    fun log(message: String, connectionToken: String? = null) = enqueue(
        Action.Event(System.currentTimeMillis(), message, token = connectionToken)
    )

    private fun enqueue(event: Action.Event) {
        Log.d(LOG_NAME, event.message)
        check(queue.trySend(event).isSuccess)
    }

    private const val LOG_NAME = "CarTester"
}
