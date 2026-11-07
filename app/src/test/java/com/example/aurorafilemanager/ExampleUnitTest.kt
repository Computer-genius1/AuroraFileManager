package com.example.aurorafilemanager

import com.example.aurorafilemanager.data.FileManager
import com.example.aurorafilemanager.ui.getMimeType
import org.junit.Test
import org.junit.Assert.*
import java.io.File

class ExampleUnitTest {
    @Test
    fun getMimeType_returnsExpectedTypes() {
        assertEquals("text/plain", getMimeType(File("test.txt")))
        assertEquals("text/plain", getMimeType(File("test.log")))
        assertEquals("text/plain", getMimeType(File("test.json")))
        assertEquals("application/pdf", getMimeType(File("doc.pdf")))
        assertEquals("image/*", getMimeType(File("photo.png")))
    }

    @Test
    fun rootDirectory_canBeRead() {
        val rootDir = File("/")
        assertTrue(FileManager.canReadDirectory(rootDir))
        val files = FileManager.listFiles(rootDir)
        assertFalse(files.isEmpty())
    }

    @Test
    fun getStorageInfo_returnsValidStorageInfo() {
        val info = FileManager.getStorageInfo(File("/"))
        assertNotNull(info)
        assertTrue(info.third in 0f..1f)
    }
}