package org.mint.android.rule.input

import org.mint.android.Action
import org.mint.android.AndroidState
import java.math.BigDecimal

data class GenericInputRule(
    override val description: String,
    val pred: (AndroidState) -> Boolean,
    val prio: (AndroidState) -> BigDecimal,
    val gen: (AndroidState) -> String
) :
    BaseInputRule() {
    override val action: Action = Action.INPUT
    override fun generate(): (AndroidState) -> String = gen
    override fun priority(): (AndroidState) -> BigDecimal = prio
    override fun predicate(): (AndroidState) -> Boolean = pred

    companion object GenericInputRule {
        val rules = listOf(
            InputRules.defaultUTF8InputRule(),
            InputRules.defaultTextInputRule(),
            InputRules.defaultMultilineTextInputRule(),
            InputRules.defaultEmailAddressInputRule(),
            InputRules.defaultNumberInputRule(),
            InputRules.defaultDecimalNumberInputRule(),
            InputRules.defaultSignedNumberInputRule(),
            InputRules.defaultPersonNameInputRule(),
            InputRules.defaultUriRule(),
            InputRules.defaultTimeInputRule(),
            InputRules.defaultDateInputRule(),
            InputRules.defaultPostalAddressInputRule(),
            InputRules.defaultPhoneNumberInputRule(),
            InputRules.defaultGenericTextInputRule()
        )
        val deprioritizationRules = listOf(
            InputRules.defaultTextClickAtPositionDeprioritizeRule(),
            InputRules.defaultUneditableTextClickDeprioritizeRule(),
            InputRules.defaultTextClickDeprioritizeRule()
        )
    }
}
