package org.mint.android.oracle.accessibility

import org.mint.lib.OracleCategory

class SpeakableTextPresentCheckOracle : AccessibilityOracle() {
    override val checkType = "SpeakableTextPresentCheck"
    override val name = "speakable-text-present-check-oracle"
    override val version = "1.0.0"
    override val description = "Checks that items that require speakable text have some."
    override val categories = setOf(
        OracleCategory.A11Y
    )
}
