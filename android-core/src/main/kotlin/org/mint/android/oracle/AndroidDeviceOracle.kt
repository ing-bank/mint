package com.ing.mint.android.oracle

import com.ing.mint.android.AndroidState
import com.ing.mint.android.probe.CPUProbe
import com.ing.mint.lib.Oracle
import com.ing.mint.lib.OracleCategory
import com.ing.mint.lib.Probe
import kotlinx.coroutines.runBlocking

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
