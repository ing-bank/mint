package org.mint.android.oracle

import kotlinx.coroutines.runBlocking
import org.mint.android.AndroidState
import org.mint.android.probe.CPUProbe
import org.mint.lib.Oracle
import org.mint.lib.OracleCategory
import org.mint.lib.Probe

class AndroidDeviceOracle : Oracle<AndroidState> {

    companion object {
        const val NAME: String = "AndroidDeviceOracle"
        const val VERSION: String = "1.0.0"
        const val DESCRIPTION: String = "An Oracle that monitors the Android Device for metrics."

        val CATEGORIES: Set<OracleCategory> = setOf(
            OracleCategory.STABILITY,
            OracleCategory.PERFORMANCE,
        )
    }

    override val name: String = NAME
    override val version: String = VERSION
    override val description: String = DESCRIPTION
    override val categories: Set<OracleCategory> = CATEGORIES

    override fun probes(): Set<Class<out Probe<AndroidState>>> = setOf(
        CPUProbe::class.javaObjectType,
    )

    override fun eval(state: AndroidState): AndroidState = runBlocking {
        // TODO: Discuss how cpu metrics affect the verdict.
        state
    }
}
