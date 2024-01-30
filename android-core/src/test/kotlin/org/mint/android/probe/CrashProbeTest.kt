package com.ing.mint.android.probe

import com.ing.mint.android.AndroidConstants
import com.ing.mint.android.ApplicationMonitor
import com.ing.mint.android.StateBuilder
import com.ing.mint.android.xml.attribute
import com.ing.mint.android.xml.query
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class CrashProbeTest : StateBuilder {
    private val applicationMonitor: ApplicationMonitor<Any> = mock()

    @Test
    fun exceptionsAreStoredToState() {
        val probe = CrashProbe(setOf(applicationMonitor))
        val message1 = "test exception1"
        val e1 = RuntimeException(message1)
        val message2 = "test exception2"
        val e2 = RuntimeException(message2)
        val state = buildState("<View></View>")

        probe.update(e1)
        probe.update(e2)

        val result = probe.measure(state)
        val exception = result.query { it.nodeName == "exception" && it.parentNode.nodeName == CrashProbe.name }

        assertEquals(2, exception.size)
        assertTrue(exception.any { node -> node.query { it.attribute(AndroidConstants.ERROR_MESSAGE) == message1 }.size == 1 })
        assertTrue(exception.any { node -> node.query { it.attribute(AndroidConstants.ERROR_MESSAGE) == message2 }.size == 1 })
    }

    @Test
    fun noExceptionIsRecorded() {
        val probe = CrashProbe(setOf(applicationMonitor))
        val state = buildState("<View></View>")

        val result = probe.measure(state)
        val exception = result.query { it.nodeName == "exception" && it.parentNode.nodeName == CrashProbe.name }

        assertTrue(exception.isEmpty())
    }

    @Test
    @Suppress("UNCHECKED_CAST")
    fun probeIsAttachedToRelevantMonitors() {
        val throwableApplicationMonitor: ApplicationMonitor<Throwable> = mock()
        val other: ApplicationMonitor<Nothing> = mock()
        val monitors: Set<ApplicationMonitor<*>> = setOf(other, throwableApplicationMonitor)
        whenever(throwableApplicationMonitor.type()).thenReturn(Throwable::class.java)
        whenever(other.type()).thenReturn(Nothing::class.java)

        val probe = CrashProbe(monitors as Set<ApplicationMonitor<in Any>>)

        probe.start()
        probe.stop()

        verify(throwableApplicationMonitor).attach(probe)
        verify(throwableApplicationMonitor).detach(probe)
        verify(other, never()).attach(any())
        verify(other, never()).detach(any())
    }
}
