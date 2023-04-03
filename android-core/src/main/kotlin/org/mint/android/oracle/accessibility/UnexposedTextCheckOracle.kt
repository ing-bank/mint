package org.mint.android.oracle.accessibility

import org.mint.lib.OracleCategory

class UnexposedTextCheckOracle : AccessibilityOracle() {
    override val checkType = "UnexposedTextCheck"
    override val name = "unexposed-text-check-oracle"
    override val version = "1.0.0"
    override val description = "Checks for texts that are recognized by the OCR (Optical Character " +
        "Reader) from a view that might be blocked that would result to it not being read by the " +
        "Accessibility Service."
    override val categories = setOf(
        OracleCategory.A11Y
    )
}
