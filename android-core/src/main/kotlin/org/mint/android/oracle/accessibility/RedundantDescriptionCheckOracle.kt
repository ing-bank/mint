package com.ing.mint.android.oracle.accessibility

import com.ing.mint.lib.OracleCategory

class RedundantDescriptionCheckOracle : AccessibilityOracle() {
    override val checkType = "RedundantDescriptionCheck"
    override val name = "redundant-description-check-oracle"
    override val version = "1.0.0"
    override val description = "Checks for speakable text that may contain redundant " +
        "or inappropriate information."
    override val categories = setOf(
        OracleCategory.A11Y,
    )
}
