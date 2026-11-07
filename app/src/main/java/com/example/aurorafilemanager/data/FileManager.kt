package com.example.aurorafilemanager.data

import android.os.Environment
import com.example.aurorafilemanager.model.FileItem
import java.io.File
import java.io.IOException

object FileManager {

    val rootStorage: File
        get() = try {
            Environment.getExternalStorageDirectory()
        } catch (e: Exception) {
            File("/storage/emulated/0")
        }

    private val commonRootPaths by lazy {
        val ext = try {
            Environment.getExternalStorageDirectory().absolutePath
        } catch (e: Exception) {
            "/storage/emulated/0"
        }
        listOf("/storage", ext, "/mnt", "/system", "/data", "/vendor", "/apex", "/dev", "/etc", "/proc", "/sys", "/product", "/odm", "/oem")
    }

    fun getQuickAccessFolders(): List<Pair<String, File>> {
        val externalDir = rootStorage
        val defaultFolders = mutableListOf(
            "Root Directory" to File("/"),
            "Internal Storage" to externalDir
        )

        val targetFolders = listOf(
            "Documents" to try { Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) } catch (_: Exception) { File(externalDir, "Documents") },
            "Downloads" to try { Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) } catch (_: Exception) { File(externalDir, "Download") },
            "Pictures" to try { Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) } catch (_: Exception) { File(externalDir, "Pictures") },
            "Photos / DCIM" to try { Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) } catch (_: Exception) { File(externalDir, "DCIM") },
            "Videos" to try { Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES) } catch (_: Exception) { File(externalDir, "Movies") },
            "Music" to try { Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC) } catch (_: Exception) { File(externalDir, "Music") }
        )

        for ((name, dir) in targetFolders) {
            val validDir = if (dir != null && dir.exists()) {
                dir
            } else {
                val fallback = when (name) {
                    "Documents" -> File(externalDir, "Documents")
                    "Downloads" -> File(externalDir, "Download")
                    "Pictures" -> File(externalDir, "Pictures")
                    "Photos / DCIM" -> File(externalDir, "DCIM")
                    "Videos" -> File(externalDir, "Movies")
                    "Music" -> File(externalDir, "Music")
                    else -> File(externalDir, name)
                }
                if (!fallback.exists()) {
                    try { fallback.mkdirs() } catch (_: Exception) {}
                }
                fallback
            }
            if (validDir.exists()) {
                defaultFolders.add(name to validDir)
            }
        }

        // Automatically discover physical SD cards and USB OTG storage mounted in /storage
        try {
            val storageMounts = File("/storage")
            if (storageMounts.exists()) {
                storageMounts.listFiles()?.forEach { mount ->
                    if ((mount.isDirectory || !mount.isFile) && mount.name != "emulated" && mount.name != "self") {
                        defaultFolders.add("SD Card / Storage (${mount.name})" to mount)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return defaultFolders
    }

    fun isSystemFile(file: File): Boolean {
        val path = file.absolutePath
        return path.startsWith("/system") ||
               path.startsWith("/boot") ||
               path.startsWith("/vendor") ||
               path.startsWith("/apex") ||
               path.startsWith("/sbin") ||
               path == "/" ||
               path.startsWith("/etc")
    }

    fun isProtectedDirectory(file: File): Boolean {
        val path = file.absolutePath.lowercase(java.util.Locale.getDefault())
        val name = file.name.lowercase(java.util.Locale.getDefault())
        return name == "android" ||
               path == "/storage/emulated/0/android" ||
               path.startsWith("/storage/emulated/0/android/") ||
               path.endsWith("/android") ||
               path.contains("/android/") ||
               path == "/" ||
               path == "/system" || path.startsWith("/system/") ||
               path == "/vendor" || path.startsWith("/vendor/") ||
               path == "/apex" || path.startsWith("/apex/") ||
               path == "/etc" || path.startsWith("/etc/") ||
               path == "/boot" || path.startsWith("/boot/")
    }

    fun listFiles(directory: File, showHidden: Boolean = false): List<FileItem> {
        if (!directory.exists()) return emptyList()

        val rawFiles = directory.listFiles()
        val items: List<FileItem> = if (rawFiles != null) {
            rawFiles.map { FileItem(it) }
        } else {
            // Try root shell execution first
            val rootItems = execRootLs(directory)
            if (rootItems != null) {
                rootItems
            } else if (directory.absolutePath == "/") {
                // Fallback for root directory on non-rooted systems
                getRootFallbackItems()
            } else {
                emptyList()
            }
        }

        return items
            .asSequence()
            .filter { showHidden || !it.name.startsWith(".") }
            .toList()
    }

    fun canReadDirectory(directory: File): Boolean {
        if (!directory.exists()) return false
        if (directory.listFiles() != null) return true
        if (execRootLs(directory) != null) return true
        if (directory.absolutePath == "/") return true
        return false
    }

    private fun execRootLs(directory: File): List<FileItem>? {
        val path = directory.absolutePath
        val escapedPath = path.replace("\"", "\\\"")
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", "ls -1pa \"$escapedPath\""))
            val reader = process.inputStream.bufferedReader()
            val lines = reader.readLines()
            val exitCode = process.waitFor()
            if (exitCode == 0 && lines.isNotEmpty()) {
                val items = mutableListOf<FileItem>()
                for (rawLine in lines) {
                    val line = rawLine.trim()
                    if (line.isEmpty() || line == "./" || line == "../" || line == "." || line == "..") continue
                    val isDir = line.endsWith("/")
                    val name = if (isDir) line.dropLast(1) else line
                    val childFile = File(directory, name)
                    items.add(
                        FileItem(
                            file = childFile,
                            name = name,
                            path = childFile.absolutePath,
                            isDirectory = isDir,
                            size = if (isDir) 0L else childFile.length(),
                            lastModified = childFile.lastModified()
                        )
                    )
                }
                items
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun getRootFallbackItems(): List<FileItem> {
        val items = mutableListOf<FileItem>()
        for (path in commonRootPaths) {
            val file = File(path)
            if (file.exists()) {
                items.add(
                    FileItem(
                        file = file,
                        name = file.name,
                        path = file.absolutePath,
                        isDirectory = file.isDirectory || !file.isFile,
                        size = if (file.isDirectory) 0L else file.length(),
                        lastModified = file.lastModified()
                    )
                )
            }
        }
        return items
    }

    private fun execSuCommand(command: String): Boolean {
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", command))
            val exitCode = process.waitFor()
            exitCode == 0
        } catch (e: Exception) {
            false
        }
    }

    fun createDirectory(parent: File, name: String): Boolean {
        val newDir = File(parent, name)
        if (newDir.exists()) return false
        if (newDir.mkdirs()) return true
        val escapedPath = newDir.absolutePath.replace("\"", "\\\"")
        return execSuCommand("mkdir -p \"$escapedPath\"")
    }

    fun createFile(parent: File, name: String): Boolean {
        val newFile = File(parent, name)
        if (newFile.exists()) return false
        return try {
            if (newFile.createNewFile()) true else {
                val escapedPath = newFile.absolutePath.replace("\"", "\\\"")
                execSuCommand("touch \"$escapedPath\"")
            }
        } catch (e: IOException) {
            val escapedPath = newFile.absolutePath.replace("\"", "\\\"")
            execSuCommand("touch \"$escapedPath\"")
        }
    }

    fun deleteItem(file: File): Boolean {
        if (isProtectedDirectory(file)) {
            return false // Impossible to delete the Android folder or system root
        }
        val deleted = if (file.isDirectory) {
            file.deleteRecursively()
        } else {
            file.delete()
        }
        if (deleted) return true
        val escapedPath = file.absolutePath.replace("\"", "\\\"")
        return execSuCommand("rm -rf \"$escapedPath\"")
    }

    fun renameItem(file: File, newName: String): Boolean {
        val newFile = File(file.parentFile, newName)
        if (file.renameTo(newFile)) return true
        val escapedOld = file.absolutePath.replace("\"", "\\\"")
        val escapedNew = newFile.absolutePath.replace("\"", "\\\"")
        return execSuCommand("mv \"$escapedOld\" \"$escapedNew\"")
    }

    fun copyItem(source: File, destinationDir: File): Boolean {
        if (!source.exists() || !destinationDir.exists()) return false
        val dest = File(destinationDir, source.name)
        try {
            if (source.canonicalPath == dest.canonicalPath) return true
            if (source.isDirectory && destinationDir.canonicalPath.startsWith(source.canonicalPath)) {
                return false
            }
        } catch (_: Exception) {}

        return try {
            if (source.isDirectory) {
                if (source.copyRecursively(dest, overwrite = true)) return true
            } else {
                source.copyTo(dest, overwrite = true)
                return true
            }
            val escapedSrc = source.absolutePath.replace("\"", "\\\"")
            val escapedDst = destinationDir.absolutePath.replace("\"", "\\\"")
            execSuCommand("cp -r \"$escapedSrc\" \"$escapedDst\"")
        } catch (e: Exception) {
            val escapedSrc = source.absolutePath.replace("\"", "\\\"")
            val escapedDst = destinationDir.absolutePath.replace("\"", "\\\"")
            execSuCommand("cp -r \"$escapedSrc\" \"$escapedDst\"")
        }
    }

    fun moveItem(source: File, destinationDir: File): Boolean {
        if (!source.exists() || !destinationDir.exists()) return false
        val dest = File(destinationDir, source.name)
        try {
            if (source.canonicalPath == dest.canonicalPath) return true
            if (source.isDirectory && destinationDir.canonicalPath.startsWith(source.canonicalPath)) {
                return false
            }
        } catch (_: Exception) {}

        if (source.renameTo(dest)) return true
        val copied = copyItem(source, destinationDir)
        return if (copied) deleteItem(source) else false
    }

    fun getStorageInfo(file: File): Triple<Long, Long, Float> {
        val isExternalSdCard = file.exists() &&
                               file.absolutePath.startsWith("/storage/") &&
                               !file.absolutePath.startsWith("/storage/emulated") &&
                               !file.absolutePath.startsWith("/storage/self")

        if (isExternalSdCard) {
            return try {
                val stat = android.os.StatFs(file.path)
                val totalSpace = stat.blockCountLong * stat.blockSizeLong
                val freeSpace = stat.availableBlocksLong * stat.blockSizeLong
                val usedSpace = (totalSpace - freeSpace).coerceAtLeast(0L)
                val percent = if (totalSpace > 0) (usedSpace.toFloat() / totalSpace.toFloat()).coerceIn(0f, 1f) else 0f
                Triple(totalSpace, freeSpace, percent)
            } catch (_: Exception) {
                val totalSpace = file.totalSpace
                val freeSpace = file.freeSpace
                val usedSpace = (totalSpace - freeSpace).coerceAtLeast(0L)
                val percent = if (totalSpace > 0) (usedSpace.toFloat() / totalSpace.toFloat()).coerceIn(0f, 1f) else 0f
                Triple(totalSpace, freeSpace, percent)
            }
        }

        // Internal Storage: Compare /data partition and /storage/emulated/0 partition to get true device capacity
        val dataDir = try {
            Environment.getDataDirectory()
        } catch (_: Exception) {
            File("/data")
        }

        return try {
            val externalDir = rootStorage

            val dataStat = try { android.os.StatFs(dataDir.path) } catch (_: Exception) { null }
            val dataTotal = dataStat?.let { it.blockCountLong * it.blockSizeLong } ?: dataDir.totalSpace
            val dataFree = dataStat?.let { it.availableBlocksLong * it.blockSizeLong } ?: dataDir.freeSpace

            val extStat = try { android.os.StatFs(externalDir.path) } catch (_: Exception) { null }
            val extTotal = extStat?.let { it.blockCountLong * it.blockSizeLong } ?: externalDir.totalSpace
            val extFree = extStat?.let { it.availableBlocksLong * it.blockSizeLong } ?: externalDir.freeSpace

            val totalSpace = maxOf(dataTotal, extTotal)
            val freeSpace = if (dataTotal >= extTotal) dataFree else extFree
            val usedSpace = (totalSpace - freeSpace).coerceAtLeast(0L)
            val percent = if (totalSpace > 0) (usedSpace.toFloat() / totalSpace.toFloat()).coerceIn(0f, 1f) else 0f
            Triple(totalSpace, freeSpace, percent)
        } catch (_: Exception) {
            val totalSpace = maxOf(dataDir.totalSpace, rootStorage.totalSpace)
            val freeSpace = if (dataDir.totalSpace >= rootStorage.totalSpace) dataDir.freeSpace else rootStorage.freeSpace
            val usedSpace = (totalSpace - freeSpace).coerceAtLeast(0L)
            val percent = if (totalSpace > 0) (usedSpace.toFloat() / totalSpace.toFloat()).coerceIn(0f, 1f) else 0f
            Triple(totalSpace, freeSpace, percent)
        }
    }
}