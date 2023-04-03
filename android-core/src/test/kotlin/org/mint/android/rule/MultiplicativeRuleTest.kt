package org.mint.android.rule

import org.junit.Assert
import org.junit.Test
import org.mint.android.Action
import org.mint.android.StateTest
import org.mint.android.rule.viewgroup.ViewGroupRules
import org.mint.android.xml.attribute
import org.mint.android.xml.query
import java.math.BigDecimal

class MultiplicativeRuleTest : StateTest() {
    override val widgetTreeXML: String = """
        <x>
          <y>
            <View isClickable="true"/>
            <View two="2" isClickable="false"/>
          </y>
          <y>
            <View isClickable="true"/>
          </y>
        </x>
    """.trimIndent()

    @Test
    fun findIsClickableElements() {
        val rule =
            GenericRule(
                description = "Click on any widget that has 'isClickable' as true",
                action = Action.CLICK,
                pred = BasicRules.xpred(".[@isClickable = 'true']"),
                prio = BasicRules.fprio(BigDecimal(2))
            )

        val prio1 = MultiplicativeRule(
            description = "prioritize the clicking of elements",
            action = Action.CLICK,
            pred = BasicRules.xpred("."),
            prio = BasicRules.fprio(BigDecimal(3))
        )

        val prio2 = MultiplicativeRule(
            description = "prioritize the clicking of elements",
            action = Action.CLICK,
            pred = BasicRules.xpred("."),
            prio = BasicRules.fprio(BigDecimal(5))
        )

        state.extendWithRuleGroups()
        state.apply(rule)
        state.apply(prio1)
        state.apply(prio2)
        state.selectAction()

        val matches = state.query {
            it.nodeName == "click" &&
                it.attribute("derived-priority") == "30" // 2 * 3 * 5
        }
        Assert.assertEquals(matches.size, 2)
    }

    @Test
    fun replicatedItemSimpleClickDeprioritizationRule() {
        val s = buildState(
            """
        <x>
          <y>
            <View isRecyclerView='true'>
                <View isClickable="true" isDisplayed="true" tag="value"/>
            </View>
          </y>
          <y>
            <View isViewPager='true'>
                <View isClickable="true" isDisplayed="true" tag="value"/>
            </View>
          </y>
          <y>
            <View isClickable="true"/>
          </y>
        </x>
            """.trimIndent()
        )
        val rule = ViewGroupRules.replicatedItemSimpleClickDeprioritizeRule()

        s.extendWithRuleGroups()
        s.apply(BasicRules.simpleClickableRule())
        s.apply(rule)
        s.selectAction()

        val matches = s.query {
            it.nodeName == "click" &&
                it.attribute("derived-priority")?.startsWith("0.01") ?: false
        }
        Assert.assertEquals(2, matches.size)
    }

    @Test
    fun historicalActionDeprioritizationRules() {
        val s = buildState(
            """
                <View
                        class="com.google.android.material.textfield.TextInputEditText"
                        isClickable="true"
                        isDisplayed="true">
                </View>
            """.trimIndent()
        )

        val actionRule = setOf(BasicRules.simpleClickableRule())
        val rules = actionRule + BasicRules.defaultHistoricalActionsDeprioritizeRules(actionRule)

        s.extendWithRuleGroups()
        rules.forEach { s.apply(it) }

        val deprioritizationRules = s.query {
            it.nodeName == "org.mint.android.rule.MultiplicativeRule"
        }
        val clickDeprioritizationRules = deprioritizationRules[0].query {
            it.nodeName == "click"
        }
        Assert.assertEquals(1, deprioritizationRules.size)
        Assert.assertEquals(1, clickDeprioritizationRules.size)
    }
}
