package com.ing.mint.android.oracle.accessibility

import com.ing.mint.lib.OracleCategory

class DuplicateSpeakableTextCheckOracle : AccessibilityOracle() {
    override val checkType = "DuplicateSpeakableTextCheck"
    override val name = "duplicate-speakable-text-check-oracle"
    override val version = "1.0.0"
    override val description = "Checks if two Views in a hierarchy have the same speakable text, " +
        "which could be confusing for users."
    override val categories = setOf(
        OracleCategory.A11Y,
    )
}
