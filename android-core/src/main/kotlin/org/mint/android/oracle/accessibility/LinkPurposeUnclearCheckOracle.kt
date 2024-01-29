package com.ing.mint.android.oracle.accessibility

import com.ing.mint.lib.OracleCategory

class LinkPurposeUnclearCheckOracle : AccessibilityOracle() {
    override val checkType = "LinkPurposeUnclearCheck"
    override val name = "link-purpose-unclear-check-oracle"
    override val version = "1.0.0"
    override val description = "Check to warn about a link (ClickableSpan) " +
        "whose purpose is unclear."
    override val categories = setOf(
        OracleCategory.A11Y,
    )
}
