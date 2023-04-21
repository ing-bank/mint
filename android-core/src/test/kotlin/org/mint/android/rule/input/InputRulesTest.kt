package org.mint.android.rule.input

import junit.framework.TestCase.assertFalse
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.mint.android.StateTest
import org.mint.android.xml.attribute

@RunWith(JUnitParamsRunner::class)
class InputRulesTest : StateTest() {
    override val widgetTreeXML: String = """
        <View class="com.android.internal.policy.DecorView">
              <View class="android.view.ViewStub" id="16908719" resourceName="action_mode_bar_stub" package="android"/>
              <View class="androidx.appcompat.widget.ContentFrameLayout" id="16908290" resourceName="content" package="android">
                <View class="android.widget.LinearLayout">
                  <View class="com.google.android.material.button.MaterialButton" id="2131230819" resourceName="button2" package="org.mint.exampleapp"/>
                  <View class="com.google.android.material.textview.MaterialTextView" id="2131231165" resourceName="textView2" package="org.mint.exampleapp"/>
                  <View class="androidx.appcompat.widget.AppCompatEditText" isEditText="true" isDisplayed="true" hasOnClickListeners="false" inputType="33" id="2131230898" resourceName="editTextNumber2" package="org.mint.exampleapp"/>
                  <View class="androidx.appcompat.widget.AppCompatEditText" isEditText="true" isDisplayed="true" hasOnClickListeners="false" inputType="4098" id="2131230898" resourceName="editTextNumber2" package="org.mint.exampleapp"/>
                  <View class="androidx.appcompat.widget.AppCompatEditText" isEditText="true" isDisplayed="true" hasOnClickListeners="false" inputType="1" id="2131230898" resourceName="editTextNumber2" package="org.mint.exampleapp"/>
                  <View class="androidx.appcompat.widget.AppCompatEditText" isEditText="true" isDisplayed="true" hasOnClickListeners="false" inputType="2" id="2131230898" resourceName="editTextNumber2" package="org.mint.exampleapp"/>
                  <View class="androidx.appcompat.widget.AppCompatEditText" isEditText="true" isDisplayed="true" hasOnClickListeners="false" inputType="3" id="2131230898" resourceName="editTextNumber2" package="org.mint.exampleapp"/>
                  <View class="androidx.appcompat.widget.AppCompatEditText" isEditText="true" isDisplayed="true" hasOnClickListeners="false" inputType="8194" id="2131230898" resourceName="editTextNumber2" package="org.mint.exampleapp"/>
                  <View class="androidx.appcompat.widget.AppCompatEditText" isEditText="true" isDisplayed="true" hasOnClickListeners="false" inputType="97" id="2131230898" resourceName="editTextNumber2" package="org.mint.exampleapp"/>
                  <View class="androidx.appcompat.widget.AppCompatEditText" isEditText="true" isDisplayed="true" hasOnClickListeners="false" inputType="17" id="2131230898" resourceName="editTextNumber2" package="org.mint.exampleapp"/>
                  <View class="androidx.appcompat.widget.AppCompatEditText" isEditText="true" isDisplayed="true" hasOnClickListeners="false" inputType="36" id="2131230898" resourceName="editTextNumber2" package="org.mint.exampleapp"/>
                  <View class="androidx.appcompat.widget.AppCompatEditText" isEditText="true" isDisplayed="true" hasOnClickListeners="false" inputType="20" id="2131230898" resourceName="editTextNumber2" package="org.mint.exampleapp"/>
                  <View class="androidx.appcompat.widget.AppCompatEditText" isEditText="true" isDisplayed="true" hasOnClickListeners="false" inputType="113" id="2131230898" resourceName="editTextNumber2" package="org.mint.exampleapp"/>
                  <View class="androidx.appcompat.widget.AppCompatEditText" isEditText="true" isDisplayed="true" hasOnClickListeners="false" inputType="131073" id="2131230898" resourceName="editTextNumber2" package="org.mint.exampleapp"/>
                </View>
              </View>
             </View>
    """.trimIndent()

    @Test
    @Parameters(method = "rules")
    fun inputRules(rule: BaseInputRule) {
        state.extendWithRuleGroups()
        state.apply(rule)

        val matches = state.query { it.nodeName == rule.ruleName() }
        val input = matches.first().firstChild.attribute("text")

        assertEquals(1, matches.size)
        assertFalse(input.isNullOrBlank())
    }

    @Test
    @Parameters(method = "genericRules")
    fun genericInputRule(rule: BaseInputRule) {
        val s = buildState(
            """
        <View class="com.android.internal.policy.DecorView">
              <View class="android.view.ViewStub" id="16908719" resourceName="action_mode_bar_stub" package="android"/>
              <View class="androidx.appcompat.widget.ContentFrameLayout" id="16908290" resourceName="content" package="android">
                <View class="android.widget.LinearLayout">
                  <View class="com.google.android.material.textview.MaterialTextView" id="2131231165" resourceName="textView2" package="org.mint.exampleapp"/>
                  <View class="androidx.appcompat.widget.AppCompatEditText" isEditText="true" isDisplayed="true" hasOnClickListeners="false" id="2131230898" resourceName="editTextNumber2" package="org.mint.exampleapp"/>
                </View>
              </View>
        </View>
            """.trimIndent(),
        )

        s.extendWithRuleGroups()
        s.apply(rule)

        val matches = s.query { it.nodeName == rule.ruleName() }
        val input = matches.first().firstChild.attribute("text")

        assertEquals(1, matches.size)
        assertFalse(input.isNullOrBlank())
    }

    private fun rules(): List<BaseInputRule> {
        // exclude the generic ones from the parameterized test since they're catch-all and will match all EditText views, regardless of inputTypes
        return GenericInputRule.rules.filter { r ->
            !genericRules().any { gr -> gr.description == r.description }
        }
    }

    private fun genericRules(): List<BaseInputRule> {
        return listOf(InputRules.defaultGenericTextInputRule(), InputRules.defaultUTF8InputRule())
    }
}
