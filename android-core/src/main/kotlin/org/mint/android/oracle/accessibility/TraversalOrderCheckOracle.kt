package com.ing.mint.android.oracle.accessibility

import com.ing.mint.lib.OracleCategory

class TraversalOrderCheckOracle : AccessibilityOracle() {
    override val checkType = "TraversalOrderCheck"
    override val name = "traversal-order-check-oracle"
    override val version = "1.0.0"
    override val description = "Check to detect problems in the developer specified " +
        "accessibility traversal ordering."
    override val categories = setOf(
        OracleCategory.A11Y,
    )
}
