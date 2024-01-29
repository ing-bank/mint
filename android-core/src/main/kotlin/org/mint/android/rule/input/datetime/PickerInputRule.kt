package com.ing.mint.android.rule.input.datetime

import com.ing.mint.android.Action
import com.ing.mint.android.AndroidState
import com.ing.mint.android.rule.input.BaseInputRule
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
