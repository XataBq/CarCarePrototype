package com.example.carcareprototype.bluetooth.mappers

import com.example.carcareprototype.bluetooth.BluetoothRecord
import org.json.JSONArray
import org.json.JSONObject

internal fun BluetoothRecord.toJson() = JSONObject().apply {
    put("id", id)
    put("timestampMillis", timestampMillis)
    put("kind", kind)
    put("message", message)
    deviceAddress?.let { put("deviceAddress", it) }
    deviceName?.let { put("deviceName", it) }
    uuids?.let { put("uuids", JSONArray(it)) }
    rssi?.let { put("rssi", it) }
}

internal fun JSONObject.toRecord(): BluetoothRecord {
    val array = optJSONArray("uuids")
    return BluetoothRecord(
        id = getString("id"),
        timestampMillis = getLong("timestampMillis"),
        kind = getString("kind"),
        message = getString("message"),
        deviceAddress = optString("deviceAddress").ifBlank { null },
        deviceName = optString("deviceName").ifBlank { null },
        uuids = array?.let { arr ->
            (0 until arr.length()).map { arr.getString(it) }
        },
        rssi = if (has("rssi") && !isNull("rssi")) getInt("rssi") else null
    )
}