package com.ing.mint.android.probe

import android.util.Log
import androidx.test.platform.app.InstrumentationRegistry
import com.ing.mint.android.AndroidState
import com.ing.mint.lib.ProbeCategory
import com.ing.mint.lib.ProbeTimingCategory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Semaphore
import java.io.FileInputStream
import java.io.InputStream

class CPUProbe : AndroidProbe {
    companion object {
        const val TAG = "o.m.a.p.CPUProbe"

        // region Probe Details
        const val NAME: String = "cpu-probe"
        const val VERSION: String = "1.0.0"
        const val DESCRIPTION: String = "This probe monitors the CPU usage of the device."
        // endregion

        // region Filters
        // For Overall Total
        const val NAME_OVERALL_TOTAL = "TOTAL"
        // endregion

        // region Configuration
        // Count for Top Entries in the Probe Metrics
        const val COUNT_TOP_ENTRIES = 10
        // endregion

        // region Constants for XML Node Elements
        const val ELEMENT_CPU_INFO = "cpu-info"
        const val ELEMENT_TOTAL = "total"
        const val ELEMENT_TARGET_APP = "target-app"
        const val ELEMENT_TEST_APP = "test-app"
        const val ELEMENT_TOP_ENTRIES = "top-entries"
        const val ELEMENT_ENTRY = "entry"
        // endregion

        // region Constants for XML Attributes
        const val ATTR_TOTAL_LOAD = "total-load"
        const val ATTR_PID = "pid"
        const val ATTR_PACKAGE_NAME = "package-name"
        const val ATTR_USER_LOAD = "user-load"
        const val ATTR_KERNEL_LOAD = "kernel-load"
        // endregion

        // region Shell Commands
        // Command for CPU Info
        const val COMMAND_CPU_INFO = "dumpsys cpuinfo"
        // endregion

        // region Regex Patterns
        // For individual entries (cpu processes)
        const val PATTERN_INDIVIDUAL =
            "(\\d+.?\\d*%)\\s(\\d+)/?(.+:\\s)?(\\d+.?\\d*%)(\\suser)(\\s\\+\\s)(\\d+.?\\d*%)(\\skernel)"

        // For overall total cpu usage
        const val PATTERN_OVERALL =
            "(\\d+.?\\d*%)(\\sTOTAL):\\s(\\d+.?\\d*%)?(\\suser)?(\\s\\+\\s)?(\\d+.?\\d*%)?(\\skernel)?"
        // endregion
    }

    override val name: String = NAME
    override val version: String = VERSION
    override val description: String = DESCRIPTION
    override val categories: Set<ProbeCategory> = setOf(
        ProbeTimingCategory.PRE_ACTION,
    )

    private var targetApplicationPackageName: String? = null
    private var testApplicationPackageName: String? = null

    private var semaphore = Semaphore(1)
    private var job: Job? = null

    private var cpuInfoResults = ArrayList<CpuInfo>()
    private val individualEntryRegex = Regex(PATTERN_INDIVIDUAL)
    private val overallTotalRegex = Regex(PATTERN_OVERALL)

    override fun start() {
        Log.d(TAG, "CPU Probe started")

        val instrumentationRegistry = InstrumentationRegistry.getInstrumentation()

        targetApplicationPackageName = instrumentationRegistry.targetContext.packageName
        testApplicationPackageName = instrumentationRegistry.context.packageName

        job = streamCpuInfo().onEach { cpuInfo ->
            semaphore.acquire()
            cpuInfoResults.add(cpuInfo)
            semaphore.release()
        }.launchIn(CoroutineScope(Dispatchers.IO + SupervisorJob()))
    }

    override fun stop() {
        Log.d(TAG, "CPU Probe stopped")
        job?.cancel()
    }

