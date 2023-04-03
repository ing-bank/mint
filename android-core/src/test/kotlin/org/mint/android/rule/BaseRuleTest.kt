package org.mint.android.rule

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mint.android.Action
import org.mint.android.AndroidConstants
import org.mint.android.AndroidState
import org.mint.android.StateTest
import org.w3c.dom.Element

class BaseRuleTest : StateTest() {
    override val widgetTreeXML: String = """
            <View class="com.android.internal.policy.DecorView">
              <View class="android.view.ViewStub" id="16908719" resourceName="action_mode_bar_stub" package="android"/>
              <View class="androidx.appcompat.widget.ContentFrameLayout" id="16908290" resourceName="content" package="android">
                <View class="android.widget.LinearLayout">
                  <View class="com.google.android.material.button.MaterialButton" id="2131230819" resourceName="button2" package="org.mint.exampleapp"/>
                  <View class="com.google.android.material.textview.MaterialTextView" id="2131231165" resourceName="textView2" package="org.mint.exampleapp"/>
                  <View class="androidx.appcompat.widget.AppCompatEditText" id="2131230898" resourceName="editTextNumber2" package="org.mint.exampleapp"/>
                </View>
              </View>
            </View>
    """.trimIndent()

    @Test
    fun noMatchesFound() {
        val rule = BasicRules.xqueryRule(
            description = "n/a",
            action = Action.CLICK,
            pred = ".[@class = 'does.not.exist']",
            prio = "1.0"
        )

        state.extendWithRuleGroups()
        state.apply(rule)

        val matches = state.query { it.nodeName == rule.ruleName() }
        assertTrue(matches.isEmpty())
    }

    @Test
    fun matchSomeSpecificClass() {
        val rule = BasicRules.xqueryRule(
            description = "n/a",
            action = Action.CLICK,
            pred = ".[@class = 'does.not.exist' or @class = 'androidx.appcompat.widget.AppCompatEditText']",
            prio = "1.0"
        )

        state.extendWithRuleGroups()
        state.apply(rule)
        val matches = state.query { it.nodeName == rule.ruleName() }
        assertEquals(matches.size, 1)
    }

    @Test
    fun matchAddedAttribute() {
        val rule = object : BaseRule() {
            val activeWidgetFQNs: Set<String> = setOf(
                "androidx.appcompat.widget.AppCompatEditText"
            )

            override fun attributes(state: AndroidState, action: Element) {
                action.setAttribute("XYZ", "abc")
            }
            override val action: Action = Action.CLICK
            override fun predicate(): (AndroidState) -> Boolean = { x ->
                val n = x.node
                if (n is Element) {
                    activeWidgetFQNs.contains(n.getAttribute(AndroidConstants.CLASS))
                } else {
                    false
                }
            }

            override val description: String = "n/a"
        }

        state.extendWithRuleGroups()
        state.apply(rule)

        val matches = state.query {
            it.attributes?.getNamedItem("XYZ")?.nodeValue == "abc"
        }
        assertEquals(matches.size, 1)
    }
}
