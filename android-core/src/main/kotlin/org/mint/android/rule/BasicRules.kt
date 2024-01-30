package com.ing.mint.android.rule

import com.ing.mint.android.Action
import com.ing.mint.android.AndroidState
import org.w3c.dom.Element
import java.math.BigDecimal

object BasicRules {

    fun xqueryRule(description: String, action: Action, pred: String, prio: String): GenericRule =
        GenericRule(description, action, xpred(pred), xprio(prio))

    fun xpred(s: String) = XQueryPredicate(s)
    fun xprio(s: String) = XQueryPriority(s)

    val defaultPrio = { _: AndroidState -> BigDecimal(1) }
    private val viewTag: (AndroidState) -> String = { s -> (s.node as Element).getAttribute("tag") }
    val positionInViewHierarchy: (AndroidState) -> String =
        { s -> (s.node as Element).getAttribute("positionInViewHierarchy") }

    fun fprio(p: BigDecimal) = { _: AndroidState -> p }

    private val defaultPreviousActionDeprioritizeRule: (BaseRule) -> MultiplicativeRule =
        { rule ->
            MultiplicativeRule(
                description = "De-prioritized actions that were already taken historically",
                action = rule.action,
                pred = rule.predicate(),
                prio = xprio(
                    "1 div (1 + count(./abstract-a:state/correlate:historical//action:${rule.action.tagName}))",
                ),
            )
        }

    /** For every rule that can lead to an action being performed (i.e. rules other than MultiplicativeRule)
     * create a modifying rule to de-prioritize the corresponding action rule that was applied historically.
     * This ensures that the priority modifying rules are applied selectively
     * to the same nodes as the action rules they are modifying.
     */
    fun defaultHistoricalActionsDeprioritizeRules(rules: Set<BaseRule>): Set<MultiplicativeRule> {
        return rules.filter { it !is MultiplicativeRule }.map(defaultPreviousActionDeprioritizeRule)
            .toSet()
    }

    fun simpleClickableRule(): GenericRule =
        GenericRule(
            description = "Click on any widget that has 'isClickable' as true and is displayed",
            action = Action.CLICK,
            pred = xpred(".[@isClickable = 'true' and @isDisplayed = 'true' ]"),
            prio = defaultPrio,
        )

    fun scrollingClickableRule(): PositionBasedRule =
        PositionBasedRule(
            description = "Scroll to and click any widget that is clickable, not yet displayed and can be scrolled to",
            action = Action.SCROLL_TO_AND_CLICK_ITEM_AT_POSITION,
            pred = xpred(
                ".[@isClickable = 'true'" +
                    // difference between displayed, shown and visible:
                    // displayed means that the view appears on the screen (view is also visible and shown)
                    // visible means the view has been inflated,
                    // but it might not be on screen because it is outside the visible screen or an ancestor is not visible
                    // shown means that the view and all the ancestors are visible
                    "and @isDisplayed = 'false' " +
                    "and @isShown = 'true' " +
                    "and @isVisible = 'true' " +
                    // ViewPager requires a different type of scroll action
                    "and not(ancestor::*[@isViewPager = 'true' ]) " +
                    // only widgets contained by a scrollable layout can be scrolled to
                    "and ancestor::*[@isScrollable = 'true'] ]",
            ),
            prio = defaultPrio,
            itemPosition = positionInViewHierarchy,
        )

    // This will only work in the case of items that have been attributed tags.
    fun clickableRuleForItemWithTag(): ItemWithTagClickRule =
        ItemWithTagClickRule(
            description = "Click on any displayed, clickable widget that has tags",
            action = Action.CLICK_ON_ITEM_WITH_TAG,
            pred = xpred(
                ".[@isDisplayed= 'true' " +
                    "and @isClickable= 'true' " +
                    "and @tag != '' " +
                    "]",
            ),
            itemTag = viewTag,
            prio = defaultPrio,
        )

    fun clickableRuleBasedOnPositionInViewHierarchy(): PositionBasedRule =
        PositionBasedRule(
            description = "Click on any displayed, clickable widget",
            action = Action.CLICK_ON_ITEM_AT_POSITION,
            pred = xpred(
                ".[@isDisplayed= 'true' " +
                    "and @isClickable= 'true' " +
                    "and not(exists(@tag))" +
                    "]",
            ),
            itemPosition = positionInViewHierarchy,
            prio = defaultPrio,
        )

    fun clickableRuleBasedOnPositionInViewHierarchyForPopupItem(): PositionBasedRule =
        PositionBasedRule(
            description = "Click on any displayed, clickable widget in a pop-up window",
            action = Action.CLICK_ON_ITEM_AT_POSITION_IN_POPUP,
            pred = xpred(
                ".[@isDisplayed= 'true' " +
                    "and @isClickable= 'true' " +
                    "and ancestor::*[@isPlatformPopup = 'true']" +
                    "]",
            ),
            itemPosition = positionInViewHierarchy,
            prio = defaultPrio,
        )

    fun deprioritizeClickingOnPopupItemOnCurrentRoot(): MultiplicativeRule =
        MultiplicativeRule(
            description = "Deprioritize normal clicking on widgets in pop-up windows",
            action = Action.CLICK_ON_ITEM_AT_POSITION,
            pred = xpred(
                ".[@isDisplayed= 'true' " +
                    "and @isClickable= 'true' " +
                    "and ancestor::*[@isPlatformPopup = 'true']" +
                    "]",
            ),
            prio = fprio(BigDecimal(0.01)),
        )

    fun deviceRotationRule(): GenericRule =
        GenericRule(
            description = "Change the Device Rotation to check the responsiveness of the UI.",
            action = Action.DEVICE_ROTATION_CHANGE,
            pred = xpred(".[@isDisplayed = 'true' and @class = 'com.android.internal.policy.DecorView']"),
            prio = fprio(BigDecimal(0.05)),
        )

    fun deviceThemeRule(): GenericRule =
        GenericRule(
            description = "Change the Device Theme to check the responsiveness of the UI.",
            action = Action.DEVICE_THEME_CHANGE,
            pred = xpred(".[@isDisplayed = 'true' and @class = 'com.android.internal.policy.DecorView']"),
            prio = fprio(BigDecimal(0.05)),
        )
}
