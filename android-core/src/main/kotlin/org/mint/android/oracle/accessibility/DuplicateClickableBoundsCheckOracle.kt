package org.mint.android.oracle.accessibility

import org.mint.lib.OracleCategory

class DuplicateClickableBoundsCheckOracle : AccessibilityOracle() {
    override val checkType = "DuplicateClickableBoundsCheck"
    override val name = "duplicate-clickable-bounds-check-oracle"
    override val version = "1.0.0"
    override val description = "Developers sometimes have containers marked clickable when " +
        "they don't process click events. This error is difficult to detect, but when a " +
        "container shares its bounds with a child view, that is a clear error. " +
        "This class catches that case."
    override val categories = setOf(
        OracleCategory.A11Y
    )
}
