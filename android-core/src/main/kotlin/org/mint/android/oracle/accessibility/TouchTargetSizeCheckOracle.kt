package org.mint.android.oracle.accessibility

import org.mint.lib.OracleCategory

class TouchTargetSizeCheckOracle : AccessibilityOracle() {
    override val checkType = "TouchTargetSizeCheck"
    override val name = "touch-target-size-check-oracle"
    override val version = "1.0.0"
    override val description = "Check ensuring touch targets have a minimum size, " +
        "48x48dp by default."
    override val categories = setOf(
        OracleCategory.A11Y
    )
}
