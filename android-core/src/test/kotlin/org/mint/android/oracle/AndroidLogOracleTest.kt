package org.mint.android.oracle

import org.junit.Assert.assertTrue
import org.junit.Test
import org.mint.android.StateBuilder
import org.mint.android.xml.attribute
import org.mint.lib.Verdict

class AndroidLogOracleTest : StateBuilder {
    @Test
    fun testVerdictCreationWithAnError() {
        val xml = """<View>
             <LogCatProbe xmlns="https://org.mint/espresso/probe">
               <error level="I" source="Choreographer" message="Skipped 94 frames!  The application may be doing too much work on its main thread." logline="07-26 14:42:05.150  8109  8109 I Choreographer: Skipped 94 frames!  The application may be doing too much work on its main thread."/>
               <error level="W" source="System.err" message="java.lang.NumberFormatException: For input string: &quot;6.0&quot;" logline="07-22 16:39:19.042   988  7145 W System.err: java.lang.NumberFormatException: For input string: &quot;6.0&quot;"/>
             </LogCatProbe>
           </View>
        """.trimIndent()
        val oracle = AndroidLogOracle()
        val result = oracle.eval(buildState(xml))
        val decision = result.query { it.attribute(AndroidLogOracle.decision)?.equals(Verdict.FAIL.name) ?: false }
        assertTrue(decision.size == 1)
    }

    @Test
    fun testVerdictCreation() {
        val xml = """<View>
             <LogCatProbe xmlns="https://org.mint/espresso/probe"/>
           </View>
        """.trimIndent()
        val oracle = AndroidLogOracle()
        val result = oracle.eval(buildState(xml))
        val decision = result.query { it.attribute(AndroidLogOracle.decision)?.equals(Verdict.OK.name) ?: false }
        assertTrue(decision.size == 1)
    }

    @Test
    fun testProbeNotExecuted() {
        val xml = """<View></View>""".trimIndent()
        val oracle = AndroidLogOracle()
        val result = oracle.eval(buildState(xml))
        val decision = result.query { it.attribute(AndroidLogOracle.decision)?.equals(Verdict.DONT_KNOW.name) ?: false }
        assertTrue(decision.size == 1)
    }
}
