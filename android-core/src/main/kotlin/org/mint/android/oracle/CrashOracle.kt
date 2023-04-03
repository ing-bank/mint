package org.mint.android.oracle

import kotlinx.coroutines.runBlocking
import org.mint.android.AndroidConstants
import org.mint.android.AndroidState
import org.mint.android.probe.CrashProbe
import org.mint.lib.Oracle
import org.mint.lib.OracleCategory
import org.mint.lib.Probe
import org.mint.lib.Verdict

/**
 * Oracle that decides if a crash occurred based on the findings of the [CrashProbe]
 */
class CrashOracle : Oracle<AndroidState> {
    companion object CrashOracle {
        const val name: String = "CrashOracle"
        const val version: String = "1"
        const val description: String = "An oracle that detects app crashes"
        val categories: Set<OracleCategory> = setOf(
            OracleCategory.STABILITY
        )
        const val decision = "decision"
        const val verdict = "verdict"
        internal const val exception: String = "exception"
    }
    override val name: String = CrashOracle.name
    override val version: String = CrashOracle.version
    override val description: String = CrashOracle.description
    override val categories: Set<OracleCategory> = CrashOracle.categories

    override fun probes(): Set<Class<out Probe<AndroidState>>> = setOf(CrashProbe::class.java)

    override fun eval(state: AndroidState): AndroidState = runBlocking {
        val oracleNS = state.appendChildNode(name, AndroidConstants.ORACLE_NS)
        val verdict = state.appendChildNode(verdict)

        val probeResult = state.query { it.nodeName == CrashProbe.name }
        if (probeResult.isEmpty()) {
            verdict.setAttribute(decision, Verdict.DONT_KNOW.name)
        } else {
            assert(probeResult.size == 1) { "There should be only one crash probe container" }
            val finding = state.query {
                it.nodeName == exception && it.parentNode.nodeName == CrashProbe.name
            }
            if (finding.isEmpty()) {
                verdict.setAttribute(decision, Verdict.OK.name)
            } else {
                verdict.setAttribute(decision, Verdict.FAIL.name)
            }
        }

        oracleNS.appendChild(verdict)

        state
    }
}
