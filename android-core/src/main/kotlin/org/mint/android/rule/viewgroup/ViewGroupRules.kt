package org.mint.android.rule.viewgroup

import org.mint.android.Action
import org.mint.android.rule.BasicRules
import org.mint.android.rule.BasicRules.defaultPrio
import org.mint.android.rule.BasicRules.xpred
import org.mint.android.rule.GenericRule
import org.mint.android.rule.MultiplicativeRule
import java.math.BigDecimal

object ViewGroupRules {

    fun scrollingPagerRightRule(): GenericRule =
        GenericRule(
            description = "Scroll pager to the right",
            action = Action.SCROLL_PAGER_TO_RIGHT,
            pred = xpred(
                ".[@isViewPager = 'true' and @canScrollRight = 'true' ]"
            ),
            prio = defaultPrio
        )

    fun scrollingPagerLeftRule(): GenericRule =
        GenericRule(
            description = "Scroll pager to the left",
            action = Action.SCROLL_PAGER_TO_LEFT,
            pred = xpred(
                ".[@isViewPager = 'true' and @canScrollLeft = 'true' ]"
            ),
            prio = defaultPrio
        )

    // The simple click action cannot be used for widgets contained by RecyclerView or ViewPager,
    // since it requires an unique resource name for the widget
    // and the RecyclerView and ViewPager are used to display replicated items.
    fun replicatedItemSimpleClickDeprioritizeRule(): MultiplicativeRule =
        MultiplicativeRule(
            description = "De-prioritized the clicking of individual RecyclerView or ViewPager items with simple click",
            action = Action.CLICK,
            pred = xpred(".[ancestor::*[@isRecyclerView = 'true'] or ancestor::*[@isViewPager = 'true'] ]"),
            prio = BasicRules.fprio(BigDecimal(0.01))
        )
}
