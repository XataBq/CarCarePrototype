package com.example.carcareprototype.car

import android.content.ContentResolver
import android.content.pm.ProviderInfo
import android.database.Cursor
import android.database.MatrixCursor
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.provider.DocumentsContract
import android.provider.DocumentsProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class CarLogExportInstrumentedTest {
    private lateinit var directory: File
    private lateinit var exporter: CarLogExporter
    private val tree = DocumentsContract.buildTreeDocumentUri("car.logs.test", "root")

    @Before fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        directory = File(context.cacheDir, "car-export-test-${UUID.randomUUID()}").apply { mkdirs() }
        val provider = LocalTestDocumentsProvider(directory)
        provider.attachInfo(context, ProviderInfo().apply {
            authority = "car.logs.test"
            exported = true
            grantUriPermissions = true
            readPermission = "android.permission.MANAGE_DOCUMENTS"
            writePermission = "android.permission.MANAGE_DOCUMENTS"
        })
        exporter = CarLogExporter(ContentResolver.wrap(provider))
    }

    @After fun tearDown() { directory.deleteRecursively() }

    @Test fun jsonlRoundTripPreservesMultilineUnicodeAndSessionMetadata() {
        val session = CarLogSession("session", "Моя машина", 42, 1000)
        val record = CarLogRecord(2000, "First\nSecond\r\n\"quoted\" 🚗", "session", session)
        val line = record.toJson()
        assertFalse(line.contains('\n'))
        assertEquals(record, CarLogRecord.fromJson(line))
    }

    @Test fun createAppendAndReplaceUseRealDocumentStreams() {
        val journalFile = File(directory, "internal.jsonl")
        val store = CarDiagnosticStore(journalFile)
        store.append("{\"event\":1}")
        val first = store.snapshot()
        val name = "export.jsonl"
        val uri = exporter.write(tree, name, CarLogExportMode.CREATE, null, first)
        assertEquals(uri, exporter.find(tree, name))
        assertEquals("{\"event\":1}\n", File(directory, name).readText())
        exporter.write(tree, name, CarLogExportMode.APPEND, uri, first)
        assertEquals("{\"event\":1}\n{\"event\":1}\n", File(directory, name).readText())
        exporter.write(tree, name, CarLogExportMode.REPLACE, uri, first)
        assertEquals("{\"event\":1}\n", File(directory, name).readText())
        File(directory, name).writeText("{\"event\":0}")
        exporter.write(tree, name, CarLogExportMode.APPEND, uri, first)
        assertEquals("{\"event\":0}\n{\"event\":1}\n", File(directory, name).readText())
    }

    @Test fun unconfirmedCollisionDoesNotOverwriteExistingData() {
        val store = CarDiagnosticStore(File(directory, "internal.jsonl"))
        store.append("{}")
        File(directory, "existing.jsonl").writeText("keep me")
        assertThrows(IllegalStateException::class.java) {
            exporter.write(tree, "existing.jsonl", CarLogExportMode.CREATE, null, store.snapshot())
        }
        assertEquals("keep me", File(directory, "existing.jsonl").readText())
    }

    @Test fun sessionExportIncludesReconnectsAndExcludesOtherCarsAndPhoneEvents() {
        val store = CarDiagnosticStore(File(directory, "internal.jsonl"))
        val tracker = CarSessionTracker()
        fun record(message: String, inSession: Boolean = true) {
            store.append(CarLogRecord(0, message, if (inSession) tracker.latest?.id else null, tracker.latest).toJson())
        }
        tracker.connect("old", "A", 0, store.size())
        record("old car")
        tracker.disconnect("old", 1)
        tracker.connect("first", "B", 2, store.size())
        record("first connection")
        tracker.disconnect("first", 3)
        record("phone event", false)
        tracker.connect("second", "B", 1000, store.size())
        record("reconnection")
        val session = tracker.latest!!
        val snapshot = store.snapshot(session.startOffset) { CarLogRecord.fromJson(it).sessionId == session.id }
        record("after snapshot")
        exporter.write(tree, "session.jsonl", CarLogExportMode.CREATE, null, snapshot)
        val messages = File(directory, "session.jsonl").readLines().map { CarLogRecord.fromJson(it).message }
        assertEquals(listOf("first connection", "reconnection"), messages)
    }
}

/** Exercises the SAF adapter on Android without writing into the user's shared files. */
private class LocalTestDocumentsProvider(private val directory: File) : DocumentsProvider() {
    override fun onCreate() = true
    override fun queryRoots(projection: Array<out String>?): Cursor = MatrixCursor(projection ?: emptyArray())
    override fun isChildDocument(parentDocumentId: String, documentId: String) = parentDocumentId == "root"
    override fun queryDocument(documentId: String, projection: Array<out String>?): Cursor =
        cursor(projection).apply { addFile(if (documentId == "root") directory else File(directory, documentId)) }

    override fun queryChildDocuments(parentDocumentId: String, projection: Array<out String>?, sortOrder: String?): Cursor =
        cursor(projection).apply { directory.listFiles().orEmpty().forEach { addFile(it) } }

    override fun createDocument(parentDocumentId: String, mimeType: String, displayName: String): String {
        check(File(directory, displayName).createNewFile())
        return displayName
    }

    override fun deleteDocument(documentId: String) { check(File(directory, documentId).delete()) }
    override fun openDocument(documentId: String, mode: String, signal: CancellationSignal?): ParcelFileDescriptor =
        ParcelFileDescriptor.open(File(directory, documentId), ParcelFileDescriptor.parseMode(mode))

    private fun cursor(projection: Array<out String>?) = MatrixCursor(projection ?: arrayOf(
        DocumentsContract.Document.COLUMN_DOCUMENT_ID, DocumentsContract.Document.COLUMN_DISPLAY_NAME,
        DocumentsContract.Document.COLUMN_MIME_TYPE
    ))

    private fun MatrixCursor.addFile(file: File) {
        addRow(columnNames.map {
            when (it) {
                DocumentsContract.Document.COLUMN_DOCUMENT_ID -> if (file == directory) "root" else file.name
                DocumentsContract.Document.COLUMN_DISPLAY_NAME -> file.name
                DocumentsContract.Document.COLUMN_MIME_TYPE -> if (file.isDirectory)
                    DocumentsContract.Document.MIME_TYPE_DIR else "application/x-ndjson"
                DocumentsContract.Document.COLUMN_FLAGS -> DocumentsContract.Document.FLAG_SUPPORTS_WRITE
                DocumentsContract.Document.COLUMN_SIZE -> file.length()
                else -> null
            }
        }.toTypedArray<Any?>())
    }
}
