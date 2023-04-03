package org.mint.android.oracle

import org.junit.Assert.assertTrue
import org.junit.Test
import org.mint.android.StateBuilder
import org.mint.android.xml.attribute
import org.mint.lib.Verdict

class CrashOracleTest : StateBuilder {
    @Test
    fun testFailVerdict() {
        val xml = """<View>
             <CrashProbe xmlns="https://org.mint/espresso/probe">
         <exception xmlns=""
                     class="androidx.test.espresso.PerformException"
                     message="Error performing 'single click - At Coordinates: 179, 1581 and precision: 16, 16' on view 'PinDigitView{id=2131362180, res-name=mp__pin_digit_one}'.">at androidx.test.espresso.PerformException.Builder.build(PerformException.java:1)
at androidx.test.espresso.action.GeneralClickAction.perform(GeneralClickAction.java:35)
at androidx.test.espresso.ViewInteraction.SingleExecutionViewAction.perform(ViewInteraction.java:2)
at androidx.test.espresso.ViewInteraction.doPerform(ViewInteraction.java:22)
at androidx.test.espresso.ViewInteraction.access.100(ViewInteraction.java:1)

<cause class="java.lang.IllegalArgumentException"
                    message="Required value was null.">at android.view.animation.Animation.dispatchAnimationEnd(Animation.java:1118)
at android.view.animation.AnimationSet.getTransformation(AnimationSet.java:417)</cause>
</exception>
      </CrashProbe>
           </View>
        """.trimIndent()
        val oracle = CrashOracle()
        val result = oracle.eval(buildState(xml))
        val decision = result.query { it.attribute(CrashOracle.decision)?.equals(Verdict.FAIL.name) ?: false }
        assertTrue(decision.size == 1)
    }

    @Test
    fun testOKVerdict() {
        val xml = """<View>
             <CrashProbe xmlns="https://org.mint/espresso/probe"/>
           </View>
        """.trimIndent()
        val oracle = CrashOracle()
        val result = oracle.eval(buildState(xml))
        val decision = result.query { it.attribute(CrashOracle.decision)?.equals(Verdict.OK.name) ?: false }
        assertTrue(decision.size == 1)
    }

    @Test
    fun testProbeNotExecuted() {
        val xml = """<View></View>""".trimIndent()
        val oracle = CrashOracle()
        val result = oracle.eval(buildState(xml))
        val decision = result.query { it.attribute(CrashOracle.decision)?.equals(Verdict.DONT_KNOW.name) ?: false }
        assertTrue(decision.size == 1)
    }
}
