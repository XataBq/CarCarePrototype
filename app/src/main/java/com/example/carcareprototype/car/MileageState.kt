package com.example.carcareprototype.car

data class MileageReply(
    val rawValueKm: Double?,
    val rawStatus: Int,
    val rawTimestamp: Long,
    val receivedAtMillis: Long,
    val elapsedRealtimeMillis: Long,
    val elapsedRealtimeNanos: Long,
) {
    val status: String get() = when (rawStatus) {
        0 -> "UNKNOWN"
        1 -> "SUCCESS"
        2 -> "UNIMPLEMENTED"
        3 -> "UNAVAILABLE"
        else -> "UNKNOWN_$rawStatus"
    }
    val valid: Boolean get() = rawStatus == 1 && rawValueKm != null && rawValueKm.isFinite() && rawValueKm >= 0
}

data class MileageReading(val valueKm: Double, val receivedAtMillis: Long, val attemptId: String)

data class MileageState(
    val requestStatus: String = "NOT_REQUESTED",
    val attemptId: String? = null,
    val lastReply: MileageReply? = null,
    val lastSuccess: MileageReading? = null,
    val receivedInCurrentAttempt: Boolean = false,
) {
    val freshness: String get() = when {
        lastSuccess == null -> "NO_READING"
        receivedInCurrentAttempt -> "RECEIVED_THIS_ATTEMPT (sensor age unverified)"
        else -> "PREVIOUS_READING"
    }
    val verification: String get() = "UNVERIFIED"
}
