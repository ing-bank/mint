package org.mint.android.rule.viewgroup

import org.junit.Assert
import org.junit.Test
import org.mint.android.StateTest

class ViewPagerScrollingRuleTest : StateTest() {
    override val widgetTreeXML: String = """
        <x>
          <y>
            <View>
                <View isViewPager='true' canScrollRight='true' canScrollLeft='true'/>
            </View>
          </y>
          <y></y>
        </x>
    """.trimIndent()

    @Test
    fun findIsViewPagerThatCanBeScrolledRight() {
        val rule = ViewGroupRules.scrollingPagerRightRule()

        state.extendWithRuleGroups()
        state.apply(rule)

        val matches = state.query { it.nodeName == rule.ruleName() }
        Assert.assertEquals(1, matches.size)
    }

    @Test
    fun findIsViewPagerThatCanBeScrolledLeft() {
        val rule = ViewGroupRules.scrollingPagerLeftRule()

        state.extendWithRuleGroups()
        state.apply(rule)

        val matches = state.query { it.nodeName == rule.ruleName() }
        Assert.assertEquals(1, matches.size)
    }
}
