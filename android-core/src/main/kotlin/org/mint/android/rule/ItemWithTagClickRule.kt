package org.mint.android.rule

import org.mint.android.Action
import org.mint.android.AndroidState
import java.math.BigDecimal

data class ItemWithTagClickRule(
    override val description: String,
    override val action: Action,
    val pred: (AndroidState) -> Boolean,
    val itemTag: (AndroidState) -> String = { "" },
    val prio: (AndroidState) -> BigDecimal,
) : BaseItemWithTagClickRule() {
    override fun priority(): (AndroidState) -> BigDecimal = prio
    override fun predicate(): (AndroidState) -> Boolean = pred
    override fun itemTag(): (AndroidState) -> String = itemTag
}
