package org.mint.android.rule

import org.mint.android.Action
import org.mint.android.AndroidState
import java.math.BigDecimal

data class PositionBasedRule(
    override val description: String,
    override val action: Action,
    val pred: (AndroidState) -> Boolean,
    val itemPosition: (AndroidState) -> String,
    val prio: (AndroidState) -> BigDecimal
) : BasePositionBasedClickRule() {
    override fun priority(): (AndroidState) -> BigDecimal = prio
    override fun predicate(): (AndroidState) -> Boolean = pred
    override fun itemPosition(): (AndroidState) -> String = itemPosition
}
