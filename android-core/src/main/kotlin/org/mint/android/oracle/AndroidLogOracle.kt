package org.mint.android.oracle

import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onEmpty
import kotlinx.coroutines.runBlocking
import org.mint.android.AndroidConstants
import org.mint.android.AndroidState
import org.mint.android.FlowTools.toListFlow
import org.mint.android.probe.LogCatProbe
import org.mint.lib.Oracle
import org.mint.lib.OracleCategory
import org.mint.lib.Probe
import org.mint.lib.Verdict

class AndroidLogOracle : Oracle<AndroidState> {
    companion object AndroidLogOracle {
        const val name: String = "AndroidLogOracle"
        const val version: String = "1"
        const val description: String = "An oracle that considers the Android (system) log"
        val categories: Set<OracleCategory> = setOf(
            OracleCategory.STABILITY, // Most (error) log entries are about crashes
            OracleCategory.PERFORMANCE // Some log entries complain about slow render times
        )
        const val decision = "decision"
        const val verdict = "verdict"
    }
    override val name: String = AndroidLogOracle.name
    override val version: String = AndroidLogOracle.version
    override val description: String = AndroidLogOracle.description
    override val categories: Set<OracleCategory> = AndroidLogOracle.categories

    override fun probes(): Set<Class<out Probe<AndroidState>>> = setOf(
        LogCatProbe::class.javaObjectType
    )

    override fun eval(state: AndroidState): AndroidState = runBlocking {
        val verdict = state.createElement(verdict)
        val errors = LogCatProbe.errorsOf(state)

        if (errors == null) {
            // no probe results found, unknown verdict
            verdict.setAttribute(decision, Verdict.DONT_KNOW.name)
        } else {
            // Decide how to handle the errors
            errors
                .onEmpty { verdict.setAttribute(decision, Verdict.OK.name) }
                .toListFlow() // Note: this collects all items and waits for a completion signal before continuing!
                .filterNot { it.isEmpty() } // nat that we need to skip the empty list now that we collected a list
                .onEach { verdict.setAttribute(decision, Verdict.FAIL.name) } // TODO: really decide on how to implement the error handling
                .collect()
        }

        state.appendChildNode(name, AndroidConstants.ORACLE_NS) {
            it.appendChild(verdict)
        }

        state
    }
}
