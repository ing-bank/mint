package org.mint.android.rule

import org.junit.Assert
import org.junit.Test
import org.mint.android.StateTest

class IsClickableRuleTest : StateTest() {
    override val widgetTreeXML: String = """
        <x>
          <y>
            <View isClickable="true" isDisplayed="true"/>
            <View two="2" isClickable="false" isDisplayed="true"/>
          </y>
          <y>
            <View isClickable="true" isDisplayed="true"/>
          </y>
        </x>
    """.trimIndent()

    @Test
    fun findIsClickableElements() {
        val rule = BasicRules.simpleClickableRule()

        state.extendWithRuleGroups()
        state.apply(rule)

        val matches = state.query { it.nodeName == rule.ruleName() }
        Assert.assertEquals(matches.size, 2)
    }
}
