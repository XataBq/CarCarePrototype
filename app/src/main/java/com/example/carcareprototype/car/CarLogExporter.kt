package com.example.carcareprototype.car

import android.content.ContentResolver
import android.net.Uri
import android.provider.DocumentsContract

internal class CarLogExporter(private val resolver: ContentResolver) {
    fun find(tree: Uri, name: String): Uri? {
        val children = DocumentsContract.buildChildDocumentsUriUsingTree(
            tree, DocumentsContract.getTreeDocumentId(tree)
        )
        val columns = arrayOf(
            DocumentsContract.Document.COLUMN_DOCUMENT_ID,
            DocumentsContract.Document.COLUMN_DISPLAY_NAME,
            DocumentsContract.Document.COLUMN_MIME_TYPE
        )
        val matches = mutableListOf<Uri>()
        val cursor = resolver.query(children, columns, null, null, null)
            ?: error("Could not read folder. Choose it again.")
        cursor.use {
            while (it.moveToNext()) {
                if (it.getString(1) == name) {
                    check(it.getString(2) != DocumentsContract.Document.MIME_TYPE_DIR) {
                        "A folder has this name. Choose a different file name."
                    }
                    matches += DocumentsContract.buildDocumentUriUsingTree(tree, it.getString(0))
                }
            }
        }
        check(matches.size <= 1) { "Several files have this name. Choose a different name." }
        return matches.singleOrNull()
    }

    fun write(
        tree: Uri,
        name: String,
        mode: CarLogExportMode,
        expected: Uri?,
        snapshot: CarLogSnapshot
    ): Uri {
        val current = find(tree, name)
        check(current == expected) { "Destination changed. Check the name and try again." }
        check(mode != CarLogExportMode.CREATE || current == null) { "File already exists" }
        check(mode == CarLogExportMode.CREATE || current != null) { "File no longer exists" }
        val destination = current ?: DocumentsContract.createDocument(
            resolver,
            DocumentsContract.buildDocumentUriUsingTree(tree, DocumentsContract.getTreeDocumentId(tree)),
            "application/x-ndjson",
            name
        ) ?: error("Could not create file")
        try {
            val needsNewline = mode == CarLogExportMode.APPEND && needsSeparator(destination)
            val stream = resolver.openOutputStream(
                destination, if (mode == CarLogExportMode.APPEND) "wa" else "wt"
            ) ?: error("Could not open destination file")
            stream.use {
                if (needsNewline) it.write('\n'.code)
                snapshot.writeTo(it)
            }
        } catch (error: Exception) {
            if (current == null) runCatching { DocumentsContract.deleteDocument(resolver, destination) }
            throw error
        }
        return destination
    }

    private fun needsSeparator(uri: Uri): Boolean {
        val input = resolver.openInputStream(uri) ?: error("Could not read existing file")
        return input.use {
            val buffer = ByteArray(8192)
            var last = -1
            while (true) {
                val count = it.read(buffer)
                if (count < 0) break
                if (count > 0) last = buffer[count - 1].toInt()
            }
            last != -1 && last != '\n'.code
        }
    }
}
