package com.example.carcareprototype.car

import java.util.UUID

internal data class CarLogSession(
    val id: String,
    val carId: String?,
    val startOffset: Long,
    val disconnectedAtMillis: Long? = null
)

internal class CarSessionTracker {
    var latest: CarLogSession? = null
        private set
    var connectionToken: String? = null
        private set

    fun restore(session: CarLogSession?, lastEventAtMillis: Long) {
        latest = session?.copy(disconnectedAtMillis = session.disconnectedAtMillis ?: lastEventAtMillis)
        connectionToken = null
    }

    fun connect(token: String, carId: String?, nowMillis: Long, offset: Long) {
        if (connectionToken == token) return
        val previous = latest
        val gap = previous?.disconnectedAtMillis?.let { nowMillis - it }
        val reconnect = carId != null && carId == previous?.carId && gap != null && gap in 0..RECONNECT_TIMEOUT_MS
        latest = if (reconnect) previous!!.copy(disconnectedAtMillis = null)
        else CarLogSession(UUID.randomUUID().toString(), carId, offset)
        connectionToken = token
    }

    fun changeCar(carId: String?, offset: Long) {
        if (connectionToken == null || latest?.carId == carId) return
        latest = if (latest?.carId == null && carId != null) latest?.copy(carId = carId)
        else CarLogSession(UUID.randomUUID().toString(), carId, offset)
    }

    fun disconnect(token: String, nowMillis: Long): Boolean {
        if (connectionToken != token) return false
        latest = latest?.copy(disconnectedAtMillis = nowMillis)
        connectionToken = null
        return true
    }

    companion object {
        const val RECONNECT_TIMEOUT_MS = 120_000L
    }
}
