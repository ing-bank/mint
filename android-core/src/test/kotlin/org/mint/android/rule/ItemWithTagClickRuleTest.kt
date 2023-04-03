package org.mint.android.rule

import org.junit.Assert
import org.junit.Test
import org.mint.android.StateTest

class ItemWithTagClickRuleTest : StateTest() {
    override val widgetTreeXML: String = """
        <x>
          <y>
            <View >
                <View isDisplayed="true"> 
                    <View >
                        <View isClickable="true" isDisplayed="true" tag="tagValue"/>
                    </View>
                </View>
            </View>
            <View isClickable="true"/>
          </y>
          <y>
            <View isClickable="true"/>
          </y>
        </x>
    """.trimIndent()

    @Test
    fun findClickableItemWithTag() {
        val rule = BasicRules.clickableRuleForItemWithTag()

        state.extendWithRuleGroups()
        state.apply(rule)

        val matches = state.query { it.nodeName == rule.ruleName() }
        Assert.assertEquals(1, matches.size)
    }
}
