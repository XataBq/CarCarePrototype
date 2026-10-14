package com.example.carcareprototype.car

import org.json.JSONObject
import java.time.Instant

internal data class CarLogRecord(
    val timestampMillis: Long,
    val message: String,
    val sessionId: String?,
    val lastSession: CarLogSession?
) {
    fun displayText(): String = "[${Instant.ofEpochMilli(timestampMillis)}] $message"

    fun toJson(): String = JSONObject().apply {
        put("timestampMillis", timestampMillis)
        put("message", message)
        put("sessionId", sessionId ?: JSONObject.NULL)
        put("carId", if (sessionId != null) lastSession?.carId ?: JSONObject.NULL else JSONObject.NULL)
        put("lastSession", lastSession?.let { session ->
            JSONObject().apply {
                put("id", session.id)
                put("carId", session.carId ?: JSONObject.NULL)
                put("startOffset", session.startOffset)
                put("disconnectedAtMillis", session.disconnectedAtMillis ?: JSONObject.NULL)
            }
        } ?: JSONObject.NULL)
    }.toString()

    companion object {
        fun fromJson(line: String): CarLogRecord {
            val json = JSONObject(line)
            val session = json.optJSONObject("lastSession")?.let {
                CarLogSession(
                    it.getString("id"),
                    if (it.isNull("carId")) null else it.getString("carId"),
                    it.getLong("startOffset"),
                    if (it.isNull("disconnectedAtMillis")) null else it.getLong("disconnectedAtMillis")
                )
            }
            return CarLogRecord(
                json.getLong("timestampMillis"), json.getString("message"),
                if (json.isNull("sessionId")) null else json.getString("sessionId"), session
            )
        }
    }
}
