package org.mint.android.rule.dialog

import org.mint.android.Action
import org.mint.android.AndroidState
import org.mint.android.rule.BasicRules
import org.mint.android.rule.BasicRules.xpred
import org.mint.android.rule.PositionBasedRule
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
                    "and ancestor::*[@resourceName='design_bottom_sheet']"
            ),
            itemPosition = positionInViewHierarchy,
            prio = BasicRules.defaultPrio
        )
}
