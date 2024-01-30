package com.ing.mint.android.oracle.accessibility

import com.ing.mint.lib.OracleCategory

class ClassNameCheckOracle : AccessibilityOracle() {
    override val checkType = "ClassNameCheck"
    override val name = "class-name-check-oracle"
    override val version = "1.0.0"
    override val description = "Checks that the class name is supported by accessibility services."
    override val categories = setOf(
        OracleCategory.A11Y,
    )
}
