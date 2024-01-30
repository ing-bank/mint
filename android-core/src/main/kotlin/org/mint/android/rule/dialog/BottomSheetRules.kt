package com.ing.mint.android.rule.dialog

import com.ing.mint.android.Action
import com.ing.mint.android.AndroidState
import com.ing.mint.android.rule.BasicRules
import com.ing.mint.android.rule.BasicRules.xpred
import com.ing.mint.android.rule.PositionBasedRule
import org.w3c.dom.Element

object BottomSheetRules {
    private val positionInViewHierarchy: (AndroidState) -> String = { state ->
        (state.node as Element).getAttribute("positionInViewHierarchy")
    }

    fun clickableRuleBasedOnPositionInViewHierarchy(): PositionBasedRule =
        PositionBasedRule(
            description = "Click on any displayed, clickable widget that has a bottom sheet as an ancestor.",
            action = Action.CLICK_ON_ITEM_AT_POSITION,
            pred = xpred(
                ".[@isDisplayed='true' " +
                    "and @isClickable='true'] " +
                    "and ancestor::*[@resourceName='design_bottom_sheet']",
            ),
            itemPosition = positionInViewHierarchy,
            prio = BasicRules.defaultPrio,
        )
}
