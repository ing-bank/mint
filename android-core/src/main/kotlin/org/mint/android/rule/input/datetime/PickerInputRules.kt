package org.mint.android.rule.input.datetime

import org.mint.android.Action
import org.mint.android.rule.BasicRules
import java.math.BigDecimal

object PickerInputRules {

    fun datePickerInputRule(): PickerInputRule {
        return PickerInputRule(
            description = "Generate date input for DatePickers",
            action = Action.DATE_PICKER_INPUT,
            pred = BasicRules.xpred(
                ".[@isDisplayed = 'true' " +
                    "and @isDatePicker = 'true' " +
                    "]",
            ),
            prio = BasicRules.fprio(BigDecimal(1)),
            gen = { DateInputSupplier.get(it.rnd) },
        )
    }

    fun timePickerInputRule(): PickerInputRule {
        return PickerInputRule(
            description = "Generate time input for TimePickers",
            action = Action.TIME_PICKER_INPUT,
            pred = BasicRules.xpred(
                ".[@isDisplayed = 'true' " +
                    "and @isTimePicker = 'true' " +
                    "]",
            ),
            prio = BasicRules.fprio(BigDecimal(1)),
            gen = { TimeInputSupplier.get(it.rnd) },
        )
    }
}
