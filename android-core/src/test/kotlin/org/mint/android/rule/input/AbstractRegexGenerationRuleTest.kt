package org.mint.android.rule.input

import org.junit.Assert.assertEquals
import org.junit.Test
import org.mint.android.AndroidConstants
import org.mint.android.StateTest

class AbstractRegexGenerationRuleTest : StateTest() {
    override val widgetTreeXML: String = """
        <View class="androidx.appcompat.widget.AppCompatEditText" id="2131230898" resourceName="editTextNumber2" package="org.mint.exampleapp"/>
    """.trimIndent()

    @Test
    fun generateTextAndMatchIt() {
        val regex = "[a-f]{8}"
        val rule = InputRules.xqueryRegexInputRule(
            description = "n/a",
            pred = ".[contains(@class, 'androidx.appcompat.widget.AppCompatEditText')]",
            prio = "1.0",
            reg = regex
        )

        state.extendWithRuleGroups()
        state.apply(rule)
        var matches = state.query { it.nodeName == rule.ruleName() }
        assertEquals(matches.size, 1)

        matches = state.query {
            Regex(regex).matches(it.attributes?.getNamedItem(AndroidConstants.TEXT)?.nodeValue.toString())
        }
        assertEquals(matches.size, 1)
    }
}
