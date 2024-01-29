package com.ing.mint.android.oracle.accessibility

import com.ing.mint.lib.OracleCategory

class ClickableSpanCheckOracle : AccessibilityOracle() {
    override val checkType = "ClickableSpanCheck"
    override val name = "clickable-span-check-oracle"
    override val version = "1.0.0"
    override val description = "Check to ensure that ClickableSpan is not being used in a " +
        "TextView. ClickableSpan was inaccessible because individual spans could not be selected " +
        "independently in a single TextView and because accessibility services were unable to " +
        "call ClickableSpan#onClick."
    override val categories = setOf(
        OracleCategory.A11Y,
    )
}
