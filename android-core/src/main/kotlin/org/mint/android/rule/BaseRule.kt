package com.ing.mint.android.rule

import com.ing.mint.android.Action
import com.ing.mint.android.AndroidConstants
import com.ing.mint.android.AndroidState
import com.ing.mint.lib.Rule
import org.w3c.dom.Element
import java.math.BigDecimal

abstract class BaseRule : AndroidConstants, RuleTools, Rule<AndroidState> {
    /** Which action to take (from the action namespace) */
    abstract val action: Action

    override fun priority(): (AndroidState) -> BigDecimal = { BigDecimal(1) }

    abstract fun predicate(): (AndroidState) -> Boolean

    override fun apply(): (state: AndroidState) -> AndroidState? = { state ->
        val doc = state.node.ownerDocument

        if (predicate()(state) && state.node is Element) {
            val rule = doc.createElementNS(AndroidConstants.RULE_NS, ruleName())
            rule.setAttribute("description", description)

            val _action = doc.createElementNS(AndroidConstants.ACTION_NS, action.tagName)
            _action.setAttribute(AndroidConstants.PRIORITY, priority()(state).toString())
            _action.setAttribute(
                AndroidConstants.RESOURCE_NAME,
                state.node.getAttribute(AndroidConstants.RESOURCE_NAME),
            )

            attributes(state, _action)

            rule.appendChild(_action)

            state.derive(rule)
        } else {
            null
        }
    }
}
