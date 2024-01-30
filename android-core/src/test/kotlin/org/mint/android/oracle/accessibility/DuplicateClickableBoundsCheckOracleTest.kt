package com.ing.mint.android.oracle.accessibility

import com.ing.mint.android.StateBuilder
import com.ing.mint.android.xml.attribute
import com.ing.mint.lib.Verdict.FAIL
import org.junit.Assert
import org.junit.Test

class DuplicateClickableBoundsCheckOracleTest : StateBuilder {

    @Test
    fun testVerdictWithFinding() {
        val oracle = DuplicateClickableBoundsCheckOracle()
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
        val oracle = DuplicateClickableBoundsCheckOracle()
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
                        <view-check result="ERROR" type="DuplicateClickableBoundsCheck"/>
                    </accessibility-checks>
                </View>
            </View>
        """.trimIndent()

        private val xmlWithoutFinding = """
            <View childCount="1" class="app.ui.settings.CreateNewCardActionView">
                <View class="com.google.android.material.button.MaterialButton" height="121" positionInViewHierarchy="root.0.1.0.1.0.0.0.0.0.4.2">
                    <accessibility-checks>
                        <view-check message="This item's height is 44dp. Consider making the height of this touch target 48dp or larger." result="ERROR" type="TouchTargetSizeCheck">
                            <metadata key-height="44" key-required-height="48" key-required-width="48" key-width="361"/>
                        </view-check>
                    </accessibility-checks>
                </View>
            </View>
        """.trimIndent()
    }
}
