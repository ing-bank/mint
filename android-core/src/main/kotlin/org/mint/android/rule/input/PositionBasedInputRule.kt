package org.mint.android.rule.input

import org.mint.android.Action
import org.mint.android.AndroidConstants
import org.mint.android.AndroidState
import org.w3c.dom.Element
import java.math.BigDecimal

data class PositionBasedInputRule(
    override val description: String,
    val pred: (AndroidState) -> Boolean,
    val prio: (AndroidState) -> BigDecimal,
    val gen: (AndroidState) -> String,
    val itemPosition: (AndroidState) -> String,
) : BaseInputRule() {
    override val action: Action = Action.INPUT
    override fun generate(): (AndroidState) -> String = gen
    override fun priority(): (AndroidState) -> BigDecimal = prio
    override fun predicate(): (AndroidState) -> Boolean = pred

    private fun itemPosition(): (AndroidState) -> String = itemPosition

    override fun attributes(state: AndroidState, action: Element) {
        super.attributes(state, action)
        action.setAttribute(AndroidConstants.POSITION, itemPosition()(state))
    }
}
