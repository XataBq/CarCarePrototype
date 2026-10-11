package com.example.carcareprototype.bluetooth

import android.content.Context
import android.net.Uri
import com.example.carcareprototype.bluetooth.mappers.toJson
import com.example.carcareprototype.bluetooth.mappers.toRecord
import org.json.JSONObject
import java.io.File

class BluetoothDiagnosticStore(context: Context) {
    private val appContext = context.applicationContext
    private val file = File(appContext.filesDir, "bluetooth_diagnostics.jsonl")

    fun readLast(limit: Int = 250): List<BluetoothRecord> {
        if (!file.exists()) return emptyList()
        return file.readLines(Charsets.UTF_8).takeLast(limit).mapNotNull { line ->
            runCatching { JSONObject(line).toRecord() }.getOrNull()
        }
    }

    fun append(record: BluetoothRecord) {
        file.appendText(record.toJson().toString() + "\n", Charsets.UTF_8)
        if (file.length() > 2_000_000L) {
            val recent = file.readLines(Charsets.UTF_8).takeLast(2_000)
            file.writeText(recent.joinToString("\n", postfix = "\n"), Charsets.UTF_8)
        }
    }

    fun clear() {
        file.writeText("", Charsets.UTF_8)
    }

    fun export(uri: Uri) {
        val output = appContext.contentResolver.openOutputStream(uri)
            ?: error("Could not open destination URI")
        output.use { stream ->
            if (file.exists()) file.inputStream().use { it.copyTo(stream) }
        }
    }
}
