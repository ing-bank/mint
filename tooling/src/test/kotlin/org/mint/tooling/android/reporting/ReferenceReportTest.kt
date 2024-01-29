package com.ing.mint.tooling.android.reporting

import org.gradle.testfixtures.ProjectBuilder
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File
import java.io.FileOutputStream

class ReferenceReportTest {
    // Convenient when working on the reporting feature.
    val devMode = false
    val devLocation = "/tmp/report/"

    val bufferSize = 4096
    var reportSource: File? = null
    var htmlReport: File? = null

    @Before
    fun setup() {
        reportSource = File.createTempFile("mint-test", ".xml")
        val inputStream = javaClass.getResourceAsStream("/notally.xml")
        val outputStream = FileOutputStream(reportSource!!)
        val bytes = ByteArray(bufferSize)
        var bytesRead: Int = inputStream!!.read(bytes)

        while (bytesRead > 0) {
            outputStream.write(bytes, 0, bytesRead)
            bytesRead = inputStream.read(bytes)
        }
    }

    @Test
    fun renderReport() {
        val logger = ProjectBuilder.builder().build().logger
        val renderer = ReferenceReportRenderer(logger)
        htmlReport = renderer.render(reportSource!!)

        if (devMode) {
            // also copy 'report' folder that is next to the html report
            htmlReport?.copyRecursively(File(devLocation), true)
        }
        assertNotNull(htmlReport)
        assertTrue(htmlReport!!.exists())
    }

    @After
    fun cleanup() {
        reportSource?.deleteOnExit()
        htmlReport?.deleteOnExit()
    }
}
