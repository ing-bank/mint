package org.mint.android.probe

import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Test

class CPUProbeTest {
    private val exampleCpuInfoDump = """
        6.9% 364/android.hardware.bluetooth@1.1-service.sim: 0% user + 6.8% kernel
        2.8% 22748/system_server: 0.5% user + 2.3% kernel / faults: 3979 minor 74 major
        4.5% TOTAL: 0.5% user + 3.8% kernel + 0% iowait + 0.1% softirq
    """.trimIndent()

    @Test
    fun testMatcher() = runBlocking {
        val results = CPUProbe()
            .streamMatcher { exampleCpuInfoDump.byteInputStream() }
            .toList()
        Assert.assertEquals(3, results.size)
    }

    @Test
    fun testEntryItems() = runBlocking {
        val results = CPUProbe()
            .streamMatcher { exampleCpuInfoDump.byteInputStream() }
            .toList()

        results.forEachIndexed { index, cpuInfo ->
            when (index) {
                0 -> {
                    Assert.assertEquals("6.9", cpuInfo.totalLoad)
                    Assert.assertEquals("364", cpuInfo.pid)
                    Assert.assertEquals(
                        "android.hardware.bluetooth@1.1-service.sim",
                        cpuInfo.packageName,
                    )
                    Assert.assertEquals("0", cpuInfo.userLoad)
                    Assert.assertEquals("6.8", cpuInfo.kernelLoad)
                }
                1 -> {
                    Assert.assertEquals("2.8", cpuInfo.totalLoad)
                    Assert.assertEquals("22748", cpuInfo.pid)
                    Assert.assertEquals("system_server", cpuInfo.packageName)
                    Assert.assertEquals("0.5", cpuInfo.userLoad)
                    Assert.assertEquals("2.3", cpuInfo.kernelLoad)
                }
                2 -> {
                    Assert.assertEquals("4.5", cpuInfo.totalLoad)
                    Assert.assertEquals("", cpuInfo.pid)
                    Assert.assertEquals("TOTAL", cpuInfo.packageName)
                    Assert.assertEquals("0.5", cpuInfo.userLoad)
                    Assert.assertEquals("3.8", cpuInfo.kernelLoad)
                }
            }
        }
    }
}
