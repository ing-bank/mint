package com.ing.mint.android.probe

import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class LogCatProbeTest {
    val exampleLog = """
        07-22 16:39:18.175  1316  7143 D androidtc: Initializing SystemTextClassifier, type = System
        07-22 16:39:18.236   516  2078 D TextClassificationManagerService: Binding to ComponentInfo{com.google.android.ext.services/com.android.textclassifier.DefaultTextClassifierService}
        07-22 16:39:18.535   988  7145 D androidtc: Loading ModelFile { path=/etc/textclassifier/lang_id.model name=lang_id.model version=1 locales=* }
        07-22 16:39:18.726  1316  1325 W putmethod.lati: Reducing the number of considered missed Gc histogram windows from 614 to 100
        07-22 16:39:18.851   988  7145 D androidtc: Loading ModelFile { path=/data/misc/textclassifier/textclassifier.model name=textclassifier.model version=805 locales=en }
        07-22 16:39:18.903   988  7145 I tflite  : Initialized TensorFlow Lite runtime.
        07-22 16:39:19.042   988  7145 W System.err: java.lang.NumberFormatException: For input string: "6.0"
        07-22 16:39:19.043   988  7145 W System.err: 	at java.lang.Integer.parseInt(Integer.java:615)
        07-22 16:39:19.043   988  7145 W System.err: 	at java.lang.Integer.parseInt(Integer.java:650)
        07-22 16:40:41.152   425   425 E wifi_forwarder: qemu_pipe_open_ns:62: Could not connect to the 'pipe:qemud:wififorward' service: Invalid argument
        07-22 16:40:41.153   425   425 E wifi_forwarder: RemoteConnection failed to initialize: RemoteConnection failed to open pipe
    """.trimIndent()

    @Test
    fun testMatcher() = runBlocking {
        val results = LogCatProbe()
            .streamMatcher { exampleLog.byteInputStream() }
            .toList()
        assertEquals(11, results.size)
    }

    @Test
    fun testErrors() = runBlocking {
        val results = LogCatProbe()
            .streamMatcher { exampleLog.byteInputStream() }
            .filter { it.isError() }
            .toList()
        assertEquals(5, results.size)
    }
}
