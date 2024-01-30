package com.ing.mint.android.rule

import com.ing.mint.android.StateTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class XQueryPredicateTest : StateTest() {
    override val widgetTreeXML: String = """
        <View>
          <HelloWorldProbe xmlns:prb="https://com.ing.mint/espresso/probe">
            <hello-world full-name="John Doe"/>
          </HelloWorldProbe>
          <HelloWorldProbe xmlns="https://com.ing.mint/espresso/probe">
            <hello-world full-name="Jane Doe"/>
          </HelloWorldProbe>
          <HelloWorldProbe>
            <hello-world full-name="John Lennon"/>
          </HelloWorldProbe>
        </View>
    """.trimIndent()

    @Test
    fun foundBecauseOfExplicitNamespace() {
        assertTrue(XQueryPredicate("//hello-world[@full-name = 'John Doe']").invoke(state))
    }

    @Test
    fun foundBecauseOfNoNamespace() {
        assertTrue(XQueryPredicate("//hello-world[@full-name = 'John Lennon']").invoke(state))
    }

    @Test
    fun notFoundBecauseOfImplicitDefaultNamespace() {
        assertFalse(XQueryPredicate("//hello-world[@full-name = 'Jane Doe']").invoke(state))
    }

    @Test
    fun testWithAttributeInANamespace() {
        val s = buildState(
            """
                     <com.ing.mint.android.rule.GenericRule description="Click on any widget that has 'isClickable' as true">
                        <click xmlns="http://com.ing.mint/espresso/action"
                                applied="true"
                                derived-priority="1"
                                priority="1"
                                resourceName=""
                                selected="true"/>
                     </com.ing.mint.android.rule.GenericRule>
            """.trimIndent(),
        )
        assertTrue(XQueryPredicate("count(//action:click[@selected = 'true']) = 1").invoke(s))
    }
}