    override fun measure(state: AndroidState): AndroidState {
        return runBlocking {
            val overallTotalEntry = try {
                cpuInfoResults.first { cpuInfo ->
                    cpuInfo.packageName == NAME_OVERALL_TOTAL
                }
            } catch (e: Exception) {
                null
            }

            val targetApplicationEntry = try {
                cpuInfoResults.first { cpuInfo ->
                    cpuInfo.packageName == "$targetApplicationPackageName"
                }
            } catch (e: Exception) {
                null
            }

            val testApplicationEntry = try {
                cpuInfoResults.first { cpuInfo ->
                    cpuInfo.packageName == testApplicationPackageName
                }
            } catch (e: Exception) {
                null
            }

            val namespace = probeNode(state) {
                it.nodeName == name
            }

            val rootElement = state.derive(state.createElement(ELEMENT_CPU_INFO))

            overallTotalEntry?.let { cpuInfo ->
                rootElement.appendChildNode(ELEMENT_TOTAL) {
                    it.setAttribute(ATTR_TOTAL_LOAD, cpuInfo.totalLoad)
                    it.setAttribute(ATTR_USER_LOAD, cpuInfo.userLoad)
                    it.setAttribute(ATTR_KERNEL_LOAD, cpuInfo.kernelLoad)
                }
            }

            targetApplicationEntry?.let { cpuInfo ->
                rootElement.appendChildNode(ELEMENT_TOTAL) {
                    it.setAttribute(ATTR_TOTAL_LOAD, cpuInfo.totalLoad)
                    it.setAttribute(ATTR_PID, cpuInfo.pid)
                    it.setAttribute(ATTR_PACKAGE_NAME, cpuInfo.packageName)
                    it.setAttribute(ATTR_USER_LOAD, cpuInfo.userLoad)
                    it.setAttribute(ATTR_KERNEL_LOAD, cpuInfo.kernelLoad)
                }
            }

            testApplicationEntry?.let { cpuInfo ->
                rootElement.appendChildNode(ELEMENT_TOTAL) {
                    it.setAttribute(ATTR_TOTAL_LOAD, cpuInfo.totalLoad)
                    it.setAttribute(ATTR_PID, cpuInfo.pid)
                    it.setAttribute(ATTR_PACKAGE_NAME, cpuInfo.packageName)
                    it.setAttribute(ATTR_USER_LOAD, cpuInfo.userLoad)
                    it.setAttribute(ATTR_KERNEL_LOAD, cpuInfo.kernelLoad)
                }
            }

            state.appendChildNode(ELEMENT_TOP_ENTRIES) { topEntriesElement ->
                cpuInfoResults.run {
                    filter { criteria ->
                        criteria.packageName != NAME_OVERALL_TOTAL
                    }.sortedByDescending { criteria ->
                        criteria.totalLoad
                    }.take(COUNT_TOP_ENTRIES).forEach { cpuInfo ->
                        state.derive(topEntriesElement).appendChildNode(ELEMENT_ENTRY) {
                            it.setAttribute(ATTR_TOTAL_LOAD, cpuInfo.totalLoad)
                            it.setAttribute(ATTR_PID, cpuInfo.pid)
                            it.setAttribute(ATTR_PACKAGE_NAME, cpuInfo.packageName)
                            it.setAttribute(ATTR_USER_LOAD, cpuInfo.userLoad)
                            it.setAttribute(ATTR_KERNEL_LOAD, cpuInfo.kernelLoad)
                        }
                    }
                }
            }

            namespace.node.appendChild(rootElement.node)

            state
        }
    }

    internal fun streamMatcher(provider: () -> InputStream): Flow<CpuInfo> = flow {
        provider().bufferedReader().useLines { lines ->
            lines.forEach { line ->
                individualEntryRegex.find(line)?.let { matchResult ->
                    emit(
                        CpuInfo(
                            totalLoad = matchResult.groupValues[1].trimPercentage(),
                            pid = matchResult.groupValues[2].trim(),
                            packageName = matchResult.groupValues[3].trimColon(),
                            userLoad = matchResult.groupValues[4].trimPercentage(),
                            kernelLoad = matchResult.groupValues[7].trimPercentage(),
                        ),
                    )
                }

                overallTotalRegex.find(line)?.let { matchResult ->
                    emit(
                        CpuInfo(
                            totalLoad = matchResult.groupValues[1].trimPercentage(),
                            pid = "",
                            packageName = matchResult.groupValues[2].trim(),
                            userLoad = matchResult.groupValues[3].trimPercentage(),
                            kernelLoad = matchResult.groupValues[6].trimPercentage(),
                        ),
                    )
                }
            }
        }
    }.flowOn(Dispatchers.IO)

    private fun streamCpuInfo(): Flow<CpuInfo> {
        val automation = InstrumentationRegistry.getInstrumentation().uiAutomation
        val parcelFileDescriptor = automation.executeShellCommand(COMMAND_CPU_INFO)
        return streamMatcher {
            FileInputStream(parcelFileDescriptor.fileDescriptor)
        }
    }

    private fun String.trimColon(): String {
        val string = this.trim()
        return if (string.endsWith(":")) {
            string.dropLast(1)
        } else {
            string
        }
    }

    private fun String.trimPercentage(): String {
        val string = this.trim()
        return if (string.endsWith("%")) {
            string.dropLast(1)
        } else {
            string
        }
    }

    // Although the load values are conceptually doubles, we have read them as
    // string, parse them as doubles and use them as string again if we want to use them
    // as such. Therefore, it's just a string.
    data class CpuInfo(
        val totalLoad: String,
        val pid: String,
        val packageName: String,
        val userLoad: String,
        val kernelLoad: String,
    )
}
