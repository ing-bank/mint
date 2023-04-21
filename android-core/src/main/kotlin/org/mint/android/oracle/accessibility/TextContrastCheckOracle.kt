package org.mint.android.oracle.accessibility

import org.mint.lib.OracleCategory

class TextContrastCheckOracle : AccessibilityOracle() {
    override val checkType = "TextContrastCheck"
    override val name = "text-contrast-check-oracle"
    override val version = "1.0.0"
    override val description = "Check that ensures text content has sufficient contrast " +
        "against its background."
    override val categories = setOf(
        OracleCategory.A11Y,
    )
}
