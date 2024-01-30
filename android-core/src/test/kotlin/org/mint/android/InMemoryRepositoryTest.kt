package com.ing.mint.android

import com.ing.mint.android.rule.BasicRules
import com.ing.mint.android.xml.attribute
import com.ing.mint.android.xml.hasNS
import com.ing.mint.android.xml.parent
import org.junit.Assert
import org.junit.Test

class InMemoryRepositoryTest : StateBuilder {
    private val xml: String = """
            <View class="com.android.internal.policy.DecorView">
              <View class="android.view.ViewStub" id="16908719" resourceName="action_mode_bar_stub" package="android"/>
              <View class="androidx.appcompat.widget.ContentFrameLayout" id="16908290" resourceName="content" package="android">
                <View class="android.widget.LinearLayout">
                  <View class="com.google.android.material.button.MaterialButton" id="2131230819" resourceName="button2" package="com.ing.mint.exampleapp" isClickable="true" isDisplayed="true"/>
                  <View class="com.google.android.material.textview.MaterialTextView" id="2131231165" resourceName="textView2" package="com.ing.mint.exampleapp"/>
                  <View class="androidx.appcompat.widget.AppCompatEditText" id="2131230898" resourceName="editTextNumber2" package="com.ing.mint.exampleapp"/>
                </View>
              </View>
            </View>
    """.trimIndent()

    @Test
    fun correlateTest() {
        val state = buildState(xml)
        val stateAbstraction = ExampleStateAbstraction.mapper()
        val actionAbstraction = ExampleActionAbstraction.mapper()
        val repo = InMemoryRepository()

        val abstract = state.copy()

        // abstract over
        abstract.extend(stateAbstraction)
        abstract.extend(actionAbstraction)
        abstract.createParentHashes()

        val ruleState = abstract.copy()
        // apply rule (1 match/extension) - (click button2)
        ruleState.extendWithRuleGroups()
        ruleState.apply(BasicRules.simpleClickableRule())

        // action selection (click button2)
        val selected = ruleState.selectAction()

        // persist
        selected.map { repo.persist(it) }

        // correlate abstract state
        val correlated = abstract.copy()
        repo.correlate(correlated)

        // we should end up with 1 (historical) click action, correlated with button2
        val matches = correlated.query {
            it.hasNS(AndroidConstants.ACTION_NS) &&
                "button2" == it.attribute("resourceName") && // button2 should be correlated
                it.parent().parent().hasNS(AndroidConstants.CORRELATE_NS) // the correlation
        }
        Assert.assertEquals(matches.size, 1)
    }
}
