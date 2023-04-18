package org.mint.android.rule.viewgroup

import org.mint.android.Action
import org.mint.android.AndroidState
import org.mint.android.rule.BasicRules.xpred
import org.mint.android.rule.MultiplicativeRule
import org.mint.android.rule.PositionBasedRule
import org.w3c.dom.Element
import java.math.BigDecimal
import java.util.Random

object AdapterViewRules {

    private val random = Random()
    private val randomPositionInList = { s: AndroidState ->
        val currentlySelectedItem =
            (s.node as Element).getAttribute("selectedItemPosition")

        var newPosition: String

        do {
            newPosition = random.nextInt(
                s.node.getAttribute("itemCount").toInt()
            ).toString()
        } while (newPosition == currentlySelectedItem)

        newPosition
    }

    private fun fprio(p: BigDecimal) = { _: AndroidState -> p }

    fun clickableRuleForSpinnerItems(): PositionBasedRule {
        return PositionBasedRule(
            description = "Click on an item in the spinner list",
            action = Action.CLICK_ON_SPINNER_ITEM,
            pred = xpred(
                ".[@isDisplayed = 'true' " +
                    "and @isClickable = 'true' " +
                    "and @isSpinner = 'true' " +
                    "]"
            ),
            itemPosition = randomPositionInList,
            prio = fprio(BigDecimal(1))
        )
    }

    fun clickableRuleForAdapterViewItems(): PositionBasedRule {
        return PositionBasedRule(
            description = "Click on an item in a list backed by an adapter",
            action = Action.CLICK_ON_ADAPTER_VIEW_ITEM,
            pred = xpred(
                ".[@isDisplayed = 'true' " +
                    "and @isClickable = 'true' " +
                    "and @isAdapterView = 'true' " +
                    "and not(@isSpinner = 'true') " +
                    "and @resourceName" +
                    "]"
            ),
            itemPosition = randomPositionInList,
            prio = fprio(BigDecimal(1))
        )
    }

    fun spinnerSimpleClickDeprioritizeRule(): MultiplicativeRule =
        MultiplicativeRule(
            description = "Deprioritize simple clicking of a Spinner",
            action = Action.CLICK_ON_ITEM_AT_POSITION,
            pred = xpred(
                ".[@isDisplayed = 'true' " +
                    "and @isClickable = 'true' " +
                    "and @isSpinner = 'true' " +
                    "]"
            ),
            prio = fprio(BigDecimal(0.01))
        )

    fun adapterViewSimpleClickDeprioritizeRule(): MultiplicativeRule =
        MultiplicativeRule(
            description = "Deprioritize simple clicking of an AdapterView",
            action = Action.CLICK_ON_ITEM_AT_POSITION,
            pred = xpred(
                ".[@isDisplayed = 'true' " +
                    "and @isClickable = 'true' " +
                    "and @isAdapterView = 'true' " +
                    "]"
            ),
            prio = fprio(BigDecimal(0.01))
        )
}
