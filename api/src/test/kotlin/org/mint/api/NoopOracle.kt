package com.ing.mint.api

import com.ing.mint.android.AndroidState
import com.ing.mint.lib.Oracle
import com.ing.mint.lib.OracleCategory
import com.ing.mint.lib.Probe

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
