package com.example.carcareprototype.car

import java.io.File
import java.io.OutputStream
import java.io.RandomAccessFile
import java.io.FilterInputStream
import java.io.InputStream

internal class CarDiagnosticStore(private val file: File) {
    private val lock = Any()
    private var recovered = false

    fun append(line: String) = synchronized(lock) {
        require('\n' !in line && '\r' !in line) { "A log entry must occupy one line" }
        recover()
        RandomAccessFile(file, "rw").use { journal ->
            val start = journal.length()
            try {
                journal.seek(start)
                journal.write((line + "\n").toByteArray(Charsets.UTF_8))
            } catch (error: Exception) {
                // A failed write must not leave a partial entry before the next append.
                runCatching { journal.setLength(start) }
                    .onFailure { recovered = false; error.addSuppressed(it) }
                throw error
            }
        }
    }

    fun readLast(limit: Int = 100): List<String> = synchronized(lock) {
        require(limit >= 0)
        recover()
        if (limit == 0 || !file.exists()) return@synchronized emptyList()
        RandomAccessFile(file, "r").use { journal ->
            val end = journal.length()
            val start = tailStart(journal, limit)
            journal.seek(start)
            val bytes = ByteArray(Math.toIntExact(end - start))
            journal.readFully(bytes)
            bytes.toString(Charsets.UTF_8).lineSequence().filter { it.isNotEmpty() }.toList()
        }
    }

    fun size(): Long = synchronized(lock) {
        recover()
        file.length()
    }

    fun snapshot(startOffset: Long = 0, include: ((String) -> Boolean)? = null): CarLogSnapshot = synchronized(lock) {
        recover()
        require(startOffset in 0..file.length()) { "Invalid session offset" }
        CarLogSnapshot(file, startOffset, file.length() - startOffset, include)
    }

    private fun recover() {
        if (recovered) return
        if (file.exists()) {
            RandomAccessFile(file, "rw").use { journal ->
                val end = journal.length()
                if (end > 0) {
                    journal.seek(end - 1)
                    if (journal.read() != '\n'.code) journal.setLength(tailStart(journal, 0))
                }
            }
        }
        recovered = true
    }

    private fun tailStart(journal: RandomAccessFile, limit: Int): Long {
        var position = journal.length()
        var separators = 0
        val buffer = ByteArray(8192)
        while (position > 0) {
            val count = minOf(position, buffer.size.toLong()).toInt()
            position -= count
            journal.seek(position)
            journal.readFully(buffer, 0, count)
            for (index in count - 1 downTo 0) {
                if (buffer[index] == '\n'.code.toByte() && ++separators > limit) {
                    return position + index + 1
                }
            }
        }
        return 0
    }
}

internal class CarLogSnapshot(
    private val file: File,
    private val startOffset: Long,
    private val byteCount: Long,
    private val include: ((String) -> Boolean)?
) {
    fun writeTo(output: OutputStream) {
        if (byteCount > 0) {
            file.inputStream().use { input ->
                input.channel.position(startOffset)
                val bounded = SnapshotInputStream(input, byteCount)
                val filter = include
                if (filter == null) bounded.copyTo(output)
                else bounded.bufferedReader(Charsets.UTF_8).forEachLine { line ->
                    if (filter(line)) output.write((line + "\n").toByteArray(Charsets.UTF_8))
                }
            }
        }
    }
}

private class SnapshotInputStream(input: InputStream, private var remaining: Long) : FilterInputStream(input) {
    override fun read(): Int {
        if (remaining == 0L) return -1
        val value = super.read()
        check(value >= 0) { "Journal changed during export" }
        remaining--
        return value
    }

    override fun read(buffer: ByteArray, offset: Int, length: Int): Int {
        if (length == 0) return 0
        if (remaining == 0L) return -1
        val count = `in`.read(buffer, offset, minOf(remaining, length.toLong()).toInt())
        check(count > 0) { "Journal changed during export" }
        remaining -= count
        return count
    }
}
