package org.mint.espressoRunner.view

import android.view.View
import com.google.android.apps.common.testing.accessibility.framework.ViewAccessibilityUtils

/**
 * Determines if the supplied {@link View} would be focused during navigation operations with a
 * screen reader.
 */
fun View.shouldFocusView(): Boolean {
    return ViewAccessibilityUtils.shouldFocusView(this)
}
