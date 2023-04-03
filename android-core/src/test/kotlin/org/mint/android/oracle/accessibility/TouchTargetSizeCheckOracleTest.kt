package org.mint.android.oracle.accessibility

import org.junit.Assert
import org.junit.Test
import org.mint.android.StateBuilder
import org.mint.android.xml.attribute
import org.mint.lib.Verdict.FAIL

class TouchTargetSizeCheckOracleTest : StateBuilder {

    @Test
    fun testVerdictWithFinding() {
        val oracle = TouchTargetSizeCheckOracle()
        val result = oracle.eval(buildState(xmlWithFinding))
        val decision = result.query { node ->
            node.nodeName.equals(ELEMENT_VERDICT) &&
                node.attribute(ATTR_DECISION)
                    ?.equals(FAIL.name) ?: false
        }
        Assert.assertTrue(decision.size == 1)
    }

    @Test
    fun testVerdictWithoutFinding() {
        val oracle = TouchTargetSizeCheckOracle()
        val result = oracle.eval(buildState(xmlWithoutFinding))
        val decision = result.query { node ->
            node.nodeName.equals(ELEMENT_VERDICT) &&
                node.attribute(ATTR_DECISION)
                    ?.equals(FAIL.name) ?: false
        }
        Assert.assertTrue(decision.isEmpty())
    }

    companion object {
        const val ELEMENT_VERDICT = "verdict"
        const val ATTR_DECISION = "decision"
        private val xmlWithFinding = """
            <View childCount="1" class="app.ui.settings.CreateNewCardActionView">
                <View class="com.google.android.material.button.MaterialButton" height="121" positionInViewHierarchy="root.0.1.0.1.0.0.0.0.0.4.2">
                    <accessibility-checks>
                        <view-check result="ERROR" type="TouchTargetSizeCheck"/>
                    </accessibility-checks>
                </View>
            </View>
        """.trimIndent()

        private val xmlWithoutFinding = """
            <View childCount="1" class="app.ui.settings.CreateNewCardActionView">
                <View class="com.google.android.material.button.MaterialButton" height="121" positionInViewHierarchy="root.0.1.0.1.0.0.0.0.0.4.2">
                    <accessibility-checks>
                        <view-check result="ERROR" type="TextContrastCheck"/>
                    </accessibility-checks>
                </View>
            </View>
        """.trimIndent()
    }
}
