package org.mint.android.oracle.accessibility

import org.mint.lib.OracleCategory

class ImageContrastCheckOracle : AccessibilityOracle() {
    override val checkType = "ImageContrastCheck"
    override val name = "image-contrast-check-oracle"
    override val version = "1.0.0"
    override val description = "Check that ensures image foregrounds have sufficient " +
        "contrast against their background."
    override val categories = setOf(
        OracleCategory.A11Y,
    )
}
