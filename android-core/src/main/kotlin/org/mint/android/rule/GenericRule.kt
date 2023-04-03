package org.mint.android.rule

import org.mint.android.Action
import org.mint.android.AndroidState
import java.math.BigDecimal

data class GenericRule(
    override val description: String,
    override val action: Action,
    val pred: (AndroidState) -> Boolean,
    val prio: (AndroidState) -> BigDecimal
) : BaseRule() {
    override fun priority(): (AndroidState) -> BigDecimal = prio
    override fun predicate(): (AndroidState) -> Boolean = pred
}
