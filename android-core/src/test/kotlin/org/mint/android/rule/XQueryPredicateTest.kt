package org.mint.android.rule

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mint.android.StateTest

class XQueryPredicateTest : StateTest() {
    override val widgetTreeXML: String = """
        <View>
          <HelloWorldProbe xmlns:prb="https://org.mint/espresso/probe">
            <hello-world full-name="John Doe"/>
          </HelloWorldProbe>
          <HelloWorldProbe xmlns="https://org.mint/espresso/probe">
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
                     <org.mint.android.rule.GenericRule description="Click on any widget that has 'isClickable' as true">
                        <click xmlns="http://org.mint/espresso/action"
                                applied="true"
                                derived-priority="1"
                                priority="1"
                                resourceName=""
                                selected="true"/>
                     </org.mint.android.rule.GenericRule>
            """.trimIndent()
        )
        assertTrue(XQueryPredicate("count(//action:click[@selected = 'true']) = 1").invoke(s))
    }
}
