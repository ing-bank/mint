package org.mint.api

import org.mint.android.AndroidState
import org.mint.lib.Oracle
import org.mint.lib.OracleCategory
import org.mint.lib.Probe

class NoopOracle : Oracle<AndroidState> {
    companion object {
        const val NAME: String = "NoopOracle"
        const val VERSION: String = "1.0.0"
        const val DESCRIPTION: String = "An Oracle that does nothing."

        val CATEGORIES: Set<OracleCategory> = setOf(
            OracleCategory.STABILITY,
            OracleCategory.PERFORMANCE,
        )
    }

    override val name: String = NAME
    override val version: String = VERSION
    override val description: String = DESCRIPTION
    override val categories: Set<OracleCategory> = CATEGORIES

    override fun probes(): Set<Class<out Probe<AndroidState>>> = setOf()

    override fun eval(state: AndroidState): AndroidState = state
}
