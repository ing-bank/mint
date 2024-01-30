package com.ing.mint.android.rule

import com.ing.mint.android.Action
import com.ing.mint.android.AndroidConstants
import com.ing.mint.android.AndroidState
import org.w3c.dom.Element
import java.math.BigDecimal

/*
 * A MultiplicativeRule indicates that its sibling action priorities (that are in the same
 * enclosing rule-group) must be modified by multiplying those priorities by this' priority
*/

data class MultiplicativeRule(
    override val description: String,
    override val action: Action,
    val pred: (AndroidState) -> Boolean,
    val prio: (AndroidState) -> BigDecimal,
) : BaseRule() {
    override fun attributes(state: AndroidState, action: Element) {
        action.setAttribute(AndroidConstants.MODIFIER, AndroidConstants.MULTIPLICATIVE)
    }
    override fun priority(): (AndroidState) -> BigDecimal = prio
    override fun predicate(): (AndroidState) -> Boolean = pred
}
