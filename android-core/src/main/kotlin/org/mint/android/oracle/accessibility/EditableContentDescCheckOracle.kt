package org.mint.android.oracle.accessibility

import org.mint.lib.OracleCategory

class EditableContentDescCheckOracle : AccessibilityOracle() {
    override val checkType = "EditableContentDescCheck"
    override val name = "editable-content-desc-check-oracle"
    override val version = "1.0.0"
    override val description = "Check to ensure that an editable TextView is not labeled " +
        "by a contentDescription."
    override val categories = setOf(
        OracleCategory.A11Y,
    )
}
