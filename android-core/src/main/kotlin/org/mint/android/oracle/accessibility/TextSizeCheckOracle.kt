package com.ing.mint.android.oracle.accessibility

import com.ing.mint.lib.OracleCategory

class TextSizeCheckOracle : AccessibilityOracle() {
    override val checkType = "TextSizeCheck"
    override val name = "text-size-check-oracle"
    override val version = "1.0.0"
    override val description = "Looks for text that may have visibility problems " +
        "related to text scaling."
    override val categories = setOf(
        OracleCategory.A11Y,
    )
}
