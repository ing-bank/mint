package org.mint.android.rule

import org.junit.Assert
import org.junit.Test
import org.mint.android.StateTest

class PositionBasedRuleTest : StateTest() {
    override val widgetTreeXML: String = """
        <x>
          <y>
            <View>
                <View isClickable="true" isDisplayed="true"/>
            </View>
          </y>
          <y>
            <View isClickable="true"/>
          </y>
        </x>
    """.trimIndent()

    @Test
    fun findIsClickableVisibleItem() {
        val rule = BasicRules.clickableRuleBasedOnPositionInViewHierarchy()

        state.extendWithRuleGroups()
        state.apply(rule)

        val matches = state.query { it.nodeName == rule.ruleName() }
        Assert.assertEquals(1, matches.size)
    }

    @Test
    fun positionRuleIsNotAppliedWhenTagsExist() {
        val s = buildState(
            """
        <x>
          <y>
            <View>
                <View isClickable="true" isDisplayed="true" tag="value"/>
            </View>
          </y>
          <y>
            <View isClickable="true"/>
          </y>
        </x>
            """.trimIndent()
        )
        val rule = BasicRules.clickableRuleBasedOnPositionInViewHierarchy()

        s.extendWithRuleGroups()
        s.apply(rule)

        val matches = s.query { it.nodeName == rule.ruleName() }
        Assert.assertEquals(0, matches.size)
    }

    @Test
    fun isItemThatCanBeScrolledToAndClicked() {
        val s = buildState(
            """
        <View isScrollable='true'>
          <y>
            <View isViewPager='true'>
                <View isClickable="true" isDisplayed="false" isShown='true' isVisible='true'/>
            </View>
          </y>
          <y>
            <View>
                <View isClickable="false" isDisplayed="false" isShown='true' isVisible='true'/>
            </View>
            <View>
                <View isClickable="true" isDisplayed="true" isShown='true' isVisible='true'/>
            </View>
            <View>
                <View isClickable="true" isDisplayed="false" isShown='false' isVisible='true'/>
            </View>
            <View>
                <View isClickable="true" isDisplayed="false" isShown='true' isVisible='true'/>
            </View>
            <View isClickable="true"/>
          </y>
        </View>
            """.trimIndent()
        )
        val rule = BasicRules.clickableRuleBasedOnPositionInViewHierarchy()

        s.extendWithRuleGroups()
        s.apply(rule)

        val matches = s.query { it.nodeName == rule.ruleName() }
        Assert.assertEquals(1, matches.size)
    }
}
