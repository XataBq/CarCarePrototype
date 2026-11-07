package com.example.carcareprototype.car

import java.util.UUID

internal interface MileageBackend {
    fun register(callback: (MileageReply) -> Unit)
    fun remove()
}

internal fun interface MileageScheduler {
    fun schedule(delayMillis: Long, action: () -> Unit): () -> Unit
}

internal class MileageSubscription(
    private val backend: MileageBackend,
    private val scheduler: MileageScheduler,
    private val elapsedMillis: () -> Long,
    private val wallMillis: () -> Long,
    private val permitted: () -> Boolean,
    private val scopeCurrent: () -> Boolean,
    private val publish: (MileageState, String) -> Unit,
    private val log: (String) -> Unit,
) {
    var state = MileageState()
        private set
    private var active = false
    private var attempt: String? = null
    private var registered = false
    private var cancelTimeout: (() -> Unit)? = null
    private var callbackReceived = false
    private var validReceived = false
    private var registeredAt = 0L

    fun start(reason: String) {
        active = true
        if (!scopeCurrent()) return
        val granted = permitted()
        log("Mileage permission granted=$granted reason=$reason")
        if (!granted) {
            remove("permission missing")
            status("PERMISSION_REQUIRED")
            return
        }
        if (attempt != null) return
        if (!remove("before registration")) return
        attempt = UUID.randomUUID().toString()
        val id = attempt!!
        registeredAt = elapsedMillis()
        callbackReceived = false
        validReceived = false
        state = state.copy(attemptId = id)
        status("SUBSCRIBING", "reason=$reason registeredWallMillis=${wallMillis()} elapsedMillis=$registeredAt")
        cancelTimeout = scheduler.schedule(CALLBACK_TIMEOUT_MS) {
            if (active && scopeCurrent() && attempt == id && !validReceived) {
                if (!permitted()) {
                    remove("permission revoked at timeout")
                    status("PERMISSION_REQUIRED")
                } else {
                    status(if (callbackReceived) "NO_VALID_DATA_TIMEOUT" else "NO_CALLBACK_TIMEOUT",
                        "waitedMillis=${elapsedMillis() - registeredAt} lastRawStatus=${state.lastReply?.rawStatus}")
                }
            }
        }
        try {
            registered = true // Also clean up a partially successful registration if the API throws.
            backend.register { reply -> receive(id, reply) }
            log("Mileage listener registered attempt=$id")
        } catch (error: Exception) {
            remove("registration failed")
            status(if (error is SecurityException) "PERMISSION_REQUIRED" else "ERROR",
                "registration=${error.javaClass.simpleName}: ${error.message}")
        }
    }

    fun retry() {
        log("Mileage retry requested active=$active attempt=$attempt")
        if (!active || !scopeCurrent()) {
            log("Mileage retry rejected: session is not STARTED or identity changed")
            if (scopeCurrent()) status("SESSION_INACTIVE")
            return
        }
        if (!permitted()) {
            remove("retry without permission")
            status("PERMISSION_REQUIRED")
            return
        }
        if (attempt != null && state.requestStatus == "SUBSCRIBING") {
            log("Mileage retry coalesced: already waiting for attempt=$attempt")
            return
        }
        if (remove("manual retry")) start("manual retry")
    }

    fun permissionDenied() {
        if (!active || !scopeCurrent()) return
        remove("permission denied")
        status("PERMISSION_DENIED")
    }

    fun permissionRequestFailed(error: Exception) {
        if (active && scopeCurrent()) status("ERROR", "permission request failed: ${error.message}")
    }

    fun stop(reason: String = "ON_STOP") {
        active = false
        remove(reason)
        status("STOPPED", reason)
    }

    fun resetIdentity() {
        // Cleanup may publish an error. Clear the old car's reading before any such publication.
        state = MileageState()
        remove("car identity changed")
        status(if (active) "NOT_REQUESTED" else "STOPPED", "car identity changed; previous reading cleared")
        if (active) start("car identity changed")
    }

    private fun receive(id: String, reply: MileageReply) {
        if (!active || !scopeCurrent() || attempt != id) {
            log("Ignored obsolete mileage callback attempt=$id")
            return
        }
        if (!permitted()) {
            remove("permission revoked at callback")
            status("PERMISSION_REQUIRED")
            return
        }
        callbackReceived = true
        val previousTimestamp = state.lastReply?.rawTimestamp
        state = state.copy(lastReply = reply, receivedInCurrentAttempt = reply.valid)
        if (reply.valid) {
            validReceived = true
            cancelTimeout?.invoke()
            cancelTimeout = null
            state = state.copy(lastSuccess = MileageReading(reply.rawValueKm!!, reply.receivedAtMillis, id))
        }
        // A late valid response recovers from a timeout; a late invalid one preserves timeout diagnosis.
        val status = if (!reply.valid && state.requestStatus.endsWith("TIMEOUT")) "NO_VALID_DATA_TIMEOUT"
            else if (reply.rawStatus == 1 && !reply.valid) "INVALID_DATA" else reply.status
        state = state.copy(requestStatus = status)
        publish(state, "Mileage callback attempt=$id rawValue=${reply.rawValueKm} rawStatus=${reply.rawStatus} " +
            "interpreted=${reply.status} request=$status rawTimestamp=${reply.rawTimestamp} " +
            "receivedWallMillis=${reply.receivedAtMillis} elapsedMillis=${reply.elapsedRealtimeMillis} " +
            "elapsedNanos=${reply.elapsedRealtimeNanos} latencyMillis=${reply.elapsedRealtimeMillis - registeredAt} " +
            "timestampAheadOfElapsedMillis=${reply.rawTimestamp > reply.elapsedRealtimeMillis} " +
            "timestampRegressed=${previousTimestamp != null && reply.rawTimestamp < previousTimestamp} " +
            "verification=UNVERIFIED")
    }

    private fun remove(reason: String): Boolean {
        val old = attempt
        attempt = null // Invalidate callbacks before calling the host, including reentrant callbacks.
        cancelTimeout?.invoke()
        cancelTimeout = null
        if (!registered) return true
        return try {
            backend.remove()
            registered = false
            log("Mileage listener removed attempt=$old reason=$reason")
            true
        } catch (error: Exception) {
            status("ERROR", "listener removal failed: ${error.message}; reason=$reason")
            false
        }
    }

    private fun status(value: String, detail: String = "") {
        state = state.copy(requestStatus = value, receivedInCurrentAttempt = false)
        publish(state, "Mileage request=$value attempt=${state.attemptId} $detail")
    }

    companion object { const val CALLBACK_TIMEOUT_MS = 15_000L }
}
