package org.mint.android.probe

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Semaphore
import org.mint.android.AndroidState
import org.mint.android.xml.attribute
import org.mint.lib.ProbeCategory
import org.mint.lib.ProbeTimingCategory
import java.io.InputStream

// Note: not too happy with the state management here yet but at least it works
class LogCatProbe : AndroidProbe {
    companion object LogCatProbe {
        const val name: String = "LogCatProbe"
        const val version: String = "1"
        const val description: String = "A probe that monitors the log cat stream"

        internal const val error: String = "error"
        internal const val line = "line"
        internal const val date = "date"
        internal const val time = "time"
        internal const val level = "level"
        internal const val source = "source"
        internal const val message = "message"

        internal val frameSkipRegex = Regex("Skipped \\d+ frames!")
        internal const val TAG = "o.m.a.p.LogCatProbe"

        /** Find the errors that have been attached by this state. on which this probe attaches
         *  error elements. If no top level node is found, return null to indicate that the probe
         *  was not invoked at all instead of having found no errors.
         */
        fun errorsOf(state: AndroidState): Flow<LogLine>? {
            val probeResult = state.query { it.nodeName == name }

            if (probeResult.isEmpty()) {
                return null
            } else {
                assert(probeResult.size == 1) { "There should only be a single log probe container" }
                val errors = state.query { it.nodeName == error }
                return errors
                    .asFlow()
                    .map {
                        LogLine(
                            line = it.attribute(line) ?: "",
                            date = it.attribute(date) ?: "",
                            time = it.attribute(time) ?: "",
                            level = it.attribute(level) ?: "",
                            source = it.attribute(source) ?: "",
                            message = it.attribute(message) ?: "",
                        )
                    }
            }
        }
    }
    override val name: String = LogCatProbe.name
    override val version: String = LogCatProbe.version
    override val description: String = LogCatProbe.description
    override val categories: Set<ProbeCategory> = setOf(
        ProbeTimingCategory.PRE_ACTION,
    )

    // a rather arbitrary initial default capacity, which is is bigger than the default.
    // 33 log lines seems reasonable as an initial size, onwards we
    // just reuse the previous size
    private var interestingResults: MutableList<LogLine> = interestingResultsCollection(33)
    private var sem = Semaphore(1)
    private var job: Job? = null

    private val regex = Regex("""\s*(\d\d-\d\d)\s(\d\d:\d\d:\d\d\.\d\d\d)\s+\d+\s+\d+\s+([VDIWEA])\s+(.+?):\s+(.+)""")

    // ArrayList() add is O(1) unless a copy is needed
    private fun <T> interestingResultsCollection(capacity: Int): MutableList<T> =
        ArrayList(capacity)

    override fun start() {
        Log.d(TAG, "Probe started")
        job = streamLogCat()
            .filter { it.isError() }
            .onEach {
                sem.acquire()
                interestingResults.add(it)
                sem.release()
            }
            .launchIn(CoroutineScope(Dispatchers.IO + SupervisorJob()))
    }

    override fun stop() {
        Log.d(TAG, "Probe stopped")
        job?.cancel()
    }

    override fun measure(state: AndroidState): AndroidState = runBlocking {
        sem.acquire()
        val errors = interestingResults
        interestingResults = interestingResultsCollection(errors.size)
        sem.release()

        val probeNS = probeNode(state) {
            it.nodeName == LogCatProbe.name
        }

        for (e in errors) {
            probeNS.appendChildNode(error) {
                it.setAttribute(level, e.level)
                it.setAttribute(date, e.date)
                it.setAttribute(time, e.time)
                it.setAttribute(source, e.source)
                it.setAttribute(message, e.message)
                it.setAttribute(line, e.line)
            }
        }

        state
    }

    private fun streamLogCat(): Flow<LogLine> {
        // Clear the buffer so we don't start with 'old' data
        Runtime.getRuntime().exec("logcat -c")
        val lc: Process = Runtime.getRuntime().exec("logcat")
        return streamMatcher {
            lc.inputStream
        }.onCompletion {
            Log.d(TAG, "logcat process destroyed")
            lc.destroy()
        }
    }

    internal fun streamMatcher(streamProvider: () -> InputStream): Flow<LogLine> = flow {
        streamProvider()
            .bufferedReader()
            .useLines { seq ->
                seq.forEach { str ->
                    regex.find(str)?.let {
                        emit(
                            LogLine(
                                it.groupValues[0],
                                // 0 matches the whole line, the rest the individual groups
                                it.groupValues[1],
                                it.groupValues[2],
                                it.groupValues[3],
                                it.groupValues[4],
                                it.groupValues[5],
                            ),
                        )
                    }
                }
            }
    }.flowOn(Dispatchers.IO)

    data class LogLine(
        val line: String,
        val date: String,
        val time: String,
        val level: String,
        val source: String,
        val message: String,
    ) {
        fun isError(): Boolean =
            // All error entries
            level == "E" ||
                // Anything sent to stderr
                source == "System.err" ||
                // Choreographer frame skip messages
                (message == "Choreographer" && frameSkipRegex.matches(message))
    }
}
