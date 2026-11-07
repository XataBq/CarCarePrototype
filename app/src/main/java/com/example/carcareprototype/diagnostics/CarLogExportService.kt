package com.example.carcareprototype.diagnostics

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.example.carcareprototype.car.CarLogExportMode
import com.example.carcareprototype.car.CarLogExporter
import com.example.carcareprototype.car.CarLogSnapshot
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout

internal sealed interface CarLogExportResult {
    data class Conflict(val destination: Uri) : CarLogExportResult
    data class Saved(val destination: Uri) : CarLogExportResult
}

/** Owns the SAF destination and IO workflow; presentation owns only dialog state. */
internal interface CarLogExportOperations {
    val folder: Uri?
    suspend fun selectFolder(uri: Uri)
    suspend fun export(folder: Uri, name: String, mode: CarLogExportMode, expected: Uri?,
        sessionId: String?, sessionStartOffset: Long): CarLogExportResult
}

internal class CarLogExportService(
    context: Context,
    private val snapshot: suspend (String?, Long) -> CarLogSnapshot,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : CarLogExportOperations {
    private val resolver = context.applicationContext.contentResolver
    private val preferences = context.applicationContext.getSharedPreferences("car_log_export", 0)
    private val exporter = CarLogExporter(resolver)

    override val folder: Uri? get() = preferences.getString("folder", null)?.let(Uri::parse)

    override suspend fun selectFolder(uri: Uri) = withContext(ioDispatcher) {
        resolver.takePersistableUriPermission(
            uri, Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        )
        preferences.edit().putString("folder", uri.toString()).apply()
    }

    override suspend fun export(
        folder: Uri,
        name: String,
        mode: CarLogExportMode,
        expected: Uri?,
        sessionId: String?,
        sessionStartOffset: Long,
    ): CarLogExportResult {
        // Only CREATE needs a preliminary lookup to present the conflict dialog.
        // write() independently revalidates the destination for every mode.
        if (mode == CarLogExportMode.CREATE) {
            val existing = withContext(ioDispatcher) { exporter.find(folder, name) }
            if (existing != null) return CarLogExportResult.Conflict(existing)
        }
        val contents = withTimeout(SNAPSHOT_TIMEOUT_MS) { snapshot(sessionId, sessionStartOffset) }
        return withContext(ioDispatcher) {
            CarLogExportResult.Saved(exporter.write(folder, name, mode, expected, contents))
        }
    }

    private companion object { const val SNAPSHOT_TIMEOUT_MS = 30_000L }
}
