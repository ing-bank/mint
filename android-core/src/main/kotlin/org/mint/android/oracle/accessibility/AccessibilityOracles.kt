package org.mint.android.oracle.accessibility

object AccessibilityOracles {
    val all = setOf(
        ClassNameCheckOracle(),
        ClickableSpanCheckOracle(),
        DuplicateClickableBoundsCheckOracle(),
        DuplicateSpeakableTextCheckOracle(),
        EditableContentDescCheckOracle(),
        ImageContrastCheckOracle(),
        LinkPurposeUnclearCheckOracle(),
        RedundantDescriptionCheckOracle(),
        SpeakableTextPresentCheckOracle(),
        TextContrastCheckOracle(),
        TextSizeCheckOracle(),
        TouchTargetSizeCheckOracle(),
        TraversalOrderCheckOracle(),
        UnexposedTextCheckOracle(),
    )
}
