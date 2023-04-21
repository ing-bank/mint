package org.mint.android.probe

import android.util.Log
import org.mint.android.AndroidState
import org.mint.android.ApplicationMonitor
import org.mint.android.Observer
import org.mint.android.xml.ThrowableAttributes
import org.mint.lib.ProbeCategory
import org.mint.lib.ProbeTimingCategory

/**
 * Probe that detects application crashes by attaching itself to the relevant [ApplicationMonitor] to be
 * notified of uncaught application exceptions.
 */
class CrashProbe(private val applicationMonitors: Set<ApplicationMonitor<in Any>>) : AndroidProbe, Observer<Throwable> {
    companion object CrashProbe {
        const val TAG: String = "o.m.a.p.CrashProbe"
        const val name: String = "CrashProbe"
        const val version: String = "1"
        const val description: String = "A probe that detects app crashes"
    }

    override val name: String = CrashProbe.name
    override val version: String = CrashProbe.version
    override val description: String = CrashProbe.description
    override val categories: Set<ProbeCategory> = setOf(
        ProbeTimingCategory.POST_ACTION,
    )

    private var throwables: List<Throwable> = listOf()

    override fun start() {
        Log.d(TAG, "Probe started")
        filterRelevant(applicationMonitors).forEach { it.attach(this) }
    }

    override fun stop() {
        Log.d(TAG, "Probe stopped")
        filterRelevant(applicationMonitors).forEach { it.detach(this) }
    }

    override fun measure(state: AndroidState): AndroidState {
        val probeNS = probeNode(state) {
            it.nodeName == CrashProbe.name
        }

        throwables.forEach {
            probeNS.appendChildNode("exception") { exceptionNode ->
                ThrowableAttributes.apply(exceptionNode, it)
            }
        }

        // reset
        throwables = listOf()

        return state
    }

    override fun update(t: Throwable?) {
        t?.let { throwables = throwables + it }
    }

    @Suppress("UNCHECKED_CAST")
    private inline fun <reified T> filterRelevant(s: Set<ApplicationMonitor<in T>>): Set<ApplicationMonitor<Throwable>> {
        return s.filter { it.type() == Throwable::class.java }
            .map { it as ApplicationMonitor<Throwable> }
            .toSet()
    }
}
