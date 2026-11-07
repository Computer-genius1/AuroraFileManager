package com.example.aurorafilemanager.model

import java.io.File

data class FileItem(
    val file: File,
    val name: String = file.name,
    val path: String = file.absolutePath,
    val isDirectory: Boolean = file.isDirectory,
    val size: Long = if (file.isDirectory) 0L else file.length(),
    val lastModified: Long = file.lastModified(),
    val extension: String = file.extension.lowercase()
) {
    val readableSize: String
        get() = if (isDirectory) {
            "--"
        } else {
            formatFileSize(size)
        }

    companion object {
        fun formatFileSize(size: Long): String {
            if (size <= 0) return "0 B"
            val units = arrayOf("B", "KB", "MB", "GB", "TB")
            val digitGroups = (Math.log10(size.toDouble()) / Math.log10(1024.0)).toInt()
            return java.text.DecimalFormat("#,##0.#").format(size / Math.pow(1024.0, digitGroups.toDouble())) + " " + units[digitGroups]
        }
    }
}

enum class SortBy {
    NAME, SIZE, DATE, TYPE
}

enum class SortOrder {
    ASCENDING, DESCENDING
}

enum class ViewMode {
    LIST, GRID
}
