package org.mint.android.rule.input.datetime

import org.mint.android.Action
import org.mint.android.AndroidState
import org.mint.android.rule.input.BaseInputRule
import java.math.BigDecimal

data class PickerInputRule(
    override val description: String,
    override val action: Action,
    val pred: (AndroidState) -> Boolean,
    val prio: (AndroidState) -> BigDecimal,
    val gen: (AndroidState) -> String,
) :
    BaseInputRule() {
    override fun generate(): (AndroidState) -> String = gen
    override fun priority(): (AndroidState) -> BigDecimal = prio
    override fun predicate(): (AndroidState) -> Boolean = pred
}
