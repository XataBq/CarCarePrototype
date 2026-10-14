package com.example.carcareprototype.car

internal enum class CarLogExportMode { CREATE, APPEND, REPLACE }

internal fun carLogFileName(input: String): String {
    val name = input.trim()
    require(name.isNotEmpty() && name != "." && name != "..") { "Enter a file name" }
    require(name.none { it.isISOControl() || it in "/\\:*?\"<>|" }) {
        "Use a file name without slashes or special characters"
    }
    val result = if (name.endsWith(".jsonl", ignoreCase = true)) name else "$name.jsonl"
    require(result.toByteArray(Charsets.UTF_8).size <= 240) { "File name is too long" }
    return result
}
